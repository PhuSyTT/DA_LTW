package com.bookstore.service.impl;

import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.Coupon;
import com.bookstore.entity.MasterOrder;
import com.bookstore.entity.SubOrder;
import com.bookstore.entity.SubOrderItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.CouponRepository;
import com.bookstore.repository.MasterOrderRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final BookItemRepository bookItemRepository;
    private final MasterOrderRepository masterOrderRepository;
    private final com.bookstore.repository.OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final BranchRepository branchRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    public OrderServiceImpl(BookItemRepository bookItemRepository,
                            MasterOrderRepository masterOrderRepository,
                            com.bookstore.repository.OrderStatusHistoryRepository orderStatusHistoryRepository,
                            UserRepository userRepository,
                            CouponRepository couponRepository,
                            BranchRepository branchRepository,
                            SimpMessageSendingOperations messagingTemplate) {
        this.bookItemRepository = bookItemRepository;
        this.masterOrderRepository = masterOrderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.branchRepository = branchRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Đảm bảo tính toàn vẹn dữ liệu khi trừ kho
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MasterOrder processCheckout(CheckoutRequestDto request, User user) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu thanh toán không hợp lệ.");
        }

        // Đảm bảo entity User được quản lý (managed) trong JPA Session hiện tại để tránh detached entity exception
        User managedUser = null;
        if (user != null && user.getId() != null) {
            managedUser = userRepository.findById(user.getId()).orElse(null);
        }

        List<BookItem> selectedItems = new ArrayList<>();
        Map<Long, Integer> itemQuantities = request.getItemQuantities();

        // 1. Kiểm tra tồn kho an toàn bằng Lock
        if (request.getBookItemIds() != null) {
            for (Long itemId : request.getBookItemIds()) {
                if (itemId == null) continue;
                BookItem item = bookItemRepository.findByIdWithLock(itemId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy bản sách với mã ID: " + itemId));

                int requestedQty = (itemQuantities != null && itemQuantities.containsKey(itemId))
                        ? Math.max(1, itemQuantities.get(itemId)) : 1;
                int currentStock = item.getStockQuantity() != null ? item.getStockQuantity() : 0;

                if (currentStock <= 0) {
                    throw new RuntimeException("Sách '" + (item.getBook() != null ? item.getBook().getTitle() : item.getSkuBarcode())
                            + "' tại chi nhánh " + (item.getBranch() != null ? item.getBranch().getBranchName() : "") + " hiện đã hết hàng.");
                }
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("Không tìm thấy cuốn sách nào hợp lệ để tiến hành tạo đơn hàng.");
        }

        // Tìm chi nhánh dự phòng nếu có item nào thiếu thông tin chi nhánh
        Branch fallbackBranch = null;
        if (request.getPreferredBranchId() != null) {
            fallbackBranch = branchRepository.findById(request.getPreferredBranchId()).orElse(null);
        }
        if (fallbackBranch == null) {
            List<Branch> allBranches = branchRepository.findAll();
            if (!allBranches.isEmpty()) {
                fallbackBranch = allBranches.get(0);
            }
        }

        final Branch effectiveFallbackBranch = fallbackBranch;

        // 2. Thuật toán tách đơn (FR-ORD-02): Nhóm sách theo Chi nhánh (Branch)
        Map<Branch, List<BookItem>> itemsGroupedByBranch = selectedItems.stream()
                .collect(Collectors.groupingBy(item -> item.getBranch() != null ? item.getBranch() : effectiveFallbackBranch));

        MasterOrder masterOrder = new MasterOrder();
        masterOrder.setMasterOrderCode("MO-" + System.currentTimeMillis());
        masterOrder.setUser(managedUser);

        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().trim().toUpperCase() : "COD";
        if ("POS".equals(paymentMethod) || request.isClickAndCollect()) {
            paymentMethod = "POS";
        } else {
            paymentMethod = "COD";
        }
        masterOrder.setPaymentMethod(paymentMethod);
        masterOrder.setPaymentStatus("PENDING");

        masterOrder.setReceiverName(request.getReceiverName() != null && !request.getReceiverName().isBlank()
                ? request.getReceiverName().trim()
                : (managedUser != null ? managedUser.getFullName() : "Khách Hàng"));
        masterOrder.setReceiverPhone(request.getReceiverPhone() != null && !request.getReceiverPhone().isBlank()
                ? request.getReceiverPhone().trim()
                : (managedUser != null ? managedUser.getPhone() : ""));
        masterOrder.setShippingAddress(request.getShippingAddress() != null && !request.getShippingAddress().isBlank()
                ? request.getShippingAddress().trim()
                : (request.isClickAndCollect() ? "Nhận trực tiếp tại quầy chi nhánh" : "Địa chỉ giao hàng"));

        List<SubOrder> subOrders = new ArrayList<>();
        BigDecimal calculatedTotalItems = BigDecimal.ZERO;
        int branchCount = Math.max(1, itemsGroupedByBranch.size());
        BigDecimal totalShippingFeeReq = (request.getShippingFee() != null && !request.isClickAndCollect())
                ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal perBranchShippingFee = totalShippingFeeReq.divide(BigDecimal.valueOf(branchCount), 0, RoundingMode.HALF_UP);

        // 3. Tạo các đơn hàng con (SubOrder)
        for (Map.Entry<Branch, List<BookItem>> entry : itemsGroupedByBranch.entrySet()) {
            Branch branch = entry.getKey();
            List<BookItem> branchItems = entry.getValue();

            SubOrder subOrder = new SubOrder();
            subOrder.setMasterOrder(masterOrder);
            subOrder.setBranch(branch);
            subOrder.setSubOrderCode("SUB-" + (branch != null ? branch.getId() : "0") + "-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 100));

            // Trạng thái đơn con ban đầu
            subOrder.setStatus("PENDING");
            subOrder.setBranchShippingFee(perBranchShippingFee);

            BigDecimal branchSubtotal = BigDecimal.ZERO;
            List<SubOrderItem> subOrderItems = new ArrayList<>();

            for (BookItem item : branchItems) {
                int orderQty = (itemQuantities != null && itemQuantities.containsKey(item.getId()))
                        ? Math.max(1, itemQuantities.get(item.getId())) : 1;
                int currentStock = item.getStockQuantity() != null ? item.getStockQuantity() : 0;

                // Trừ kho chuẩn xác theo số lượng đặt mua
                int remainingStock = Math.max(0, currentStock - orderQty);
                item.setStockQuantity(remainingStock);

                // Nếu hết kho thì đổi trạng thái, ngược lại vẫn duy trì AVAILABLE
                if (remainingStock == 0) {
                    item.setStatus(request.isClickAndCollect() ? "RESERVED" : "SOLD");
                } else {
                    item.setStatus("AVAILABLE");
                }
                bookItemRepository.save(item);

                BigDecimal unitPrice = item.getSellingPrice() != null ? item.getSellingPrice() : BigDecimal.ZERO;
                BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(orderQty));

                SubOrderItem orderItem = new SubOrderItem();
                orderItem.setSubOrder(subOrder);
                orderItem.setBookItem(item);
                orderItem.setQuantity(orderQty);
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSubtotal(itemSubtotal);

                subOrderItems.add(orderItem);
                branchSubtotal = branchSubtotal.add(itemSubtotal);
            }

            subOrder.setItems(subOrderItems);
            subOrder.setBranchSubtotal(branchSubtotal);
            subOrders.add(subOrder);
            calculatedTotalItems = calculatedTotalItems.add(branchSubtotal);
        }

        masterOrder.setSubOrders(subOrders);

        // Đồng bộ các số tiền tài chính của đơn hàng
        BigDecimal totalItemsAmount = request.getTotalItemsAmount() != null ? request.getTotalItemsAmount() : calculatedTotalItems;
        masterOrder.setTotalItemsAmount(totalItemsAmount);
        masterOrder.setTotalShippingFee(totalShippingFeeReq);

        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (request.getDiscountAmount() != null) {
            totalDiscount = totalDiscount.add(request.getDiscountAmount());
        }
        if (request.getShippingDiscountAmount() != null) {
            totalDiscount = totalDiscount.add(request.getShippingDiscountAmount());
        }
        masterOrder.setDiscountAmount(totalDiscount);

        BigDecimal finalAmount = request.getFinalAmount() != null ? request.getFinalAmount()
                : totalItemsAmount.add(totalShippingFeeReq).subtract(totalDiscount).max(BigDecimal.ZERO);
        masterOrder.setFinalAmount(finalAmount);

        // Áp dụng Coupon nếu có
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            couponRepository.findByCode(request.getCouponCode().trim()).ifPresent(coupon -> {
                masterOrder.setCoupon(coupon);
                coupon.setUsedCount((coupon.getUsedCount() != null ? coupon.getUsedCount() : 0) + 1);
                couponRepository.save(coupon);
            });
        }

        // Lưu MasterOrder sẽ kéo theo lưu SubOrder và SubOrderItem nhờ CascadeType.ALL
        MasterOrder savedMaster = masterOrderRepository.save(masterOrder);

        // 4. Khởi tạo nhật ký lịch sử tiến độ cho từng đơn hàng con
        if (savedMaster.getSubOrders() != null) {
            for (SubOrder sub : savedMaster.getSubOrders()) {
                com.bookstore.entity.OrderStatusHistory history = new com.bookstore.entity.OrderStatusHistory(
                        sub,
                        savedMaster,
                        null,
                        "PENDING",
                        "Khách hàng đã đặt đơn thành công qua hệ thống Book4Life (PTTT: " + paymentMethod + ")",
                        managedUser,
                        null,
                        sub.getBranch(),
                        "STATUS_CHANGE"
                );
                orderStatusHistoryRepository.save(history);

                // FR-FUL-01: Bắn chuông thông báo đơn hàng mới qua WebSocket cho Chi nhánh và Quản trị HQ
                try {
                    java.util.Map<String, Object> wsPayload = new java.util.HashMap<>();
                    wsPayload.put("type", "NEW_ORDER");
                    wsPayload.put("subOrderId", sub.getId());
                    wsPayload.put("subOrderCode", sub.getSubOrderCode());
                    wsPayload.put("masterOrderCode", savedMaster.getMasterOrderCode());
                    wsPayload.put("branchId", sub.getBranch() != null ? sub.getBranch().getId() : null);
                    wsPayload.put("branchName", sub.getBranch() != null ? sub.getBranch().getBranchName() : "Toàn chuỗi");
                    wsPayload.put("totalAmount", sub.getBranchSubtotal());
                    wsPayload.put("customerName", savedMaster.getReceiverName());
                    wsPayload.put("paymentMethod", savedMaster.getPaymentMethod());
                    wsPayload.put("message", "Đơn hàng mới #" + sub.getSubOrderCode() + " cần xử lý đóng gói!");

                    if (sub.getBranch() != null) {
                        messagingTemplate.convertAndSend("/topic/branch/" + sub.getBranch().getId() + "/orders", wsPayload);
                    }
                    messagingTemplate.convertAndSend("/topic/admin/orders", wsPayload);
                } catch (Exception wsEx) {
                    logger.warn("WebSocket push notification error: {}", wsEx.getMessage());
                }
            }
        }

        logger.info("Đã đồng bộ và tạo thành công MasterOrder [{}] với {} đơn hàng con.",
                savedMaster.getMasterOrderCode(), savedMaster.getSubOrders() != null ? savedMaster.getSubOrders().size() : 0);
        return savedMaster;
    }
}

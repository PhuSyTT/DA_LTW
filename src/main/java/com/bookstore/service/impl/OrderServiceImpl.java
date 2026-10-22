package com.bookstore.service.impl;

import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.MasterOrder;
import com.bookstore.entity.SubOrder;
import com.bookstore.entity.SubOrderItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.MasterOrderRepository;
import com.bookstore.service.OrderService;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final BookItemRepository bookItemRepository;
    private final MasterOrderRepository masterOrderRepository;
    private final com.bookstore.repository.OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    public OrderServiceImpl(BookItemRepository bookItemRepository,
                            MasterOrderRepository masterOrderRepository,
                            com.bookstore.repository.OrderStatusHistoryRepository orderStatusHistoryRepository,
                            SimpMessageSendingOperations messagingTemplate) {
        this.bookItemRepository = bookItemRepository;
        this.masterOrderRepository = masterOrderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Đảm bảo tính toàn vẹn dữ liệu khi trừ kho
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MasterOrder processCheckout(CheckoutRequestDto request, User user) {
        List<BookItem> selectedItems = new ArrayList<>();

        // 1. Kiểm tra tồn kho an toàn bằng Lock
        for (Long itemId : request.getBookItemIds()) {
            BookItem item = bookItemRepository.findByIdWithLock(itemId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
            
            if (item.getStockQuantity() < 1 || !"AVAILABLE".equals(item.getStatus())) {
                throw new RuntimeException("Sách " + item.getSkuBarcode() + " đã hết hàng.");
            }
            selectedItems.add(item);
        }

        // 2. Thuật toán tách đơn (FR-ORD-02): Nhóm sách theo Chi nhánh (Branch)
        Map<Branch, List<BookItem>> itemsGroupedByBranch = selectedItems.stream()
                .collect(Collectors.groupingBy(BookItem::getBranch));

        MasterOrder masterOrder = new MasterOrder();
        masterOrder.setMasterOrderCode("MO-" + System.currentTimeMillis());
        masterOrder.setUser(user);
        masterOrder.setPaymentStatus("PENDING");
        masterOrder.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD");
        masterOrder.setReceiverName(request.getReceiverName() != null ? request.getReceiverName() : (user != null ? user.getFullName() : "Khách Hàng"));
        masterOrder.setReceiverPhone(request.getReceiverPhone() != null ? request.getReceiverPhone() : (user != null ? user.getPhone() : ""));
        masterOrder.setShippingAddress(request.getShippingAddress() != null ? request.getShippingAddress() : "Nhận tại quầy");
        
        List<SubOrder> subOrders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Tạo các đơn hàng con
        for (Map.Entry<Branch, List<BookItem>> entry : itemsGroupedByBranch.entrySet()) {
            Branch branch = entry.getKey();
            List<BookItem> branchItems = entry.getValue();

            SubOrder subOrder = new SubOrder();
            subOrder.setMasterOrder(masterOrder);
            subOrder.setBranch(branch);
            subOrder.setSubOrderCode("SUB-" + branch.getId() + "-" + System.currentTimeMillis());
            
            // Xử lý giữ sách tại quầy (FR-ORD-04)
            String nextStatus = request.isClickAndCollect() ? "RESERVED" : "SOLD";
            subOrder.setStatus("PENDING");

            BigDecimal subtotal = BigDecimal.ZERO;
            List<SubOrderItem> subOrderItems = new ArrayList<>();

            for (BookItem item : branchItems) {
                // Trừ kho và đổi trạng thái
                item.setStockQuantity(item.getStockQuantity() - 1);
                item.setStatus(nextStatus);
                bookItemRepository.save(item);

                SubOrderItem orderItem = new SubOrderItem();
                orderItem.setSubOrder(subOrder);
                orderItem.setBookItem(item);
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(item.getSellingPrice());
                
                subOrderItems.add(orderItem);
                subtotal = subtotal.add(item.getSellingPrice());
            }

            subOrder.setItems(subOrderItems);
            subOrder.setBranchSubtotal(subtotal);
            subOrders.add(subOrder);
            totalAmount = totalAmount.add(subtotal);
        }

        masterOrder.setSubOrders(subOrders);
        masterOrder.setTotalItemsAmount(totalAmount);
        masterOrder.setFinalAmount(totalAmount);

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
                        "Khách hàng đã đặt đơn thành công qua hệ thống",
                        user,
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
                    wsPayload.put("branchId", sub.getBranch() != null ? sub.getBranch().getId() : null);
                    wsPayload.put("branchName", sub.getBranch() != null ? sub.getBranch().getBranchName() : "Toàn chuỗi");
                    wsPayload.put("totalAmount", sub.getBranchSubtotal());
                    wsPayload.put("customerName", savedMaster.getReceiverName());
                    wsPayload.put("message", "Đơn hàng mới #" + sub.getSubOrderCode() + " cần xử lý đóng gói!");

                    if (sub.getBranch() != null) {
                        messagingTemplate.convertAndSend("/topic/branch/" + sub.getBranch().getId() + "/orders", wsPayload);
                    }
                    messagingTemplate.convertAndSend("/topic/admin/orders", wsPayload);
                } catch (Exception wsEx) {
                    // Không để lỗi đẩy WebSocket làm rollback đơn hàng
                }
            }
        }

        return savedMaster;
    }
}
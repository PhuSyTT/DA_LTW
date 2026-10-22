package com.bookstore.controller.admin;

import com.bookstore.dto.PosCheckoutRequestDto;
import com.bookstore.dto.PosReceiptDto;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class PosController {

    private static final Logger log = LoggerFactory.getLogger(PosController.class);

    private final BranchRepository branchRepository;
    private final BookItemRepository bookItemRepository;
    private final CouponRepository couponRepository;
    private final MasterOrderRepository masterOrderRepository;
    private final SubOrderRepository subOrderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public PosController(BranchRepository branchRepository,
                         BookItemRepository bookItemRepository,
                         CouponRepository couponRepository,
                         MasterOrderRepository masterOrderRepository,
                         SubOrderRepository subOrderRepository,
                         OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.branchRepository = branchRepository;
        this.bookItemRepository = bookItemRepository;
        this.couponRepository = couponRepository;
        this.masterOrderRepository = masterOrderRepository;
        this.subOrderRepository = subOrderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    /**
     * RBAC Màn hình POS bán hàng tại quầy:
     * - Dành cho: CASHIER, STAFF_GENERAL, BRANCH_MANAGER, ADMIN.
     * - Chặn: STAFF_APPRAISER, CUSTOMER.
     */
    @GetMapping({"/admin/pos", "/staff/pos"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER', 'ROLE_CASHIER', 'CASHIER', 'ROLE_STAFF_GENERAL', 'STAFF_GENERAL')")
    public String showPosScreen(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Model model
    ) {
        boolean isAdmin = userDetails != null && (
                "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRoleName()) ||
                "ADMIN".equalsIgnoreCase(userDetails.getRoleName())
        );

        List<Branch> allBranches = branchRepository.findAll();
        Branch selectedBranch;

        if (isAdmin) {
            // Admin có quyền chọn quầy của bất kỳ chi nhánh nào
            if (branchId != null) {
                selectedBranch = branchRepository.findById(branchId).orElse(allBranches.isEmpty() ? null : allBranches.get(0));
            } else {
                selectedBranch = allBranches.isEmpty() ? null : allBranches.get(0);
            }
        } else {
            // Thu ngân và Quản lý chi nhánh bị khóa cứng vào chi nhánh phụ trách của mình
            Long userBranchId = (userDetails != null) ? userDetails.getBranchId() : null;
            if (userBranchId != null) {
                selectedBranch = branchRepository.findById(userBranchId).orElse(allBranches.isEmpty() ? null : allBranches.get(0));
            } else {
                selectedBranch = allBranches.isEmpty() ? null : allBranches.get(0);
            }
        }

        model.addAttribute("branches", allBranches);
        model.addAttribute("currentBranch", selectedBranch);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("cashierName", userDetails != null ? userDetails.getFullName() : "Thu Ngân");
        model.addAttribute("cashierRole", userDetails != null ? userDetails.getRoleName() : "STAFF");
        model.addAttribute("activeMenu", "pos");

        return "admin/pos/index";
    }

    /**
     * API Tìm kiếm sách theo SKU / Tên / ISBN tại chi nhánh
     */
    @GetMapping("/api/pos/search")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER', 'ROLE_CASHIER', 'CASHIER', 'ROLE_STAFF_GENERAL', 'STAFF_GENERAL')")
    public ResponseEntity<?> searchPosItems(
            @RequestParam("branchId") Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 30);
        org.springframework.data.domain.Page<BookItem> pageResult = bookItemRepository.searchBookItems(cleanKeyword, branchId, null, "AVAILABLE", pageable);

        List<Map<String, Object>> items = new ArrayList<>();
        for (BookItem item : pageResult.getContent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("skuBarcode", item.getSkuBarcode());
            map.put("title", item.getBook() != null ? item.getBook().getTitle() : "Sách");
            map.put("author", (item.getBook() != null && item.getBook().getAuthor() != null) ? item.getBook().getAuthor().getName() : "");
            map.put("conditionGrade", item.getConditionGrade());
            map.put("price", item.getSellingPrice());
            map.put("shelfLocation", item.getShelfLocation() != null ? item.getShelfLocation() : "Tại quầy");
            
            String coverUrl = null;
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                coverUrl = item.getImages().get(0).getImageUrl();
            } else if (item.getBook() != null) {
                coverUrl = item.getBook().getCoverImageUrl();
            }
            map.put("coverImage", coverUrl);
            items.add(map);
        }

        return ResponseEntity.ok(items);
    }

    /**
     * API Thanh toán POS tại quầy (Tạo đơn, trừ tồn kho, tính tiền thối, trả về biên lai)
     */
    @PostMapping("/api/pos/checkout")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER', 'ROLE_CASHIER', 'CASHIER', 'ROLE_STAFF_GENERAL', 'STAFF_GENERAL')")
    public ResponseEntity<?> processPosCheckout(
            @RequestBody PosCheckoutRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            if (request.getBookItemIds() == null || request.getBookItemIds().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng thanh toán không có sản phẩm nào!"));
            }

            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh ID: " + request.getBranchId()));

            // 1. Kiểm tra và khóa các sản phẩm (Pessimistic Lock)
            List<BookItem> selectedItems = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;

            for (Long itemId : request.getBookItemIds()) {
                BookItem item = bookItemRepository.findByIdWithLock(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + itemId));

                if (!"AVAILABLE".equalsIgnoreCase(item.getStatus())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Sách '" + item.getSkuBarcode() + "' đã được bán hoặc đặt giữ!"));
                }

                if (!item.getBranch().getId().equals(branch.getId())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Sách '" + item.getSkuBarcode() + "' thuộc chi nhánh khác, không thể bán tại quầy này!"));
                }

                selectedItems.add(item);
                subtotal = subtotal.add(item.getSellingPrice());
            }

            // 2. Xử lý Coupon (nếu có)
            BigDecimal discountAmount = BigDecimal.ZERO;
            Coupon appliedCoupon = null;

            if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
                String cleanCode = request.getCouponCode().trim().toUpperCase();
                Optional<Coupon> couponOpt = couponRepository.findByCodeAndIsActiveTrue(cleanCode);
                if (couponOpt.isPresent()) {
                    Coupon coupon = couponOpt.get();
                    // Kiểm tra chi nhánh
                    if (coupon.getApplicableBranch() == null || coupon.getApplicableBranch().getId().equals(branch.getId())) {
                        // Kiểm tra đơn tối thiểu
                        if (coupon.getMinOrderAmount() == null || subtotal.compareTo(coupon.getMinOrderAmount()) >= 0) {
                            if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
                                BigDecimal discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                                    discount = coupon.getMaxDiscountAmount();
                                }
                                discountAmount = discount;
                            } else {
                                discountAmount = coupon.getDiscountValue();
                            }
                            appliedCoupon = coupon;
                            coupon.setUsedCount((coupon.getUsedCount() != null ? coupon.getUsedCount() : 0) + 1);
                            couponRepository.save(coupon);
                        }
                    }
                }
            }

            BigDecimal finalTotal = subtotal.subtract(discountAmount);
            if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                finalTotal = BigDecimal.ZERO;
            }

            BigDecimal amountGiven = (request.getAmountGiven() != null) ? request.getAmountGiven() : finalTotal;
            BigDecimal changeAmount = amountGiven.subtract(finalTotal);
            if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                changeAmount = BigDecimal.ZERO;
            }

            String customerName = (request.getCustomerName() != null && !request.getCustomerName().isBlank())
                    ? request.getCustomerName().trim() : "Khách lẻ tại quầy";
            String customerPhone = (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank())
                    ? request.getCustomerPhone().trim() : "Tại quầy";

            // 3. Tạo MasterOrder
            String masterOrderCode = "POS-" + System.currentTimeMillis();
            MasterOrder masterOrder = new MasterOrder();
            masterOrder.setMasterOrderCode(masterOrderCode);
            masterOrder.setUser(userDetails != null ? userDetails.getUser() : null);
            masterOrder.setCoupon(appliedCoupon);
            masterOrder.setTotalItemsAmount(subtotal);
            masterOrder.setTotalShippingFee(BigDecimal.ZERO);
            masterOrder.setDiscountAmount(discountAmount);
            masterOrder.setFinalAmount(finalTotal);
            masterOrder.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
            masterOrder.setPaymentStatus("PAID");
            masterOrder.setReceiverName(customerName);
            masterOrder.setReceiverPhone(customerPhone);
            masterOrder.setShippingAddress("Bán trực tiếp tại: " + branch.getBranchName());
            masterOrder.setCreatedAt(LocalDateTime.now());

            // 4. Tạo SubOrder
            SubOrder subOrder = new SubOrder();
            subOrder.setSubOrderCode("SUB-" + masterOrderCode);
            subOrder.setMasterOrder(masterOrder);
            subOrder.setBranch(branch);
            subOrder.setBranchSubtotal(subtotal);
            subOrder.setBranchShippingFee(BigDecimal.ZERO);
            subOrder.setStatus("COMPLETED");
            subOrder.setConfirmedAt(LocalDateTime.now());
            subOrder.setCompletedAt(LocalDateTime.now());
            subOrder.setCreatedAt(LocalDateTime.now());

            List<SubOrderItem> orderItems = new ArrayList<>();
            for (BookItem item : selectedItems) {
                SubOrderItem subItem = new SubOrderItem();
                subItem.setSubOrder(subOrder);
                subItem.setBookItem(item);
                subItem.setQuantity(1);
                subItem.setUnitPrice(item.getSellingPrice());
                subItem.setSubtotal(item.getSellingPrice());
                orderItems.add(subItem);

                // Cập nhật trạng thái sách thành SOLD và trừ tồn kho
                item.setStatus("SOLD");
                item.setStockQuantity(0);
                bookItemRepository.save(item);
            }
            subOrder.setItems(orderItems);
            masterOrder.setSubOrders(Collections.singletonList(subOrder));

            MasterOrder savedMaster = masterOrderRepository.save(masterOrder);

            // 5. Ghi lịch sử đơn hàng
            OrderStatusHistory history = new OrderStatusHistory(
                    subOrder,
                    savedMaster,
                    null,
                    "COMPLETED",
                    "Đơn hàng bán lẻ trực tiếp tại quầy POS đã thanh toán thành công",
                    userDetails != null ? userDetails.getUser() : null,
                    null,
                    branch,
                    "POS_SALE"
            );
            orderStatusHistoryRepository.save(history);

            // 6. Xây dựng DTO Hóa đơn trả về cho màn hình in
            PosReceiptDto receipt = new PosReceiptDto();
            receipt.setOrderCode(masterOrderCode);
            receipt.setDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            receipt.setBranchName(branch.getBranchName());
            receipt.setBranchAddress(branch.getAddress());
            receipt.setBranchPhone(branch.getPhone() != null ? branch.getPhone() : "1900 6868");
            receipt.setCashierName(userDetails != null ? userDetails.getFullName() : "Thu Ngân");
            receipt.setCustomerName(customerName);
            receipt.setCustomerPhone(customerPhone);
            receipt.setSubtotal(subtotal);
            receipt.setDiscountAmount(discountAmount);
            receipt.setCouponCode(appliedCoupon != null ? appliedCoupon.getCode() : null);
            receipt.setFinalTotal(finalTotal);
            receipt.setPaymentMethod("BANK_TRANSFER".equalsIgnoreCase(request.getPaymentMethod()) ? "Chuyển Khoản QR" : "Tiền Mặt");
            receipt.setAmountGiven(amountGiven);
            receipt.setChangeAmount(changeAmount);

            List<PosReceiptDto.PosReceiptItemDto> receiptItems = new ArrayList<>();
            for (BookItem item : selectedItems) {
                receiptItems.add(new PosReceiptDto.PosReceiptItemDto(
                        item.getBook() != null ? item.getBook().getTitle() : "Sách",
                        item.getSkuBarcode(),
                        item.getConditionGrade(),
                        item.getSellingPrice()
                ));
            }
            receipt.setItems(receiptItems);

            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý đơn POS: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi thanh toán: " + e.getMessage()));
        }
    }
}

package com.bookstore.controller;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.CartItemDto;
import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.Coupon;
import com.bookstore.entity.MasterOrder;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.CouponRepository;
import com.bookstore.service.BranchService;
import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final BranchService branchService;
    private final BookItemRepository bookItemRepository;
    private final CouponRepository couponRepository;
    private final CategoryRepository categoryRepository;

    public CartController(CartService cartService, OrderService orderService,
                          BranchService branchService, BookItemRepository bookItemRepository,
                          CouponRepository couponRepository, CategoryRepository categoryRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.branchService = branchService;
        this.bookItemRepository = bookItemRepository;
        this.couponRepository = couponRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("branches", branchService.getAllActiveBranches());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("availableCoupons", getActiveAvailableCoupons());
        model.addAttribute("pageTitle", "Giỏ Hàng Sách Cũ Của Bạn - Book4Life");
        return "cart";
    }

    @GetMapping("/checkout")
    public String viewCheckout(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("branches", branchService.getAllActiveBranches());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("availableCoupons", getActiveAvailableCoupons());
        model.addAttribute("pageTitle", "Thanh Toán & Hóa Đơn Tạm Tính - Book4Life");
        model.addAttribute("autoOpenCheckout", true);
        return "cart";
    }

    private List<Coupon> getActiveAvailableCoupons() {
        try {
            return couponRepository.findAllByOrderByIdDesc().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .toList();
        } catch (Exception e) {
            logger.warn("Không thể tải danh sách coupon có sẵn: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "bookItemId", required = false) Long bookItemId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session
    ) {
        if (bookId == null && bookItemId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã sách không hợp lệ!"));
        }

        CartDto cart = cartService.addToCart(bookId, bookItemId, quantity, session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "success",
                "message", "Đã thêm vào giỏ hàng thành công!",
                "totalQuantity", cart.getTotalQuantity(),
                "count", cart.getTotalQuantity(),
                "subtotal", cart.getSubtotal(),
                "grandTotal", cart.getGrandTotal()
        ));
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            HttpSession session
    ) {
        CartDto cart = cartService.updateQuantity(bookId, quantity, session);
        BigDecimal itemSubtotal = BigDecimal.ZERO;
        for (CartItemDto item : cart.getItems()) {
            if (item.getBookId().equals(bookId)) {
                itemSubtotal = item.getSubtotal();
                break;
            }
        }

        Map<String, Object> resp = buildCartResponse(cart, "Đã cập nhật số lượng thành công!", true);
        resp.put("itemSubtotal", itemSubtotal);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeItem(
            @RequestParam("bookId") Long bookId,
            HttpSession session
    ) {
        CartDto cart = cartService.removeFromCart(bookId, session);
        return ResponseEntity.ok(buildCartResponse(cart, "Đã xóa sách khỏi giỏ hàng!", true));
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(HttpSession session) {
        int count = cartService.getTotalQuantity(session);
        return ResponseEntity.ok(Map.of("totalQuantity", count, "count", count));
    }

    @PostMapping("/cart/apply-coupon")
    @ResponseBody
    public ResponseEntity<?> applyCoupon(
            @RequestParam("code") String code,
            HttpSession session
    ) {
        CartDto cart = cartService.applyCoupon(code, session);
        boolean applied = Boolean.TRUE.equals(cart.getLastCouponSuccess());
        String msg = cart.getLastCouponMessage() != null ? cart.getLastCouponMessage() :
                (applied ? "Áp dụng mã giảm giá thành công!" : "Mã không hợp lệ hoặc chưa đủ điều kiện!");

        return ResponseEntity.ok(buildCartResponse(cart, msg, applied));
    }

    @PostMapping("/cart/remove-coupon")
    @ResponseBody
    public ResponseEntity<?> removeCoupon(
            @RequestParam(value = "type", required = false) String type,
            HttpSession session
    ) {
        CartDto cart;
        if ("shipping".equalsIgnoreCase(type)) {
            cart = cartService.removeShippingCoupon(session);
        } else if ("order".equalsIgnoreCase(type)) {
            cart = cartService.removeOrderCoupon(session);
        } else {
            cart = cartService.removeCoupon(session);
        }

        String msg = cart.getLastCouponMessage() != null ? cart.getLastCouponMessage() : "Đã gỡ mã giảm giá";
        return ResponseEntity.ok(buildCartResponse(cart, msg, true));
    }

    @PostMapping("/cart/switch-branch")
    @ResponseBody
    public ResponseEntity<?> switchBranch(
            @RequestParam("bookId") Long bookId,
            @RequestParam("targetBookItemId") Long targetBookItemId,
            HttpSession session
    ) {
        CartDto cart = cartService.switchItemBranch(bookId, targetBookItemId, session);
        return ResponseEntity.ok(buildCartResponse(cart, "Đã chuyển đổi chi nhánh và cập nhật giá mới thành công!", true));
    }

    @PostMapping("/cart/optimize-best-price")
    @ResponseBody
    public ResponseEntity<?> optimizeBestPrice(HttpSession session) {
        CartDto cart = cartService.optimizeCartForBestPrice(session);
        return ResponseEntity.ok(buildCartResponse(cart, "Toàn bộ giỏ hàng đã được tự động tối ưu sang các chi nhánh có giá tốt nhất!", true));
    }

    private Map<String, Object> buildCartResponse(CartDto cart, String message, boolean success) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        resp.put("status", success ? "success" : "error");
        resp.put("message", message != null ? message : "");
        resp.put("totalQuantity", cart.getTotalQuantity());
        resp.put("count", cart.getTotalQuantity());
        resp.put("subtotal", cart.getSubtotal());
        resp.put("discountAmount", cart.getDiscountAmount());
        resp.put("shippingFee", cart.getShippingFee());
        resp.put("baseShippingFee", cart.getBaseShippingFee());
        resp.put("shippingDiscountAmount", cart.getShippingDiscountAmount());
        resp.put("shippingPromoMessage", cart.getShippingPromoMessage());
        resp.put("grandTotal", cart.getGrandTotal());
        resp.put("isEmpty", cart.isEmpty());
        resp.put("couponCode", cart.getCouponCode() != null ? cart.getCouponCode() : "");
        resp.put("orderCouponCode", cart.getOrderCouponCode() != null ? cart.getOrderCouponCode() : "");
        resp.put("orderCouponType", cart.getOrderCouponType() != null ? cart.getOrderCouponType() : "");
        resp.put("shippingCouponCode", cart.getShippingCouponCode() != null ? cart.getShippingCouponCode() : "");
        resp.put("shippingCouponType", cart.getShippingCouponType() != null ? cart.getShippingCouponType() : "");
        resp.put("couponType", cart.getCouponType() != null ? cart.getCouponType() : "");
        resp.put("totalSavings", cart.getTotalSavingsPotential());
        resp.put("hasSavings", cart.hasSavingsAvailable());
        return resp;
    }

    @GetMapping("/api/cart/book/{bookId}/branches")
    @ResponseBody
    public ResponseEntity<?> getAvailableBranchesForBook(@PathVariable("bookId") Long bookId) {
        List<BookItem> items = bookItemRepository.findAvailableItemsByBookId(bookId);
        BigDecimal minPrice = items.stream()
                .filter(i -> i.getSellingPrice() != null && i.getStockQuantity() != null && i.getStockQuantity() > 0)
                .map(BookItem::getSellingPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);

        List<Map<String, Object>> result = new ArrayList<>();
        for (BookItem item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("bookItemId", item.getId());
            map.put("branchId", item.getBranch().getId());
            map.put("branchName", item.getBranch().getBranchName());
            map.put("branchAddress", item.getBranch().getAddress());
            map.put("sellingPrice", item.getSellingPrice());
            map.put("conditionGrade", item.getConditionGrade());
            map.put("stockQuantity", item.getStockQuantity());
            map.put("isBestPrice", minPrice != null && item.getSellingPrice() != null && item.getSellingPrice().compareTo(minPrice) == 0);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/cart/calculate-shipping")
    @ResponseBody
    public ResponseEntity<?> calculateShipping(
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "clickAndCollect", defaultValue = "false") boolean clickAndCollect,
            HttpSession session
    ) {
        CartDto cart = cartService.getCart(session);
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal baseShip;
        String deliveryEst;

        if (clickAndCollect) {
            baseShip = BigDecimal.ZERO;
            deliveryEst = "Sẵn sàng trong 2 giờ tại quầy chi nhánh";
        } else if (subtotal.compareTo(new BigDecimal("200000")) >= 0) {
            baseShip = BigDecimal.ZERO;
            deliveryEst = "Freeship toàn quốc (Đơn từ 200.000₫) • 1 - 3 ngày";
        } else {
            boolean isLocalHcm = province != null && (province.contains("Hồ Chí Minh") || province.contains("TP.HCM"));
            boolean isInnerDistrict = district != null && (
                    district.contains("1") || district.contains("3") || district.contains("5") ||
                    district.contains("7") || district.contains("10") || district.contains("Thủ Đức") ||
                    district.contains("Bình Thạnh") || district.contains("Phú Nhuận") ||
                    district.contains("Tân Bình") || district.contains("Gò Vấp") || district.contains("Tân Phú") ||
                    district.contains("4") || district.contains("6") || district.contains("8") || district.contains("11")
            );

            if (isLocalHcm && isInnerDistrict) {
                baseShip = new BigDecimal("15000");
                deliveryEst = "Giao siêu tốc nội thành: 24h - 48h";
            } else if (isLocalHcm) {
                baseShip = new BigDecimal("22000");
                deliveryEst = "Giao ngoại thành TP.HCM: 1 - 2 ngày";
            } else if (province != null && (province.contains("Bình Dương") || province.contains("Đồng Nai") || province.contains("Long An") || province.contains("Vũng Tàu"))) {
                baseShip = new BigDecimal("28000");
                deliveryEst = "Giao tỉnh lân cận: 2 - 3 ngày";
            } else {
                baseShip = new BigDecimal("35000");
                deliveryEst = "Chuyển phát nhanh liên tỉnh: 3 - 5 ngày";
            }
        }

        BigDecimal shippingDiscount = BigDecimal.ZERO;
        if (!clickAndCollect && cart.hasShippingCoupon()) {
            if ("FREESHIP".equalsIgnoreCase(cart.getShippingCouponCode()) || "FREESHIPMAX".equalsIgnoreCase(cart.getShippingCouponCode())) {
                shippingDiscount = baseShip;
            } else if ("SHIP10K".equalsIgnoreCase(cart.getShippingCouponCode())) {
                shippingDiscount = new BigDecimal("10000").min(baseShip);
            }
        }

        BigDecimal finalShippingFee = baseShip.subtract(shippingDiscount).max(BigDecimal.ZERO);
        BigDecimal orderDiscount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(finalShippingFee).subtract(orderDiscount).max(BigDecimal.ZERO);

        return ResponseEntity.ok(Map.of(
                "subtotal", subtotal,
                "baseShippingFee", baseShip,
                "shippingDiscount", shippingDiscount,
                "shippingFee", finalShippingFee,
                "orderDiscount", orderDiscount,
                "grandTotal", grandTotal,
                "deliveryEstimate", deliveryEst
        ));
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam(value = "shippingAddress", required = false) String shippingAddress,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "streetAddress", required = false) String streetAddress,
            @RequestParam(value = "orderNote", required = false) String orderNote,
            @RequestParam(value = "shippingFee", required = false) BigDecimal customShippingFee,
            @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
            @RequestParam(value = "clickAndCollect", defaultValue = "false") boolean clickAndCollect,
            @RequestParam(value = "preferredBranchId", required = false) Long preferredBranchId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstore.security.CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        CartDto cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống! Vui lòng chọn sách trước.");
            return "redirect:/cart";
        }

        try {
            // Chuẩn hóa địa chỉ nhận hàng từ 3 cấp hành chính nếu có
            if ((shippingAddress == null || shippingAddress.isBlank()) && province != null && !province.isBlank()) {
                StringBuilder sb = new StringBuilder();
                if (streetAddress != null && !streetAddress.isBlank()) sb.append(streetAddress.trim()).append(", ");
                if (ward != null && !ward.isBlank()) sb.append(ward.trim()).append(", ");
                if (district != null && !district.isBlank()) sb.append(district.trim()).append(", ");
                sb.append(province.trim());
                shippingAddress = sb.toString();
            } else if (shippingAddress == null || shippingAddress.isBlank()) {
                shippingAddress = clickAndCollect ? "Nhận trực tiếp tại quầy chi nhánh" : "Địa chỉ giao hàng";
            }

            List<Long> bookItemIds = new ArrayList<>();
            Map<Long, Integer> itemQuantities = new HashMap<>();

            for (CartItemDto item : cart.getItems()) {
                Long effectiveItemId = item.getBookItemId();

                // Nếu item chưa có bookItemId, tự động tìm BookItem khả dụng tốt nhất của cuốn sách này
                if (effectiveItemId == null && item.getBookId() != null) {
                    List<BookItem> availItems = bookItemRepository.findAvailableItemsByBookId(item.getBookId());
                    if (!availItems.isEmpty()) {
                        BookItem best = availItems.stream()
                                .filter(i -> i.getSellingPrice() != null && i.getStockQuantity() != null && i.getStockQuantity() > 0)
                                .min(java.util.Comparator.comparing(BookItem::getSellingPrice))
                                .orElse(availItems.get(0));
                        effectiveItemId = best.getId();
                        item.setBookItemId(best.getId());
                        if (best.getBranch() != null) {
                            item.setBranchId(best.getBranch().getId());
                            item.setBranchName(best.getBranch().getBranchName());
                        }
                    } else {
                        List<BookItem> anyItems = bookItemRepository.findByBookId(item.getBookId());
                        if (!anyItems.isEmpty()) {
                            effectiveItemId = anyItems.get(0).getId();
                            item.setBookItemId(effectiveItemId);
                        }
                    }
                }

                if (effectiveItemId != null) {
                    bookItemIds.add(effectiveItemId);
                    itemQuantities.put(effectiveItemId, item.getQuantity());
                }
            }

            if (bookItemIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy cuốn sách nào khả dụng trong kho để thanh toán. Vui lòng thử lại!");
                return "redirect:/cart";
            }

            // Create checkout request with full synchronized data
            CheckoutRequestDto req = new CheckoutRequestDto();
            req.setReceiverName(receiverName);
            req.setReceiverPhone(receiverPhone);
            req.setShippingAddress(shippingAddress);
            req.setProvince(province);
            req.setDistrict(district);
            req.setWard(ward);
            req.setStreetAddress(streetAddress);
            req.setOrderNote(orderNote);

            String effectivePaymentMethod = ("POS".equalsIgnoreCase(paymentMethod) || clickAndCollect) ? "POS" : "COD";
            req.setPaymentMethod(effectivePaymentMethod);
            req.setClickAndCollect(clickAndCollect);
            req.setPreferredBranchId(preferredBranchId);
            req.setBookItemIds(bookItemIds);
            req.setItemQuantities(itemQuantities);

            // Pass amounts and discounts from cart / dynamic calculation
            req.setTotalItemsAmount(cart.getSubtotal());
            BigDecimal effectiveShippingFee;
            if (clickAndCollect) {
                effectiveShippingFee = BigDecimal.ZERO;
            } else if (customShippingFee != null && customShippingFee.compareTo(BigDecimal.ZERO) >= 0) {
                effectiveShippingFee = customShippingFee;
            } else {
                effectiveShippingFee = cart.getShippingFee();
            }
            req.setShippingFee(effectiveShippingFee);

            BigDecimal effectiveDiscount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
            req.setDiscountAmount(effectiveDiscount);
            req.setShippingDiscountAmount(clickAndCollect ? BigDecimal.ZERO : cart.getShippingDiscountAmount());

            BigDecimal finalAmount = cart.getSubtotal().add(effectiveShippingFee).subtract(effectiveDiscount).max(BigDecimal.ZERO);
            req.setFinalAmount(finalAmount);

            req.setCouponCode(cart.getOrderCouponCode());
            req.setShippingCouponCode(cart.getShippingCouponCode());

            com.bookstore.entity.User currentUser = (userDetails != null) ? userDetails.getUser() : null;
            MasterOrder masterOrder = orderService.processCheckout(req, currentUser);

            // Clear cart upon successful order placement
            cartService.clearCart(session);

            redirectAttributes.addFlashAttribute("orderSuccess", true);
            redirectAttributes.addFlashAttribute("orderCode", masterOrder.getMasterOrderCode());
            redirectAttributes.addFlashAttribute("orderId", masterOrder.getId());
            redirectAttributes.addFlashAttribute("paymentMethod", masterOrder.getPaymentMethod());
            redirectAttributes.addFlashAttribute("finalAmount", masterOrder.getFinalAmount());
            redirectAttributes.addFlashAttribute("receiverName", receiverName);
            redirectAttributes.addFlashAttribute("receiverPhone", receiverPhone);
            redirectAttributes.addFlashAttribute("shippingAddress", shippingAddress);
            redirectAttributes.addFlashAttribute("clickAndCollect", clickAndCollect);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Đơn hàng " + masterOrder.getMasterOrderCode() + " đã được ghi nhận trên toàn hệ thống.");
            return "redirect:/cart";
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý đặt hàng: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hoàn tất đơn hàng: " + e.getMessage());
            return "redirect:/cart";
        }
    }
}


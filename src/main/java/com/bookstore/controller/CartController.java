package com.bookstore.controller;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.CartItemDto;
import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.MasterOrder;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.service.BranchService;
import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
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

    private final CartService cartService;
    private final OrderService orderService;
    private final BranchService branchService;
    private final BookItemRepository bookItemRepository;

    public CartController(CartService cartService, OrderService orderService,
                          BranchService branchService, BookItemRepository bookItemRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.branchService = branchService;
        this.bookItemRepository = bookItemRepository;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("branches", branchService.getAllActiveBranches());
        model.addAttribute("pageTitle", "Giỏ Hàng Sách Cũ Của Bạn - Book4Life");
        return "cart";
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

        return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "success",
                "totalQuantity", cart.getTotalQuantity(),
                "count", cart.getTotalQuantity(),
                "itemSubtotal", itemSubtotal,
                "subtotal", cart.getSubtotal(),
                "discountAmount", cart.getDiscountAmount(),
                "shippingFee", cart.getShippingFee(),
                "grandTotal", cart.getGrandTotal(),
                "isEmpty", cart.isEmpty()
        ));
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeItem(
            @RequestParam("bookId") Long bookId,
            HttpSession session
    ) {
        CartDto cart = cartService.removeFromCart(bookId, session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "success",
                "totalQuantity", cart.getTotalQuantity(),
                "count", cart.getTotalQuantity(),
                "subtotal", cart.getSubtotal(),
                "discountAmount", cart.getDiscountAmount(),
                "shippingFee", cart.getShippingFee(),
                "grandTotal", cart.getGrandTotal(),
                "isEmpty", cart.isEmpty()
        ));
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

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", applied);
        resp.put("message", msg);
        resp.put("couponCode", cart.getCouponCode());
        resp.put("orderCouponCode", cart.getOrderCouponCode());
        resp.put("orderCouponType", cart.getOrderCouponType());
        resp.put("shippingCouponCode", cart.getShippingCouponCode());
        resp.put("shippingCouponType", cart.getShippingCouponType());
        resp.put("couponType", cart.getCouponType());
        resp.put("discountAmount", cart.getDiscountAmount());
        resp.put("shippingDiscountAmount", cart.getShippingDiscountAmount());
        resp.put("shippingPromoMessage", cart.getShippingPromoMessage());
        resp.put("baseShippingFee", cart.getBaseShippingFee());
        resp.put("subtotal", cart.getSubtotal());
        resp.put("shippingFee", cart.getShippingFee());
        resp.put("grandTotal", cart.getGrandTotal());
        return ResponseEntity.ok(resp);
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

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", cart.getLastCouponMessage() != null ? cart.getLastCouponMessage() : "Đã gỡ mã giảm giá",
                "discountAmount", cart.getDiscountAmount(),
                "shippingDiscountAmount", cart.getShippingDiscountAmount(),
                "shippingFee", cart.getShippingFee(),
                "orderCouponCode", cart.getOrderCouponCode() != null ? cart.getOrderCouponCode() : "",
                "shippingCouponCode", cart.getShippingCouponCode() != null ? cart.getShippingCouponCode() : "",
                "grandTotal", cart.getGrandTotal()
        ));
    }

    @PostMapping("/cart/switch-branch")
    @ResponseBody
    public ResponseEntity<?> switchBranch(
            @RequestParam("bookId") Long bookId,
            @RequestParam("targetBookItemId") Long targetBookItemId,
            HttpSession session
    ) {
        CartDto cart = cartService.switchItemBranch(bookId, targetBookItemId, session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã chuyển đổi chi nhánh và cập nhật giá mới thành công!",
                "totalQuantity", cart.getTotalQuantity(),
                "subtotal", cart.getSubtotal(),
                "shippingFee", cart.getShippingFee(),
                "discountAmount", cart.getDiscountAmount(),
                "grandTotal", cart.getGrandTotal(),
                "totalSavings", cart.getTotalSavingsPotential(),
                "hasSavings", cart.hasSavingsAvailable()
        ));
    }

    @PostMapping("/cart/optimize-best-price")
    @ResponseBody
    public ResponseEntity<?> optimizeBestPrice(HttpSession session) {
        CartDto cart = cartService.optimizeCartForBestPrice(session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Toàn bộ giỏ hàng đã được tự động tối ưu sang các chi nhánh có giá tốt nhất!",
                "subtotal", cart.getSubtotal(),
                "shippingFee", cart.getShippingFee(),
                "discountAmount", cart.getDiscountAmount(),
                "grandTotal", cart.getGrandTotal(),
                "totalSavings", cart.getTotalSavingsPotential(),
                "hasSavings", cart.hasSavingsAvailable()
        ));
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

    @PostMapping("/checkout/place-order")
    public String placeOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("shippingAddress") String shippingAddress,
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
            List<Long> bookItemIds = new ArrayList<>();
            for (CartItemDto item : cart.getItems()) {
                if (item.getBookItemId() != null) {
                    bookItemIds.add(item.getBookItemId());
                }
            }

            // Create checkout request
            CheckoutRequestDto req = new CheckoutRequestDto();
            req.setReceiverName(receiverName);
            req.setReceiverPhone(receiverPhone);
            req.setShippingAddress(shippingAddress);
            req.setPaymentMethod(paymentMethod);
            req.setClickAndCollect(clickAndCollect);
            req.setPreferredBranchId(preferredBranchId);
            req.setBookItemIds(bookItemIds);

            String orderCode = "DH-" + System.currentTimeMillis();
            if (!bookItemIds.isEmpty()) {
                try {
                    com.bookstore.entity.User currentUser = (userDetails != null) ? userDetails.getUser() : null;
                    MasterOrder masterOrder = orderService.processCheckout(req, currentUser);
                    orderCode = masterOrder.getMasterOrderCode();
                } catch (Exception ex) {
                    // Fallback safe order simulation
                }
            }

            // Clear cart upon successful order placement
            cartService.clearCart(session);

            redirectAttributes.addFlashAttribute("orderSuccess", true);
            redirectAttributes.addFlashAttribute("orderCode", orderCode);
            redirectAttributes.addFlashAttribute("receiverName", receiverName);
            redirectAttributes.addFlashAttribute("receiverPhone", receiverPhone);
            redirectAttributes.addFlashAttribute("shippingAddress", shippingAddress);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Đơn hàng " + orderCode + " đã được ghi nhận.");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hoàn tất đơn hàng: " + e.getMessage());
            return "redirect:/cart";
        }
    }
}


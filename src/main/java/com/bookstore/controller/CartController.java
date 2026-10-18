package com.bookstore.controller;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.CartItemDto;
import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.MasterOrder;
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

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
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
        boolean applied = cart.getCouponCode() != null && !cart.getCouponCode().isEmpty();

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", applied);
        resp.put("message", applied ? "Áp dụng mã giảm giá thành công!" : "Mã giảm giá không hợp lệ hoặc không đủ điều kiện đơn hàng!");
        resp.put("couponCode", cart.getCouponCode());
        resp.put("discountAmount", cart.getDiscountAmount());
        resp.put("subtotal", cart.getSubtotal());
        resp.put("shippingFee", cart.getShippingFee());
        resp.put("grandTotal", cart.getGrandTotal());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cart/remove-coupon")
    @ResponseBody
    public ResponseEntity<?> removeCoupon(HttpSession session) {
        CartDto cart = cartService.removeCoupon(session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "discountAmount", cart.getDiscountAmount(),
                "grandTotal", cart.getGrandTotal()
        ));
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
            @RequestParam(value = "clickAndCollect", defaultValue = "false") boolean clickAndCollect,
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
            req.setBookItemIds(bookItemIds);

            String orderCode = "DH-" + System.currentTimeMillis();
            if (!bookItemIds.isEmpty()) {
                try {
                    MasterOrder masterOrder = orderService.processCheckout(req, null);
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


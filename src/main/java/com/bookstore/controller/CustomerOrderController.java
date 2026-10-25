package com.bookstore.controller;

import com.bookstore.dto.OrderTrackingTimelineDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.entity.User;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.OrderTrackingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CustomerOrderController {

    private final OrderTrackingService orderTrackingService;

    public CustomerOrderController(OrderTrackingService orderTrackingService) {
        this.orderTrackingService = orderTrackingService;
    }

    /**
     * Danh sách đơn hàng cá nhân của khách hàng
     */
    @GetMapping({"/my-orders", "/orders"})
    public String myOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/my-orders";
        }
        Long userId = userDetails.getId();
        List<SubOrderDto> orders = orderTrackingService.getCustomerSubOrders(userId);
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Đơn Hàng Của Tôi - Book4Life");
        return "customer/orders";
    }

    /**
     * Xem chi tiết tiến độ đơn hàng theo timeline trực quan
     */
    @GetMapping("/orders/{id}/timeline")
    public String orderTimeline(@PathVariable("id") Long subOrderId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/orders/" + subOrderId + "/timeline";
        }

        try {
            boolean isStaffOrAdmin = userDetails.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().contains("ADMIN") ||
                    a.getAuthority().contains("BRANCH_MANAGER") ||
                    a.getAuthority().contains("STAFF") ||
                    a.getAuthority().contains("WAREHOUSE"));

            OrderTrackingTimelineDto tracking = orderTrackingService.getSubOrderTimeline(subOrderId, userDetails.getId(), isStaffOrAdmin);
            model.addAttribute("tracking", tracking);
            model.addAttribute("pageTitle", "Tiến Độ Đơn Hàng #" + tracking.getSubOrderCode());
            return "customer/order-timeline";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-orders";
        }
    }

    /**
     * Khách hàng hủy đơn khi đang ở trạng thái 'Chờ xác nhận' (PENDING)
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long subOrderId,
                              @RequestParam(name = "cancelReason", required = false) String cancelReason,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            orderTrackingService.cancelSubOrderByCustomer(subOrderId, cancelReason, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn: " + e.getMessage());
        }
        return "redirect:/orders/" + subOrderId + "/timeline";
    }

    /**
     * Khách hàng xác nhận đã nhận hàng thành công và gửi đánh giá / phản hồi
     */
    @PostMapping("/orders/{id}/confirm-received")
    public String confirmReceivedOrder(
            @PathVariable("id") Long subOrderId,
            @RequestParam(name = "feedbackNotes", required = false) String feedbackNotes,
            @RequestParam(name = "rating", required = false, defaultValue = "5") Integer rating,
            @RequestParam(name = "redirectToList", required = false, defaultValue = "false") boolean redirectToList,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            orderTrackingService.confirmDeliveryByCustomer(subOrderId, userDetails.getId(), feedbackNotes, rating);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn! Đơn hàng đã được xác nhận hoàn tất thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác nhận nhận hàng: " + e.getMessage());
        }

        if (redirectToList) {
            return "redirect:/my-orders";
        }
        return "redirect:/orders/" + subOrderId + "/timeline";
    }
}


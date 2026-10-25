package com.bookstore.controller.api;

import com.bookstore.dto.*;
import com.bookstore.entity.User;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.OrderTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class OrderTrackingApiController {

    private final OrderTrackingService orderTrackingService;

    public OrderTrackingApiController(OrderTrackingService orderTrackingService) {
        this.orderTrackingService = orderTrackingService;
    }

    /**
     * API 1: Xem chi tiết tiến độ timeline đơn hàng (Khách hàng & Nhân viên)
     */
    @GetMapping("/orders/{subOrderId}/timeline")
    public ResponseEntity<?> getTimeline(
            @PathVariable Long subOrderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails != null ? userDetails.getId() : null;
            boolean isStaffOrAdmin = userDetails != null && userDetails.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().contains("ADMIN") || a.getAuthority().contains("BRANCH_MANAGER") || a.getAuthority().contains("STAFF"));

            OrderTrackingTimelineDto timeline = orderTrackingService.getSubOrderTimeline(subOrderId, userId, isStaffOrAdmin);
            return ResponseEntity.ok(ApiResponse.success(timeline));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 2: Khách hàng hủy đơn khi PENDING
     */
    @PostMapping("/orders/{subOrderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long subOrderId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Vui lòng đăng nhập để hủy đơn!"));
        }
        try {
            String reason = body != null ? body.get("reason") : "Khách hàng yêu cầu hủy đơn";
            orderTrackingService.cancelSubOrderByCustomer(subOrderId, reason, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công và đã hoàn lại tồn kho sách!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 2b: Khách hàng xác nhận đã nhận hàng (SHIPPED -> COMPLETED) kèm đánh giá
     */
    @PostMapping("/orders/{subOrderId}/confirm-received")
    public ResponseEntity<?> confirmReceived(
            @PathVariable Long subOrderId,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Vui lòng đăng nhập để xác nhận đã nhận hàng!"));
        }
        try {
            String feedback = body != null && body.containsKey("feedback") ? String.valueOf(body.get("feedback")) : null;
            Integer rating = null;
            if (body != null && body.containsKey("rating") && body.get("rating") != null) {
                try {
                    rating = Integer.parseInt(String.valueOf(body.get("rating")));
                } catch (Exception ignored) {}
            }
            orderTrackingService.confirmDeliveryByCustomer(subOrderId, userDetails.getId(), feedback, rating);
            return ResponseEntity.ok(ApiResponse.success("Xác nhận đã nhận hàng thành công. Cảm ơn phản hồi của bạn!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 3: Quản lý chi nhánh lấy danh sách đơn & lọc
     */
    @GetMapping("/branch/{branchId}/orders")
    public ResponseEntity<?> getBranchOrders(
            @PathVariable Long branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            List<SubOrderDto> orders = orderTrackingService.getBranchSubOrders(branchId, status, keyword, fromDate, toDate);
            return ResponseEntity.ok(ApiResponse.success(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 4: Thống kê nhanh trong ngày của chi nhánh
     */
    @GetMapping("/branch/{branchId}/daily-stats")
    public ResponseEntity<?> getBranchStats(@PathVariable Long branchId) {
        try {
            FulfillmentStatsDto stats = orderTrackingService.getBranchDailyStats(branchId);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 5: Cập nhật tiến độ đơn hàng (Branch Manager / Staff)
     */
    @PutMapping("/orders/{subOrderId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long subOrderId,
            @RequestBody UpdateSubOrderStatusDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            SubOrderDto updated = orderTrackingService.updateStatusByBranchManager(
                    subOrderId, dto.getStatus(), dto.getNote(), dto.getTrackingNumber(), user);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 6: Điều phối đơn hàng sang chi nhánh khác (Super Admin)
     */
    @PostMapping("/orders/{subOrderId}/reassign")
    public ResponseEntity<?> reassignBranch(
            @PathVariable Long subOrderId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long newBranchId = Long.valueOf(body.get("newBranchId").toString());
            String reason = body.containsKey("reason") ? body.get("reason").toString() : null;
            User admin = userDetails != null ? userDetails.getUser() : null;
            SubOrderDto updated = orderTrackingService.reassignBranch(subOrderId, newBranchId, reason, admin);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 7: Lấy audit log lịch sử đơn hàng
     */
    @GetMapping("/orders/{subOrderId}/audit-logs")
    public ResponseEntity<?> getAuditLogs(@PathVariable Long subOrderId) {
        try {
            List<OrderStatusHistoryDto> logs = orderTrackingService.getSubOrderAuditLogs(subOrderId);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API 8: Dashboard tổng quan giám sát cho Super Admin
     */
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            AdminOrderDashboardDto dashboard = orderTrackingService.getAdminDashboard();
            return ResponseEntity.ok(ApiResponse.success(dashboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


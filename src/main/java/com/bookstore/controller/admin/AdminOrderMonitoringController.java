package com.bookstore.controller.admin;

import com.bookstore.dto.AdminOrderDashboardDto;
import com.bookstore.dto.OrderStatusHistoryDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.entity.Branch;
import com.bookstore.entity.User;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.BranchService;
import com.bookstore.service.OrderTrackingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderMonitoringController {

    private final OrderTrackingService orderTrackingService;
    private final BranchService branchService;

    public AdminOrderMonitoringController(OrderTrackingService orderTrackingService, BranchService branchService) {
        this.orderTrackingService = orderTrackingService;
        this.branchService = branchService;
    }

    /**
     * Dashboard Giám Sát Tiến Độ Đơn Hàng Toàn Chuỗi (Super Admin & Managers)
     */
    @GetMapping("/monitoring")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER')")
    public String orderMonitoringDashboard(
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        AdminOrderDashboardDto dashboard = orderTrackingService.getAdminDashboard();
        List<Branch> branches = branchService.getAllActiveBranches();

        // Nếu là Quản lý chi nhánh (không phải Admin), tự động gắn chi nhánh của họ nếu chưa chọn
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
        if (!isAdmin && userDetails != null && userDetails.getBranchId() != null && branchId == null) {
            branchId = userDetails.getBranchId();
        }

        List<SubOrderDto> orders = orderTrackingService.getAllSubOrdersForAdmin(branchId, status, keyword, fromDate, toDate);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("orders", orders);
        model.addAttribute("branches", branches);
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("activeMenu", "orderMonitoring");
        model.addAttribute("pageTitle", "Giám Sát Tiến Độ Đơn Hàng (Order Monitoring)");
        model.addAttribute("pageSubtitle", "Theo dõi tình trạng đơn hàng toàn chuỗi, phát hiện chi nhánh quá tải và điều phối linh hoạt");

        return "admin/orders/monitoring";
    }

    /**
     * Điều phối đơn hàng từ chi nhánh hiện tại sang chi nhánh khác (Dành cho Super Admin)
     */
    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public String reassignBranch(
            @PathVariable("id") Long subOrderId,
            @RequestParam("newBranchId") Long newBranchId,
            @RequestParam(name = "reason", required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User admin = userDetails != null ? userDetails.getUser() : null;
            SubOrderDto updated = orderTrackingService.reassignBranch(subOrderId, newBranchId, reason, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Điều phối thành công đơn hàng #" + updated.getSubOrderCode() + " sang chi nhánh mới!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể điều phối: " + e.getMessage());
        }
        return "redirect:/admin/orders/monitoring";
    }

    /**
     * Xem toàn bộ Nhật ký kiểm toán (Audit Log) trạng thái và điều phối
     */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER')")
    public String orderAuditLogs(
            @RequestParam(name = "subOrderId", required = false) Long subOrderId,
            Model model) {

        List<OrderStatusHistoryDto> logs;
        if (subOrderId != null) {
            logs = orderTrackingService.getSubOrderAuditLogs(subOrderId);
            model.addAttribute("filteredSubOrderId", subOrderId);
        } else {
            logs = orderTrackingService.getAllRecentAuditLogs();
        }

        model.addAttribute("logs", logs);
        model.addAttribute("activeMenu", "orderAuditLogs");
        model.addAttribute("pageTitle", "Nhật Ký Lịch Sử Đơn Hàng (Audit Trail)");
        model.addAttribute("pageSubtitle", "Theo dõi minh bạch toàn bộ thao tác chuyển trạng thái, ghi chú và lịch sử điều phối");

        return "admin/orders/audit-logs";
    }

    /**
     * Cập nhật trạng thái tiến độ đơn hàng nhanh từ trang quản trị
     */
    @PostMapping("/{id}/update-status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER', 'ROLE_WAREHOUSE_STAFF', 'WAREHOUSE_STAFF')")
    public String updateOrderStatus(
            @PathVariable("id") Long subOrderId,
            @RequestParam("status") String status,
            @RequestParam(name = "note", required = false) String note,
            @RequestParam(name = "trackingNumber", required = false) String trackingNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            orderTrackingService.updateStatusByBranchManager(subOrderId, status, note, trackingNumber, user);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/orders/monitoring";
    }
}


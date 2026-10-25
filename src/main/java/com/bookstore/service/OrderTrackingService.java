package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.User;

import java.util.List;

public interface OrderTrackingService {

    // ==========================================
    // 1. DÀNH CHO KHÁCH HÀNG (CUSTOMER)
    // ==========================================
    List<SubOrderDto> getCustomerSubOrders(Long userId);

    OrderTrackingTimelineDto getSubOrderTimeline(Long subOrderId, Long currentUserId, boolean isStaffOrAdmin);

    void cancelSubOrderByCustomer(Long subOrderId, String cancelReason, Long userId);

    void confirmDeliveryByCustomer(Long subOrderId, Long userId, String feedbackNotes, Integer rating);


    // ==========================================
    // 2. DÀNH CHO QUẢN LÝ CHI NHÁNH (BRANCH MANAGER)
    // ==========================================
    List<SubOrderDto> getBranchSubOrders(Long branchId, String status, String keyword, String fromDate, String toDate);

    FulfillmentStatsDto getBranchDailyStats(Long branchId);

    SubOrderDto updateStatusByBranchManager(Long subOrderId, String newStatus, String note, String trackingNumber, User manager);


    // ==========================================
    // 3. DÀNH CHO TỔNG QUẢN TRỊ (SUPER ADMIN)
    // ==========================================
    List<SubOrderDto> getAllSubOrdersForAdmin(Long branchId, String status, String keyword, String fromDate, String toDate);

    AdminOrderDashboardDto getAdminDashboard();

    SubOrderDto reassignBranch(Long subOrderId, Long newBranchId, String reason, User admin);

    List<OrderStatusHistoryDto> getSubOrderAuditLogs(Long subOrderId);

    List<OrderStatusHistoryDto> getAllRecentAuditLogs();
}


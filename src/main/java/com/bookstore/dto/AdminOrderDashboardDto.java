package com.bookstore.dto;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderDashboardDto {
    private long totalSubOrders;
    private long totalPending;
    private long totalProcessing;
    private long totalCompleted;
    private long totalCancelled;
    private double avgCompletionRate;
    private List<BranchLoadStatsDto> branchLoads = new ArrayList<>();
    private List<OrderStatusHistoryDto> recentAuditLogs = new ArrayList<>();

    public AdminOrderDashboardDto() {
    }

    public long getTotalSubOrders() {
        return totalSubOrders;
    }

    public void setTotalSubOrders(long totalSubOrders) {
        this.totalSubOrders = totalSubOrders;
    }

    public long getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(long totalPending) {
        this.totalPending = totalPending;
    }

    public long getTotalProcessing() {
        return totalProcessing;
    }

    public void setTotalProcessing(long totalProcessing) {
        this.totalProcessing = totalProcessing;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(long totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public long getTotalCancelled() {
        return totalCancelled;
    }

    public void setTotalCancelled(long totalCancelled) {
        this.totalCancelled = totalCancelled;
    }

    public double getAvgCompletionRate() {
        return avgCompletionRate;
    }

    public void setAvgCompletionRate(double avgCompletionRate) {
        this.avgCompletionRate = avgCompletionRate;
    }

    public List<BranchLoadStatsDto> getBranchLoads() {
        return branchLoads;
    }

    public void setBranchLoads(List<BranchLoadStatsDto> branchLoads) {
        this.branchLoads = branchLoads;
    }

    public List<OrderStatusHistoryDto> getRecentAuditLogs() {
        return recentAuditLogs;
    }

    public void setRecentAuditLogs(List<OrderStatusHistoryDto> recentAuditLogs) {
        this.recentAuditLogs = recentAuditLogs;
    }
}


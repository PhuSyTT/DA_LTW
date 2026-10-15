package com.bookstore.dto;

public class FulfillmentStatsDto {
    private Long branchId;
    private String branchName;
    private long pendingCount;
    private long packingCount;
    private long shippedCount;
    private long completedCount;
    private long cancelledCount;
    private long totalCount;

    public FulfillmentStatsDto() {
    }

    public FulfillmentStatsDto(Long branchId, String branchName, long pendingCount, long packingCount,
                               long shippedCount, long completedCount, long cancelledCount) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.pendingCount = pendingCount;
        this.packingCount = packingCount;
        this.shippedCount = shippedCount;
        this.completedCount = completedCount;
        this.cancelledCount = cancelledCount;
        this.totalCount = pendingCount + packingCount + shippedCount + completedCount + cancelledCount;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getPackingCount() {
        return packingCount;
    }

    public void setPackingCount(long packingCount) {
        this.packingCount = packingCount;
    }

    public long getShippedCount() {
        return shippedCount;
    }

    public void setShippedCount(long shippedCount) {
        this.shippedCount = shippedCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(long cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}

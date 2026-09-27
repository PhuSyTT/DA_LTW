package com.bookstore.dto;
public class TransferStatsDto {
    private long totalTransfers;
    private long completedTransfers;
    private double averageTransitTimeHours; // Thời gian vận chuyển trung bình (Giờ)
    private double lossRatePercentage;      // Tỷ lệ thất thoát (%)   

    public long getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(long totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public long getCompletedTransfers() {
        return completedTransfers;
    }

    public void setCompletedTransfers(long completedTransfers) {
        this.completedTransfers = completedTransfers;
    }

    public double getAverageTransitTimeHours() {
        return averageTransitTimeHours;
    }

    public void setAverageTransitTimeHours(double averageTransitTimeHours) {
        this.averageTransitTimeHours = averageTransitTimeHours;
    }

    public double getLossRatePercentage() {
        return lossRatePercentage;
    }

    public void setLossRatePercentage(double lossRatePercentage) {
        this.lossRatePercentage = lossRatePercentage;
    }
}
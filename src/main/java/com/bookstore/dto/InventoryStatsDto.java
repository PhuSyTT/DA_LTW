package com.bookstore.dto;

public class InventoryStatsDto {
    private long totalItems;
    private long totalAvailable;
    private long totalReserved;
    private long totalSold;

    public InventoryStatsDto() {
    }

    public InventoryStatsDto(long totalItems, long totalAvailable, long totalReserved, long totalSold) {
        this.totalItems = totalItems;
        this.totalAvailable = totalAvailable;
        this.totalReserved = totalReserved;
        this.totalSold = totalSold;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getTotalAvailable() {
        return totalAvailable;
    }

    public void setTotalAvailable(long totalAvailable) {
        this.totalAvailable = totalAvailable;
    }

    public long getTotalReserved() {
        return totalReserved;
    }

    public void setTotalReserved(long totalReserved) {
        this.totalReserved = totalReserved;
    }

    public long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(long totalSold) {
        this.totalSold = totalSold;
    }
}

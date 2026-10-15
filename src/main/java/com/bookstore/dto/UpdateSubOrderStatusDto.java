package com.bookstore.dto;

public class UpdateSubOrderStatusDto {
    private String status; // PACKING, SHIPPED, COMPLETED, CANCELLED
    private String trackingNumber;
    private String note;

    public UpdateSubOrderStatusDto() {
    }

    public UpdateSubOrderStatusDto(String status, String trackingNumber, String note) {
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

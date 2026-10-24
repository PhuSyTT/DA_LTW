package com.bookstore.dto;

public class UpdateSubOrderStatusDto {
    private String status; // PACKING, SHIPPED, COMPLETED, CANCELLED
    private String trackingNumber;
    private String note;

    // Driver & Shipper dispatch fields
    private Long driverId;
    private String shipperName;
    private String shipperPhone;
    private String shipperVehicle;
    private String shippingCarrier;
    private String deliveryStatus;
    private String estimatedDeliveryTime;
    private String deliveryNotes;

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

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public String getShipperVehicle() {
        return shipperVehicle;
    }

    public void setShipperVehicle(String shipperVehicle) {
        this.shipperVehicle = shipperVehicle;
    }

    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(String shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
}

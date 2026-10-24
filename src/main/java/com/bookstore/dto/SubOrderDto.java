package com.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubOrderDto {
    private Long id;
    private Long masterOrderId;
    private String masterOrderCode;
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String subOrderCode;
    private BigDecimal branchSubtotal;
    private BigDecimal branchShippingFee;
    private BigDecimal totalAmount;
    private String trackingNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private List<SubOrderItemDto> items = new ArrayList<>();

    // Driver & Shipper fields
    private Long driverId;
    private String shipperName;
    private String shipperPhone;
    private String shipperVehicle;
    private String shippingCarrier;
    private String deliveryStatus;
    private String estimatedDeliveryTime;
    private String deliveryNotes;

    public SubOrderDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMasterOrderId() {
        return masterOrderId;
    }

    public void setMasterOrderId(Long masterOrderId) {
        this.masterOrderId = masterOrderId;
    }

    public String getMasterOrderCode() {
        return masterOrderCode;
    }

    public void setMasterOrderCode(String masterOrderCode) {
        this.masterOrderCode = masterOrderCode;
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

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getSubOrderCode() {
        return subOrderCode;
    }

    public void setSubOrderCode(String subOrderCode) {
        this.subOrderCode = subOrderCode;
    }

    public BigDecimal getBranchSubtotal() {
        return branchSubtotal;
    }

    public void setBranchSubtotal(BigDecimal branchSubtotal) {
        this.branchSubtotal = branchSubtotal;
    }

    public BigDecimal getBranchShippingFee() {
        return branchShippingFee;
    }

    public void setBranchShippingFee(BigDecimal branchShippingFee) {
        this.branchShippingFee = branchShippingFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public List<SubOrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<SubOrderItemDto> items) {
        this.items = items;
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

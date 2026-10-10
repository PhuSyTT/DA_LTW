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
}

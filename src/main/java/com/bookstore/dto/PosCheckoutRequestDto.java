package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class PosCheckoutRequestDto {
    private Long branchId;
    private List<Long> bookItemIds;
    private String customerName;
    private String customerPhone;
    private String couponCode;
    private String paymentMethod; // CASH, BANK_TRANSFER
    private BigDecimal amountGiven;

    public PosCheckoutRequestDto() {}

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public List<Long> getBookItemIds() {
        return bookItemIds;
    }

    public void setBookItemIds(List<Long> bookItemIds) {
        this.bookItemIds = bookItemIds;
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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmountGiven() {
        return amountGiven;
    }

    public void setAmountGiven(BigDecimal amountGiven) {
        this.amountGiven = amountGiven;
    }
}


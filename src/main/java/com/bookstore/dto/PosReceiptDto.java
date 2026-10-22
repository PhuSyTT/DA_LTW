package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PosReceiptDto {
    private String orderCode;
    private String dateTime;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String cashierName;
    private String customerName;
    private String customerPhone;
    private List<PosReceiptItemDto> items = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String couponCode;
    private BigDecimal finalTotal = BigDecimal.ZERO;
    private String paymentMethod;
    private BigDecimal amountGiven = BigDecimal.ZERO;
    private BigDecimal changeAmount = BigDecimal.ZERO;

    public PosReceiptDto() {}

    public static class PosReceiptItemDto {
        private String bookTitle;
        private String skuBarcode;
        private String conditionGrade;
        private BigDecimal price;

        public PosReceiptItemDto() {}

        public PosReceiptItemDto(String bookTitle, String skuBarcode, String conditionGrade, BigDecimal price) {
            this.bookTitle = bookTitle;
            this.skuBarcode = skuBarcode;
            this.conditionGrade = conditionGrade;
            this.price = price;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public String getSkuBarcode() {
            return skuBarcode;
        }

        public void setSkuBarcode(String skuBarcode) {
            this.skuBarcode = skuBarcode;
        }

        public String getConditionGrade() {
            return conditionGrade;
        }

        public void setConditionGrade(String conditionGrade) {
            this.conditionGrade = conditionGrade;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
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

    public String getBranchPhone() {
        return branchPhone;
    }

    public void setBranchPhone(String branchPhone) {
        this.branchPhone = branchPhone;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
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

    public List<PosReceiptItemDto> getItems() {
        return items;
    }

    public void setItems(List<PosReceiptItemDto> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
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

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }
}


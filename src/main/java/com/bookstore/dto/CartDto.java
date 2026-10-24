package com.bookstore.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemDto> items = new ArrayList<>();
    
    // Voucher Giảm giá đơn hàng (Order Discount)
    private String orderCouponCode;
    private String orderCouponType; // PERCENT, FIXED_AMOUNT
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Voucher Vận chuyển (Shipping Voucher / Freeship)
    private String shippingCouponCode;
    private String shippingCouponType; // FREESHIP, SHIP_DISCOUNT
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal baseShippingFee = BigDecimal.ZERO;
    private BigDecimal shippingDiscountAmount = BigDecimal.ZERO;
    private String shippingPromoMessage;

    // Thông điệp phản hồi từ lần áp mã gần nhất
    private String lastCouponMessage;
    private Boolean lastCouponSuccess;

    public CartDto() {
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getCouponCode() {
        if (orderCouponCode != null && shippingCouponCode != null) {
            return orderCouponCode + " & " + shippingCouponCode;
        }
        return orderCouponCode != null ? orderCouponCode : shippingCouponCode;
    }

    public void setCouponCode(String couponCode) {
        this.orderCouponCode = couponCode;
    }

    public String getOrderCouponCode() {
        return orderCouponCode;
    }

    public void setOrderCouponCode(String orderCouponCode) {
        this.orderCouponCode = orderCouponCode;
    }

    public String getOrderCouponType() {
        return orderCouponType;
    }

    public void setOrderCouponType(String orderCouponType) {
        this.orderCouponType = orderCouponType;
    }

    public String getShippingCouponCode() {
        return shippingCouponCode;
    }

    public void setShippingCouponCode(String shippingCouponCode) {
        this.shippingCouponCode = shippingCouponCode;
    }

    public String getShippingCouponType() {
        return shippingCouponType;
    }

    public void setShippingCouponType(String shippingCouponType) {
        this.shippingCouponType = shippingCouponType;
    }

    public String getLastCouponMessage() {
        return lastCouponMessage;
    }

    public void setLastCouponMessage(String lastCouponMessage) {
        this.lastCouponMessage = lastCouponMessage;
    }

    public Boolean getLastCouponSuccess() {
        return lastCouponSuccess;
    }

    public void setLastCouponSuccess(Boolean lastCouponSuccess) {
        this.lastCouponSuccess = lastCouponSuccess;
    }

    public boolean hasOrderCoupon() {
        return orderCouponCode != null && !orderCouponCode.isBlank();
    }

    public boolean hasShippingCoupon() {
        return shippingCouponCode != null && !shippingCouponCode.isBlank();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public BigDecimal getShippingFee() {
        return shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        return items.stream().mapToInt(CartItemDto::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrandTotal() {
        BigDecimal subtotal = getSubtotal();
        BigDecimal discount = getDiscountAmount();
        BigDecimal shipping = getShippingFee();

        BigDecimal total = subtotal.subtract(discount).add(shipping);
        return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSavingsPotential() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItemDto::getPotentialSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean hasSavingsAvailable() {
        return getTotalSavingsPotential().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public String getCouponType() {
        if (orderCouponType != null && shippingCouponType != null) {
            return orderCouponType + " & " + shippingCouponType;
        }
        return orderCouponType != null ? orderCouponType : shippingCouponType;
    }

    public void setCouponType(String couponType) {
        if ("FREESHIP".equalsIgnoreCase(couponType) || "SHIP_DISCOUNT".equalsIgnoreCase(couponType)) {
            this.shippingCouponType = couponType;
        } else {
            this.orderCouponType = couponType;
        }
    }

    public BigDecimal getBaseShippingFee() {
        return baseShippingFee != null ? baseShippingFee : BigDecimal.ZERO;
    }

    public void setBaseShippingFee(BigDecimal baseShippingFee) {
        this.baseShippingFee = baseShippingFee != null ? baseShippingFee : BigDecimal.ZERO;
    }

    public BigDecimal getShippingDiscountAmount() {
        return shippingDiscountAmount != null ? shippingDiscountAmount : BigDecimal.ZERO;
    }

    public void setShippingDiscountAmount(BigDecimal shippingDiscountAmount) {
        this.shippingDiscountAmount = shippingDiscountAmount != null ? shippingDiscountAmount : BigDecimal.ZERO;
    }

    public String getShippingPromoMessage() {
        return shippingPromoMessage;
    }

    public void setShippingPromoMessage(String shippingPromoMessage) {
        this.shippingPromoMessage = shippingPromoMessage;
    }

    public boolean isFreeshipEligible() {
        return getSubtotal().compareTo(new BigDecimal("200000")) >= 0 || "FREESHIP".equalsIgnoreCase(shippingCouponType);
    }

    public boolean isDiscountShipEligible() {
        return getSubtotal().compareTo(new BigDecimal("100000")) >= 0 && getSubtotal().compareTo(new BigDecimal("200000")) < 0;
    }

    public BigDecimal getRemainingForFreeship() {
        BigDecimal target = new BigDecimal("200000");
        BigDecimal subtotal = getSubtotal();
        if (subtotal.compareTo(target) < 0) {
            return target.subtract(subtotal);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getRemainingForDiscountShip() {
        BigDecimal target = new BigDecimal("100000");
        BigDecimal subtotal = getSubtotal();
        if (subtotal.compareTo(target) < 0) {
            return target.subtract(subtotal);
        }
        return BigDecimal.ZERO;
    }

    public int getFreeshipProgressPercent() {
        BigDecimal subtotal = getSubtotal();
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) return 0;
        BigDecimal target = new BigDecimal("200000");
        if (subtotal.compareTo(target) >= 0) return 100;
        return subtotal.multiply(new BigDecimal(100)).divide(target, 0, java.math.RoundingMode.HALF_UP).intValue();
    }
}


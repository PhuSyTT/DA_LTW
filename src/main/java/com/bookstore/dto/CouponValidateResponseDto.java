package com.bookstore.dto;

import java.math.BigDecimal;

public class CouponValidateResponseDto {
    private boolean valid;
    private String message;
    private String code;
    private String couponCategory = "ORDER_DISCOUNT"; // SHIPPING hoặc ORDER_DISCOUNT
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String discountType = "FIXED_AMOUNT"; // PERCENT, FIXED_AMOUNT, FREESHIP, SHIP_DISCOUNT
    private BigDecimal shippingDiscountAmount = BigDecimal.ZERO;
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    public CouponValidateResponseDto(boolean valid, String message, BigDecimal discountAmount) {
        this.valid = valid;
        this.message = message;
        this.discountAmount = discountAmount;
    }

    public CouponValidateResponseDto(boolean valid, String message, BigDecimal discountAmount,
                                     String discountType, BigDecimal shippingDiscountAmount) {
        this.valid = valid;
        this.message = message;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.shippingDiscountAmount = shippingDiscountAmount;
    }

    public CouponValidateResponseDto(boolean valid, String message, String code, String couponCategory,
                                     BigDecimal discountAmount, String discountType, BigDecimal shippingDiscountAmount,
                                     BigDecimal minOrderAmount) {
        this.valid = valid;
        this.message = message;
        this.code = code;
        this.couponCategory = couponCategory;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.shippingDiscountAmount = shippingDiscountAmount;
        this.minOrderAmount = minOrderAmount;
    }

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCouponCategory() {
        return couponCategory;
    }

    public void setCouponCategory(String couponCategory) {
        this.couponCategory = couponCategory;
    }

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getShippingDiscountAmount() {
        return shippingDiscountAmount;
    }

    public void setShippingDiscountAmount(BigDecimal shippingDiscountAmount) {
        this.shippingDiscountAmount = shippingDiscountAmount;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public boolean isShippingCoupon() {
        return "SHIPPING".equalsIgnoreCase(couponCategory);
    }
}
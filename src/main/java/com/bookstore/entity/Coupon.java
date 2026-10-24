package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "coupon_category", length = 30)
    private String couponCategory = "ORDER_DISCOUNT"; // SHIPPING hoặc ORDER_DISCOUNT

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // PERCENT, FIXED_AMOUNT, FREESHIP, SHIP_DISCOUNT

    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "applicable_condition_grade", length = 30)
    private String applicableConditionGrade; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicable_branch_id")
    private Branch applicableBranch; // NULL = Toàn chuỗi

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCouponCategory() {
		if (couponCategory != null && !couponCategory.isBlank()) {
			return couponCategory;
		}
		if ("FREESHIP".equalsIgnoreCase(discountType) || "SHIP_DISCOUNT".equalsIgnoreCase(discountType)
				|| (code != null && (code.toUpperCase().contains("FREESHIP") || code.toUpperCase().startsWith("SHIP")))) {
			return "SHIPPING";
		}
		return "ORDER_DISCOUNT";
	}

	public void setCouponCategory(String couponCategory) {
		this.couponCategory = couponCategory;
	}

	public boolean isShippingCoupon() {
		return "SHIPPING".equalsIgnoreCase(getCouponCategory());
	}

	public boolean isOrderDiscountCoupon() {
		return "ORDER_DISCOUNT".equalsIgnoreCase(getCouponCategory());
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(BigDecimal discountValue) {
		this.discountValue = discountValue;
	}

	public BigDecimal getMinOrderAmount() {
		return minOrderAmount;
	}

	public void setMinOrderAmount(BigDecimal minOrderAmount) {
		this.minOrderAmount = minOrderAmount;
	}

	public BigDecimal getMaxDiscountAmount() {
		return maxDiscountAmount;
	}

	public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
		this.maxDiscountAmount = maxDiscountAmount;
	}

	public String getApplicableConditionGrade() {
		return applicableConditionGrade;
	}

	public void setApplicableConditionGrade(String applicableConditionGrade) {
		this.applicableConditionGrade = applicableConditionGrade;
	}

	public Branch getApplicableBranch() {
		return applicableBranch;
	}

	public void setApplicableBranch(Branch applicableBranch) {
		this.applicableBranch = applicableBranch;
	}

	public Integer getUsageLimit() {
		return usageLimit;
	}

	public void setUsageLimit(Integer usageLimit) {
		this.usageLimit = usageLimit;
	}

	public Integer getUsedCount() {
		return usedCount;
	}

	public void setUsedCount(Integer usedCount) {
		this.usedCount = usedCount;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
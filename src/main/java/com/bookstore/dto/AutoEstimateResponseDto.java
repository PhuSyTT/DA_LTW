package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.Map;

public class AutoEstimateResponseDto {
    private BigDecimal originalPrice;
    private BigDecimal baseRate;
    private BigDecimal totalPenalty;
    private BigDecimal estimatedPrice;
    private String userConditionGrade;
    private Map<String, BigDecimal> penaltyBreakdown;

    public AutoEstimateResponseDto() {
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }

    public BigDecimal getTotalPenalty() {
        return totalPenalty;
    }

    public void setTotalPenalty(BigDecimal totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getUserConditionGrade() {
        return userConditionGrade;
    }

    public void setUserConditionGrade(String userConditionGrade) {
        this.userConditionGrade = userConditionGrade;
    }

    public Map<String, BigDecimal> getPenaltyBreakdown() {
        return penaltyBreakdown;
    }

    public void setPenaltyBreakdown(Map<String, BigDecimal> penaltyBreakdown) {
        this.penaltyBreakdown = penaltyBreakdown;
    }
}

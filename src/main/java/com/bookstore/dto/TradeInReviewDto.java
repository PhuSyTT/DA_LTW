package com.bookstore.dto;

import java.math.BigDecimal;

public class TradeInReviewDto {
    private BigDecimal staffReviewedPrice;
    private String staffConditionGrade;
    private String staffNotes;
    private boolean acceptInitialEstimate; // true = accept auto_estimated_price, false = custom price

    public TradeInReviewDto() {
    }

    public BigDecimal getStaffReviewedPrice() {
        return staffReviewedPrice;
    }

    public void setStaffReviewedPrice(BigDecimal staffReviewedPrice) {
        this.staffReviewedPrice = staffReviewedPrice;
    }

    public String getStaffConditionGrade() {
        return staffConditionGrade;
    }

    public void setStaffConditionGrade(String staffConditionGrade) {
        this.staffConditionGrade = staffConditionGrade;
    }

    public String getStaffNotes() {
        return staffNotes;
    }

    public void setStaffNotes(String staffNotes) {
        this.staffNotes = staffNotes;
    }

    public boolean isAcceptInitialEstimate() {
        return acceptInitialEstimate;
    }

    public void setAcceptInitialEstimate(boolean acceptInitialEstimate) {
        this.acceptInitialEstimate = acceptInitialEstimate;
    }
}

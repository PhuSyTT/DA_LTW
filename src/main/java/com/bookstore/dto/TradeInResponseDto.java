package com.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TradeInResponseDto {
    private Long id;
    private String requestCode;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private BigDecimal bookOriginalPrice;
    private String bookCoverUrl;
    private Long targetBranchId;
    private String targetBranchName;
    private String userConditionGrade;
    private String surveyAnswersJson;
    private BigDecimal autoEstimatedPrice;
    private BigDecimal staffReviewedPrice;
    private String staffConditionGrade;
    private Long reviewedByStaffId;
    private String reviewedByStaffName;
    private String status;
    private String deliveryMethod;
    private String staffNotes;
    private LocalDateTime createdAt;
    private List<TradeInImageDto> images = new ArrayList<>();

    public TradeInResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public BigDecimal getBookOriginalPrice() {
        return bookOriginalPrice;
    }

    public void setBookOriginalPrice(BigDecimal bookOriginalPrice) {
        this.bookOriginalPrice = bookOriginalPrice;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    public Long getTargetBranchId() {
        return targetBranchId;
    }

    public void setTargetBranchId(Long targetBranchId) {
        this.targetBranchId = targetBranchId;
    }

    public String getTargetBranchName() {
        return targetBranchName;
    }

    public void setTargetBranchName(String targetBranchName) {
        this.targetBranchName = targetBranchName;
    }

    public String getUserConditionGrade() {
        return userConditionGrade;
    }

    public void setUserConditionGrade(String userConditionGrade) {
        this.userConditionGrade = userConditionGrade;
    }

    public String getSurveyAnswersJson() {
        return surveyAnswersJson;
    }

    public void setSurveyAnswersJson(String surveyAnswersJson) {
        this.surveyAnswersJson = surveyAnswersJson;
    }

    public BigDecimal getAutoEstimatedPrice() {
        return autoEstimatedPrice;
    }

    public void setAutoEstimatedPrice(BigDecimal autoEstimatedPrice) {
        this.autoEstimatedPrice = autoEstimatedPrice;
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

    public Long getReviewedByStaffId() {
        return reviewedByStaffId;
    }

    public void setReviewedByStaffId(Long reviewedByStaffId) {
        this.reviewedByStaffId = reviewedByStaffId;
    }

    public String getReviewedByStaffName() {
        return reviewedByStaffName;
    }

    public void setReviewedByStaffName(String reviewedByStaffName) {
        this.reviewedByStaffName = reviewedByStaffName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getStaffNotes() {
        return staffNotes;
    }

    public void setStaffNotes(String staffNotes) {
        this.staffNotes = staffNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TradeInImageDto> getImages() {
        return images;
    }

    public void setImages(List<TradeInImageDto> images) {
        this.images = images;
    }
}

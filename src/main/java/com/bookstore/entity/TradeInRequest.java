package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade_in_requests")
public class TradeInRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", nullable = false, unique = true, length = 50)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_branch_id", nullable = false)
    private Branch targetBranch;

    @Column(name = "user_condition_grade", nullable = false, length = 30)
    private String userConditionGrade; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE

    @Column(name = "survey_answers_json", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String surveyAnswersJson;

    @Column(name = "auto_estimated_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal autoEstimatedPrice;

    @Column(name = "staff_reviewed_price", precision = 12, scale = 2)
    private BigDecimal staffReviewedPrice;

    @Column(name = "staff_condition_grade", length = 30)
    private String staffConditionGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id")
    private User reviewedByStaff;

    @Column(nullable = false, length = 35)
    private String status = "PENDING_REVIEW"; // PENDING_REVIEW, OFFERED, ACCEPTED_AWAITING_DELIVERY, RECEIVED_STOCKED, REJECTED, CANCELLED

    @Column(name = "delivery_method", nullable = false, length = 30)
    private String deliveryMethod = "BRING_TO_STORE"; // BRING_TO_STORE, POSTAL_SHIPPING

    @Column(name = "staff_notes", length = 500)
    private String staffNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "tradeInRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TradeInImage> images = new ArrayList<>();

    public TradeInRequest() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "PENDING_REVIEW";
        }
        if (deliveryMethod == null || deliveryMethod.isBlank()) {
            deliveryMethod = "BRING_TO_STORE";
        }
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Branch getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(Branch targetBranch) {
        this.targetBranch = targetBranch;
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

    public User getReviewedByStaff() {
        return reviewedByStaff;
    }

    public void setReviewedByStaff(User reviewedByStaff) {
        this.reviewedByStaff = reviewedByStaff;
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

    public List<TradeInImage> getImages() {
        return images;
    }

    public void setImages(List<TradeInImage> images) {
        this.images = images;
    }

    public void addImage(TradeInImage image) {
        images.add(image);
        image.setTradeInRequest(this);
    }
}

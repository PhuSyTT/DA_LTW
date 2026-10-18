package com.bookstore.dto;

import java.time.LocalDateTime;

public class WishlistResponseDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String coverImageUrl;
    private Long preferredBranchId;
    private String preferredBranchName;
    private String minConditionGrade;
    private Boolean isNotified;
    private LocalDateTime createdAt;
    private LocalDateTime notifiedAt;

    public WishlistResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Long getPreferredBranchId() {
        return preferredBranchId;
    }

    public void setPreferredBranchId(Long preferredBranchId) {
        this.preferredBranchId = preferredBranchId;
    }

    public String getPreferredBranchName() {
        return preferredBranchName;
    }

    public void setPreferredBranchName(String preferredBranchName) {
        this.preferredBranchName = preferredBranchName;
    }

    public String getMinConditionGrade() {
        return minConditionGrade;
    }

    public void setMinConditionGrade(String minConditionGrade) {
        this.minConditionGrade = minConditionGrade;
    }

    public Boolean getIsNotified() {
        return isNotified;
    }

    public void setIsNotified(Boolean notified) {
        isNotified = notified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}

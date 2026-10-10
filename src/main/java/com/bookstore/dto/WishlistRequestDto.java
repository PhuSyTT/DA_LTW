package com.bookstore.dto;

public class WishlistRequestDto {
    private Long bookId;
    private Long preferredBranchId;
    private String minConditionGrade; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE

    public WishlistRequestDto() {
    }

    public WishlistRequestDto(Long bookId, Long preferredBranchId, String minConditionGrade) {
        this.bookId = bookId;
        this.preferredBranchId = preferredBranchId;
        this.minConditionGrade = minConditionGrade;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getPreferredBranchId() {
        return preferredBranchId;
    }

    public void setPreferredBranchId(Long preferredBranchId) {
        this.preferredBranchId = preferredBranchId;
    }

    public String getMinConditionGrade() {
        return minConditionGrade;
    }

    public void setMinConditionGrade(String minConditionGrade) {
        this.minConditionGrade = minConditionGrade;
    }
}

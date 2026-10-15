package com.bookstore.dto;

public class AutoEstimateRequestDto {
    private Long bookId;
    private String userConditionGrade; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE
    private boolean coverDamaged;
    private boolean spineDamaged;
    private boolean yellowPages;
    private boolean markingsPresent;
    private boolean missingAccessory;

    public AutoEstimateRequestDto() {
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getUserConditionGrade() {
        return userConditionGrade;
    }

    public void setUserConditionGrade(String userConditionGrade) {
        this.userConditionGrade = userConditionGrade;
    }

    public boolean isCoverDamaged() {
        return coverDamaged;
    }

    public void setCoverDamaged(boolean coverDamaged) {
        this.coverDamaged = coverDamaged;
    }

    public boolean isSpineDamaged() {
        return spineDamaged;
    }

    public void setSpineDamaged(boolean spineDamaged) {
        this.spineDamaged = spineDamaged;
    }

    public boolean isYellowPages() {
        return yellowPages;
    }

    public void setYellowPages(boolean yellowPages) {
        this.yellowPages = yellowPages;
    }

    public boolean isMarkingsPresent() {
        return markingsPresent;
    }

    public void setMarkingsPresent(boolean markingsPresent) {
        this.markingsPresent = markingsPresent;
    }

    public boolean isMissingAccessory() {
        return missingAccessory;
    }

    public void setMissingAccessory(boolean missingAccessory) {
        this.missingAccessory = missingAccessory;
    }
}

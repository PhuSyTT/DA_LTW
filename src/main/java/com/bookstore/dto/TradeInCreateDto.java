package com.bookstore.dto;

import java.util.ArrayList;
import java.util.List;

public class TradeInCreateDto {
    private Long bookId;
    private Long targetBranchId;
    private String userConditionGrade; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE
    private String deliveryMethod = "BRING_TO_STORE"; // BRING_TO_STORE, POSTAL_SHIPPING

    // Chi tiết khảo sát nghiệp vụ mục 3.2
    private boolean coverDamaged = false;     // Bìa rách nhẹ / nếp gãy: Khấu trừ 5%
    private boolean spineDamaged = false;     // Gáy sách lỏng / bung keo: Khấu trừ 10%
    private boolean yellowPages = false;      // Trang giấy bị ố vàng / ẩm nước: Khấu trừ 10%
    private boolean markingsPresent = false;  // Có viết vẽ / Highlight bút mực: Khấu trừ 10%
    private boolean missingAccessory = false; // Không có Bookmark / Bìa áo gốc: Khấu trừ 5%
    private String userNote;

    private List<TradeInImageDto> images = new ArrayList<>();

    public TradeInCreateDto() {
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getTargetBranchId() {
        return targetBranchId;
    }

    public void setTargetBranchId(Long targetBranchId) {
        this.targetBranchId = targetBranchId;
    }

    public String getUserConditionGrade() {
        return userConditionGrade;
    }

    public void setUserConditionGrade(String userConditionGrade) {
        this.userConditionGrade = userConditionGrade;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
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

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public List<TradeInImageDto> getImages() {
        return images;
    }

    public void setImages(List<TradeInImageDto> images) {
        this.images = images;
    }
}

package com.bookstore.dto;

import java.math.BigDecimal;

public class AutoEstimateRequestDto {
    private Long bookId;
    private BigDecimal customOriginalPrice;
    private String userConditionGrade = "LIKE_NEW"; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE

    // 10 Tiêu chí khảo sát khấu trừ thẩm định
    private boolean coverTorn = false;         // 1. Bìa ngoài bị rách mép, trầy xước hoặc gấp nếp rõ (-5%)
    private boolean coverPeeling = false;      // 2. Bìa sách bị mất màng bóng hoặc bong tróc mép (-5%)
    private boolean spineLoose = false;        // 3. Gáy sách bị lỏng lẻo, rách hoặc nứt keo chỉ (-10%)
    private boolean yellowPages = false;       // 4. Trang sách bị ố vàng, thâm kim hoặc ẩm nhẹ (-10%)
    private boolean markingsPresent = false;   // 5. Có vết mực viết tay, ghi chú, highlight bút dạ (-10%)
    private boolean dogEared = false;          // 6. Trang sách bị quăn mép, gập nếp góc ("chó tai") (-5%)
    private boolean tornPages = false;         // 7. Có trang bị rách nhẹ hoặc dán băng keo phục chế (-10%)
    private boolean missingAccessory = false;  // 8. Thiếu bìa áo, đai sách hoặc bookmark gốc (-5%)
    private boolean moldySmell = false;        // 9. Sách có mùi ẩm mốc cũ hoặc phai màu chữ (-5%)
    private String otherDefectDescription;     // 10. Tình trạng khác (Text box)

    // Tương thích ngược
    private boolean coverDamaged = false;
    private boolean spineDamaged = false;

    public AutoEstimateRequestDto() {
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public BigDecimal getCustomOriginalPrice() {
        return customOriginalPrice;
    }

    public void setCustomOriginalPrice(BigDecimal customOriginalPrice) {
        this.customOriginalPrice = customOriginalPrice;
    }

    public String getUserConditionGrade() {
        return userConditionGrade;
    }

    public void setUserConditionGrade(String userConditionGrade) {
        this.userConditionGrade = userConditionGrade;
    }

    public boolean isCoverTorn() {
        return coverTorn || coverDamaged;
    }

    public void setCoverTorn(boolean coverTorn) {
        this.coverTorn = coverTorn;
        this.coverDamaged = coverTorn;
    }

    public boolean isCoverPeeling() {
        return coverPeeling;
    }

    public void setCoverPeeling(boolean coverPeeling) {
        this.coverPeeling = coverPeeling;
    }

    public boolean isSpineLoose() {
        return spineLoose || spineDamaged;
    }

    public void setSpineLoose(boolean spineLoose) {
        this.spineLoose = spineLoose;
        this.spineDamaged = spineLoose;
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

    public boolean isDogEared() {
        return dogEared;
    }

    public void setDogEared(boolean dogEared) {
        this.dogEared = dogEared;
    }

    public boolean isTornPages() {
        return tornPages;
    }

    public void setTornPages(boolean tornPages) {
        this.tornPages = tornPages;
    }

    public boolean isMissingAccessory() {
        return missingAccessory;
    }

    public void setMissingAccessory(boolean missingAccessory) {
        this.missingAccessory = missingAccessory;
    }

    public boolean isMoldySmell() {
        return moldySmell;
    }

    public void setMoldySmell(boolean moldySmell) {
        this.moldySmell = moldySmell;
    }

    public String getOtherDefectDescription() {
        return otherDefectDescription;
    }

    public void setOtherDefectDescription(String otherDefectDescription) {
        this.otherDefectDescription = otherDefectDescription;
    }

    public boolean isCoverDamaged() {
        return isCoverTorn();
    }

    public void setCoverDamaged(boolean coverDamaged) {
        setCoverTorn(coverDamaged);
    }

    public boolean isSpineDamaged() {
        return isSpineLoose();
    }

    public void setSpineDamaged(boolean spineDamaged) {
        setSpineLoose(spineDamaged);
    }
}

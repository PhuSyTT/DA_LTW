package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TradeInCreateDto {
    private Long bookId;
    private Long targetBranchId;
    private String userConditionGrade = "LIKE_NEW"; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE
    private String deliveryMethod = "BRING_TO_STORE"; // Bán trực tiếp tại cửa hàng

    // Nhóm 1: Thông tin sách do khách hàng nhập
    private String bookTitle;
    private String categoryName;
    private String publisher;
    private Integer publicationYear;
    private BigDecimal estimatedOriginalPrice;

    // Nhóm 2: Bảng kiểm tra khấu trừ thẩm định (10 tiêu chí)
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
    private String otherDefectImageUrl;        // Ảnh thực tế của tình trạng khác

    // Tương thích ngược
    private boolean coverDamaged = false;
    private boolean spineDamaged = false;
    private String userNote;

    // Nhóm 3: Biên lai bán sách
    private String receiptCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private String expectedArrivalTime; // Thời gian dự kiến đến quầy

    // Danh sách ảnh 6 mặt + ảnh lỗi khác
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

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public BigDecimal getEstimatedOriginalPrice() {
        return estimatedOriginalPrice;
    }

    public void setEstimatedOriginalPrice(BigDecimal estimatedOriginalPrice) {
        this.estimatedOriginalPrice = estimatedOriginalPrice;
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

    public String getOtherDefectImageUrl() {
        return otherDefectImageUrl;
    }

    public void setOtherDefectImageUrl(String otherDefectImageUrl) {
        this.otherDefectImageUrl = otherDefectImageUrl;
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

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public List<TradeInImageDto> getImages() {
        return images;
    }

    public void setImages(List<TradeInImageDto> images) {
        this.images = images;
    }
}

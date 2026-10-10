package com.bookstore.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookItemDto {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private String bookCoverImageUrl;
    private BigDecimal bookOriginalPrice;
    private String authorName;
    private String categoryName;

    private Long branchId;
    private String branchName;
    private String branchCity;

    private String skuBarcode;
    private String conditionGrade;
    private BigDecimal sellingPrice;
    private Integer stockQuantity;
    private String shelfLocation;
    private String conditionNote;
    private LocalDateTime importDate;
    private String status;

    private List<BookItemImageDto> images = new ArrayList<>();
    private String primaryImageUrl;

    public BookItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBookCoverImageUrl() {
        return bookCoverImageUrl;
    }

    public void setBookCoverImageUrl(String bookCoverImageUrl) {
        this.bookCoverImageUrl = bookCoverImageUrl;
    }

    public BigDecimal getBookOriginalPrice() {
        return bookOriginalPrice;
    }

    public void setBookOriginalPrice(BigDecimal bookOriginalPrice) {
        this.bookOriginalPrice = bookOriginalPrice;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCity() {
        return branchCity;
    }

    public void setBranchCity(String branchCity) {
        this.branchCity = branchCity;
    }

    public String getSkuBarcode() {
        return skuBarcode;
    }

    public void setSkuBarcode(String skuBarcode) {
        this.skuBarcode = skuBarcode;
    }

    public String getConditionGrade() {
        return conditionGrade;
    }

    public void setConditionGrade(String conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getConditionNote() {
        return conditionNote;
    }

    public void setConditionNote(String conditionNote) {
        this.conditionNote = conditionNote;
    }

    public LocalDateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDateTime importDate) {
        this.importDate = importDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BookItemImageDto> getImages() {
        return images;
    }

    public void setImages(List<BookItemImageDto> images) {
        this.images = images;
    }

    public String getPrimaryImageUrl() {
        if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
            return primaryImageUrl;
        }
        if (images != null && !images.isEmpty()) {
            for (BookItemImageDto img : images) {
                if (Boolean.TRUE.equals(img.getIsPrimary())) {
                    return img.getImageUrl();
                }
            }
            return images.get(0).getImageUrl();
        }
        return bookCoverImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    // Helper methods for UI presentation
    public String getConditionGradeLabel() {
        if (conditionGrade == null) return "Chưa xác định";
        return switch (conditionGrade) {
            case "LIKE_NEW" -> "LIKE NEW (99%)";
            case "VERY_GOOD" -> "VERY GOOD (90%)";
            case "GOOD" -> "GOOD (80%)";
            case "ACCEPTABLE" -> "ACCEPTABLE (60%)";
            case "COLLECTIBLE" -> "COLLECTIBLE (Hiếm)";
            default -> conditionGrade;
        };
    }

    public String getConditionGradeBadgeClass() {
        if (conditionGrade == null) return "bg-secondary";
        return switch (conditionGrade) {
            case "LIKE_NEW" -> "badge-like-new";
            case "VERY_GOOD" -> "badge-very-good";
            case "GOOD" -> "badge-good";
            case "ACCEPTABLE" -> "badge-acceptable";
            case "COLLECTIBLE" -> "bg-dark text-warning border border-warning";
            default -> "bg-secondary";
        };
    }

    public String getStatusLabel() {
        if (status == null) return "Chưa xác định";
        return switch (status) {
            case "AVAILABLE" -> "Sẵn sàng bán";
            case "RESERVED" -> "Đang giữ chỗ";
            case "SOLD" -> "Đã bán";
            default -> status;
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) return "bg-secondary-subtle text-secondary";
        return switch (status) {
            case "AVAILABLE" -> "bg-success-subtle text-success border border-success-subtle";
            case "RESERVED" -> "bg-warning-subtle text-warning border border-warning-subtle";
            case "SOLD" -> "bg-secondary-subtle text-secondary border border-secondary-subtle";
            default -> "bg-light text-dark";
        };
    }

    public Integer getDiscountPercent() {
        if (bookOriginalPrice == null || bookOriginalPrice.compareTo(BigDecimal.ZERO) <= 0 || sellingPrice == null) {
            return 0;
        }
        BigDecimal diff = bookOriginalPrice.subtract(sellingPrice);
        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return diff.multiply(BigDecimal.valueOf(100))
                   .divide(bookOriginalPrice, 0, RoundingMode.HALF_UP)
                   .intValue();
    }
}

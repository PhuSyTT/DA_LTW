package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookDetailResponseDto {
    private Long id;
    private String title;
    private String isbn;
    private Long authorId;
    private String authorName;
    private String authorBio;
    private Long categoryId;
    private String categoryName;
    private String publisher;
    private Integer publishYear;
    private BigDecimal originalPrice;
    private String coverImageUrl;
    private String description;
    private Long totalInventory;
    private List<BranchStockDto> branchStocks = new ArrayList<>();

    public BookDetailResponseDto() {
    }

    public BookDetailResponseDto(Long id, String title, String isbn, Long authorId, String authorName,
                                 String authorBio, Long categoryId, String categoryName, String publisher,
                                 Integer publishYear, BigDecimal originalPrice, String coverImageUrl,
                                 String description, Long totalInventory, List<BranchStockDto> branchStocks) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorBio = authorBio;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.originalPrice = originalPrice;
        this.coverImageUrl = coverImageUrl;
        this.description = description;
        this.totalInventory = totalInventory;
        this.branchStocks = (branchStocks != null) ? branchStocks : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorBio() {
        return authorBio;
    }

    public void setAuthorBio(String authorBio) {
        this.authorBio = authorBio;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTotalInventory() {
        return totalInventory;
    }

    public void setTotalInventory(Long totalInventory) {
        this.totalInventory = totalInventory;
    }

    public List<BranchStockDto> getBranchStocks() {
        return branchStocks;
    }

    public void setBranchStocks(List<BranchStockDto> branchStocks) {
        this.branchStocks = branchStocks;
    }

    public static class BranchStockDto {
        private Long branchId;
        private String branchName;
        private String skuBarcode;
        private String conditionGrade;
        private BigDecimal sellingPrice;
        private Integer stockQuantity;
        private String shelfLocation;
        private String status;

        public BranchStockDto() {
        }

        public BranchStockDto(Long branchId, String branchName, String skuBarcode, String conditionGrade,
                              BigDecimal sellingPrice, Integer stockQuantity, String shelfLocation, String status) {
            this.branchId = branchId;
            this.branchName = branchName;
            this.skuBarcode = skuBarcode;
            this.conditionGrade = conditionGrade;
            this.sellingPrice = sellingPrice;
            this.stockQuantity = stockQuantity;
            this.shelfLocation = shelfLocation;
            this.status = status;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long branchId;
            private String branchName;
            private String skuBarcode;
            private String conditionGrade;
            private BigDecimal sellingPrice;
            private Integer stockQuantity;
            private String shelfLocation;
            private String status;

            public Builder branchId(Long branchId) {
                this.branchId = branchId;
                return this;
            }

            public Builder branchName(String branchName) {
                this.branchName = branchName;
                return this;
            }

            public Builder skuBarcode(String skuBarcode) {
                this.skuBarcode = skuBarcode;
                return this;
            }

            public Builder conditionGrade(String conditionGrade) {
                this.conditionGrade = conditionGrade;
                return this;
            }

            public Builder sellingPrice(BigDecimal sellingPrice) {
                this.sellingPrice = sellingPrice;
                return this;
            }

            public Builder stockQuantity(Integer stockQuantity) {
                this.stockQuantity = stockQuantity;
                return this;
            }

            public Builder shelfLocation(String shelfLocation) {
                this.shelfLocation = shelfLocation;
                return this;
            }

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public BranchStockDto build() {
                return new BranchStockDto(branchId, branchName, skuBarcode, conditionGrade, sellingPrice, stockQuantity, shelfLocation, status);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String isbn;
        private Long authorId;
        private String authorName;
        private String authorBio;
        private Long categoryId;
        private String categoryName;
        private String publisher;
        private Integer publishYear;
        private BigDecimal originalPrice;
        private String coverImageUrl;
        private String description;
        private Long totalInventory;
        private List<BranchStockDto> branchStocks = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder authorId(Long authorId) {
            this.authorId = authorId;
            return this;
        }

        public Builder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public Builder authorBio(String authorBio) {
            this.authorBio = authorBio;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder publishYear(Integer publishYear) {
            this.publishYear = publishYear;
            return this;
        }

        public Builder originalPrice(BigDecimal originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }

        public Builder coverImageUrl(String coverImageUrl) {
            this.coverImageUrl = coverImageUrl;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder totalInventory(Long totalInventory) {
            this.totalInventory = totalInventory;
            return this;
        }

        public Builder branchStocks(List<BranchStockDto> branchStocks) {
            this.branchStocks = branchStocks;
            return this;
        }

        public BookDetailResponseDto build() {
            return new BookDetailResponseDto(id, title, isbn, authorId, authorName, authorBio, categoryId, categoryName, publisher, publishYear, originalPrice, coverImageUrl, description, totalInventory, branchStocks);
        }
    }
}

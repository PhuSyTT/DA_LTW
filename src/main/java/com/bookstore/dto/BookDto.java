package com.bookstore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class BookDto {

    private Long id;

    @NotBlank(message = "Tên đầu sách không được để trống")
    @Size(max = 255, message = "Tên sách tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Mã ISBN không được để trống")
    @Pattern(regexp = "^[0-9X-]{10,17}$", message = "Mã ISBN phải từ 10-17 ký tự hợp lệ (chữ số và dấu gạch nối)")
    private String isbn;

    @NotNull(message = "Vui lòng chọn tác giả chuẩn")
    private Long authorId;

    private String authorName;

    @NotNull(message = "Vui lòng chọn thể loại sách")
    private Long categoryId;

    private String categoryName;

    @Size(max = 150, message = "Nhà xuất bản tối đa 150 ký tự")
    private String publisher;

    @Min(value = 1800, message = "Năm xuất bản từ năm 1800 trở đi")
    @Max(value = 2100, message = "Năm xuất bản không hợp lệ")
    private Integer publishYear;

    @NotNull(message = "Giá bìa gốc không được để trống")
    @DecimalMin(value = "1000.00", message = "Giá bìa tối thiểu là 1.000 VNĐ")
    @DecimalMax(value = "100000000.00", message = "Giá bìa tối đa là 100.000.000 VNĐ")
    private BigDecimal originalPrice;

    private String coverImageUrl;

    private String description;

    private Long inventoryCount;

    public BookDto() {
    }

    public BookDto(Long id, String title, String isbn, Long authorId, String authorName,
                   Long categoryId, String categoryName, String publisher, Integer publishYear,
                   BigDecimal originalPrice, String coverImageUrl, String description, Long inventoryCount) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.originalPrice = originalPrice;
        this.coverImageUrl = coverImageUrl;
        this.description = description;
        this.inventoryCount = inventoryCount;
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

    public Long getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(Long inventoryCount) {
        this.inventoryCount = inventoryCount;
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
        private Long categoryId;
        private String categoryName;
        private String publisher;
        private Integer publishYear;
        private BigDecimal originalPrice;
        private String coverImageUrl;
        private String description;
        private Long inventoryCount;

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

        public Builder inventoryCount(Long inventoryCount) {
            this.inventoryCount = inventoryCount;
            return this;
        }

        public BookDto build() {
            return new BookDto(id, title, isbn, authorId, authorName, categoryId, categoryName,
                    publisher, publishYear, originalPrice, coverImageUrl, description, inventoryCount);
        }
    }
}

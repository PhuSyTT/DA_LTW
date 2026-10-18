package com.bookstore.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long bookId;
    private Long bookItemId;
    private String title;
    private String isbn;
    private String authorName;
    private String categoryName;
    private String coverImageUrl;
    private String conditionGrade;
    private String branchName;
    private BigDecimal price;
    private int quantity;

    public CartItemDto() {
    }

    public CartItemDto(Long bookId, Long bookItemId, String title, String isbn,
                       String authorName, String categoryName, String coverImageUrl,
                       String conditionGrade, String branchName, BigDecimal price, int quantity) {
        this.bookId = bookId;
        this.bookItemId = bookItemId;
        this.title = title;
        this.isbn = isbn;
        this.authorName = authorName;
        this.categoryName = categoryName;
        this.coverImageUrl = coverImageUrl;
        this.conditionGrade = conditionGrade;
        this.branchName = branchName;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.quantity = Math.max(1, quantity);
    }

    public BigDecimal getSubtotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getBookItemId() {
        return bookItemId;
    }

    public void setBookItemId(Long bookItemId) {
        this.bookItemId = bookItemId;
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getConditionGrade() {
        return conditionGrade;
    }

    public void setConditionGrade(String conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }
}


package com.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_items")
public class BookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "sku_barcode", nullable = false, unique = true, length = 50)
    private String skuBarcode;

    @Column(name = "condition_grade", nullable = false, length = 30)
    private String conditionGrade; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE, COLLECTIBLE

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 1;

    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    @Column(name = "condition_note", length = 500)
    private String conditionNote;

    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, RESERVED, SOLD

    @OneToMany(mappedBy = "bookItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<BookItemImage> images = new java.util.ArrayList<>();

    public BookItem() {
    }

    public BookItem(Long id, Book book, Branch branch, String skuBarcode, String conditionGrade,
                    BigDecimal sellingPrice, Integer stockQuantity, String shelfLocation,
                    String conditionNote, LocalDateTime importDate, String status) {
        this.id = id;
        this.book = book;
        this.branch = branch;
        this.skuBarcode = skuBarcode;
        this.conditionGrade = conditionGrade;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = (stockQuantity != null) ? stockQuantity : 1;
        this.shelfLocation = shelfLocation;
        this.conditionNote = conditionNote;
        this.importDate = (importDate != null) ? importDate : LocalDateTime.now();
        this.status = (status != null) ? status : "AVAILABLE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
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

    public java.util.List<BookItemImage> getImages() {
        return images;
    }

    public void setImages(java.util.List<BookItemImage> images) {
        this.images = images;
    }

    public void addImage(BookItemImage image) {
        if (this.images == null) {
            this.images = new java.util.ArrayList<>();
        }
        this.images.add(image);
        image.setBookItem(this);
    }

    public void removeImage(BookItemImage image) {
        if (this.images != null) {
            this.images.remove(image);
            image.setBookItem(null);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Book book;
        private Branch branch;
        private String skuBarcode;
        private String conditionGrade;
        private BigDecimal sellingPrice;
        private Integer stockQuantity = 1;
        private String shelfLocation;
        private String conditionNote;
        private LocalDateTime importDate = LocalDateTime.now();
        private String status = "AVAILABLE";

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder book(Book book) {
            this.book = book;
            return this;
        }

        public Builder branch(Branch branch) {
            this.branch = branch;
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

        public Builder conditionNote(String conditionNote) {
            this.conditionNote = conditionNote;
            return this;
        }

        public Builder importDate(LocalDateTime importDate) {
            this.importDate = importDate;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public BookItem build() {
            return new BookItem(id, book, branch, skuBarcode, conditionGrade, sellingPrice, stockQuantity, shelfLocation, conditionNote, importDate, status);
        }
    }
}

package com.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 150)
    private String publisher;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookItem> bookItems = new ArrayList<>();

    public Book() {
    }

    public Book(Long id, String title, String isbn, Author author, Category category,
                String publisher, Integer publishYear, BigDecimal originalPrice,
                String coverImageUrl, String description) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.originalPrice = originalPrice;
        this.coverImageUrl = coverImageUrl;
        this.description = description;
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public List<BookItem> getBookItems() {
        return bookItems;
    }

    public void setBookItems(List<BookItem> bookItems) {
        this.bookItems = bookItems;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String isbn;
        private Author author;
        private Category category;
        private String publisher;
        private Integer publishYear;
        private BigDecimal originalPrice;
        private String coverImageUrl;
        private String description;
        private List<BookItem> bookItems = new ArrayList<>();

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

        public Builder author(Author author) {
            this.author = author;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
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

        public Builder bookItems(List<BookItem> bookItems) {
            this.bookItems = bookItems;
            return this;
        }

        public Book build() {
            Book book = new Book(id, title, isbn, author, category, publisher, publishYear, originalPrice, coverImageUrl, description);
            book.setBookItems(bookItems);
            return book;
        }
    }
}

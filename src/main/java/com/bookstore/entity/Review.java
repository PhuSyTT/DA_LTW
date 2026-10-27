package com.bookstore.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_book_id", columnList = "book_id"),
    @Index(name = "idx_reviews_user_id", columnList = "user_id")
})
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "sub_order_code", length = 64)
    private String subOrderCode;

    @Column(nullable = false)
    private Integer rating = 5;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {
        this.createdAt = LocalDateTime.now();
        this.rating = 5;
        this.isVerifiedPurchase = true;
    }

    public Review(Book book, User user, Long orderId, String subOrderCode, Integer rating, String title, String comment, Boolean isVerifiedPurchase) {
        this.book = book;
        this.user = user;
        this.orderId = orderId;
        this.subOrderCode = subOrderCode;
        this.rating = (rating != null && rating >= 1 && rating <= 5) ? rating : 5;
        this.title = title;
        this.comment = comment;
        this.isVerifiedPurchase = (isVerifiedPurchase != null) ? isVerifiedPurchase : true;
        this.createdAt = LocalDateTime.now();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSubOrderCode() {
        return subOrderCode;
    }

    public void setSubOrderCode(String subOrderCode) {
        this.subOrderCode = subOrderCode;
    }

    public Integer getRating() {
        return rating != null ? rating : 5;
    }

    public void setRating(Integer rating) {
        this.rating = (rating != null && rating >= 1 && rating <= 5) ? rating : 5;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getIsVerifiedPurchase() {
        return isVerifiedPurchase != null ? isVerifiedPurchase : true;
    }

    public void setIsVerifiedPurchase(Boolean isVerifiedPurchase) {
        this.isVerifiedPurchase = isVerifiedPurchase;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

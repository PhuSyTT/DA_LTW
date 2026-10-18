package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists")
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_branch_id")
    private Branch preferredBranch;

    @Column(name = "min_condition_grade", nullable = false, length = 30)
    private String minConditionGrade = "GOOD"; // LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE

    @Column(name = "is_notified", nullable = false)
    private Boolean isNotified = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    public Wishlist() {
    }

    public Wishlist(User user, Book book, Branch preferredBranch, String minConditionGrade) {
        this.user = user;
        this.book = book;
        this.preferredBranch = preferredBranch;
        this.minConditionGrade = (minConditionGrade != null && !minConditionGrade.isBlank()) ? minConditionGrade : "GOOD";
        this.isNotified = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isNotified == null) {
            isNotified = false;
        }
        if (minConditionGrade == null || minConditionGrade.isBlank()) {
            minConditionGrade = "GOOD";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Branch getPreferredBranch() {
        return preferredBranch;
    }

    public void setPreferredBranch(Branch preferredBranch) {
        this.preferredBranch = preferredBranch;
    }

    public String getMinConditionGrade() {
        return minConditionGrade;
    }

    public void setMinConditionGrade(String minConditionGrade) {
        this.minConditionGrade = minConditionGrade;
    }

    public Boolean getIsNotified() {
        return isNotified;
    }

    public void setIsNotified(Boolean notified) {
        isNotified = notified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}

package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_item_images")
public class BookItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_item_id", nullable = false)
    private BookItem bookItem;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    public BookItemImage() {
    }

    public BookItemImage(Long id, BookItem bookItem, String imageUrl, String cloudinaryPublicId, Boolean isPrimary) {
        this.id = id;
        this.bookItem = bookItem;
        this.imageUrl = imageUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.isPrimary = (isPrimary != null) ? isPrimary : false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookItem getBookItem() {
        return bookItem;
    }

    public void setBookItem(BookItem bookItem) {
        this.bookItem = bookItem;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private BookItem bookItem;
        private String imageUrl;
        private String cloudinaryPublicId;
        private Boolean isPrimary = false;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder bookItem(BookItem bookItem) {
            this.bookItem = bookItem;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder cloudinaryPublicId(String cloudinaryPublicId) {
            this.cloudinaryPublicId = cloudinaryPublicId;
            return this;
        }

        public Builder isPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public BookItemImage build() {
            return new BookItemImage(id, bookItem, imageUrl, cloudinaryPublicId, isPrimary);
        }
    }
}

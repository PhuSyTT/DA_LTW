package com.bookstore.dto;

public class BookItemImageDto {
    private Long id;
    private Long bookItemId;
    private String imageUrl;
    private String cloudinaryPublicId;
    private Boolean isPrimary;

    public BookItemImageDto() {
    }

    public BookItemImageDto(Long id, Long bookItemId, String imageUrl, String cloudinaryPublicId, Boolean isPrimary) {
        this.id = id;
        this.bookItemId = bookItemId;
        this.imageUrl = imageUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.isPrimary = isPrimary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookItemId() {
        return bookItemId;
    }

    public void setBookItemId(Long bookItemId) {
        this.bookItemId = bookItemId;
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
}

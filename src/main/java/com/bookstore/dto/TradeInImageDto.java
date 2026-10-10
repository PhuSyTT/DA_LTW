package com.bookstore.dto;

public class TradeInImageDto {
    private String imageUrl;
    private String cloudinaryPublicId;
    private String imageType; // COVER, SPINE, DEFECT_PAGE

    public TradeInImageDto() {
    }

    public TradeInImageDto(String imageUrl, String cloudinaryPublicId, String imageType) {
        this.imageUrl = imageUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.imageType = imageType;
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

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}

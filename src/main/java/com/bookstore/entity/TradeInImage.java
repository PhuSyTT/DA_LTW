package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trade_in_images")
public class TradeInImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_in_request_id", nullable = false)
    private TradeInRequest tradeInRequest;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    @Column(name = "image_type", nullable = false, length = 30)
    private String imageType; // COVER, SPINE, DEFECT_PAGE

    public TradeInImage() {
    }

    public TradeInImage(TradeInRequest tradeInRequest, String imageUrl, String cloudinaryPublicId, String imageType) {
        this.tradeInRequest = tradeInRequest;
        this.imageUrl = imageUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.imageType = imageType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TradeInRequest getTradeInRequest() {
        return tradeInRequest;
    }

    public void setTradeInRequest(TradeInRequest tradeInRequest) {
        this.tradeInRequest = tradeInRequest;
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

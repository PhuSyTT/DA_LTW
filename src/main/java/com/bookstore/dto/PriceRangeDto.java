package com.bookstore.dto;

import java.math.BigDecimal;

public class PriceRangeDto {
    private BigDecimal originalPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal suggestedPrice;
    private int rateMinPercent;
    private int rateMaxPercent;
    private int suggestedPercent;

    public PriceRangeDto() {
    }

    public PriceRangeDto(BigDecimal originalPrice, BigDecimal minPrice, BigDecimal maxPrice,
                         BigDecimal suggestedPrice, int rateMinPercent, int rateMaxPercent, int suggestedPercent) {
        this.originalPrice = originalPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.suggestedPrice = suggestedPrice;
        this.rateMinPercent = rateMinPercent;
        this.rateMaxPercent = rateMaxPercent;
        this.suggestedPercent = suggestedPercent;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getSuggestedPrice() {
        return suggestedPrice;
    }

    public void setSuggestedPrice(BigDecimal suggestedPrice) {
        this.suggestedPrice = suggestedPrice;
    }

    public int getRateMinPercent() {
        return rateMinPercent;
    }

    public void setRateMinPercent(int rateMinPercent) {
        this.rateMinPercent = rateMinPercent;
    }

    public int getRateMaxPercent() {
        return rateMaxPercent;
    }

    public void setRateMaxPercent(int rateMaxPercent) {
        this.rateMaxPercent = rateMaxPercent;
    }

    public int getSuggestedPercent() {
        return suggestedPercent;
    }

    public void setSuggestedPercent(int suggestedPercent) {
        this.suggestedPercent = suggestedPercent;
    }
}

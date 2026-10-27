package com.bookstore.dto;

import java.util.ArrayList;
import java.util.List;

public class ReviewSummaryDto {
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String coverImageUrl;

    private double averageRating = 5.0;
    private long totalReviews = 0;

    private long star5Count = 0;
    private long star4Count = 0;
    private long star3Count = 0;
    private long star2Count = 0;
    private long star1Count = 0;

    private int star5Percent = 0;
    private int star4Percent = 0;
    private int star3Percent = 0;
    private int star2Percent = 0;
    private int star1Percent = 0;

    private List<ReviewDto> reviews = new ArrayList<>();

    public ReviewSummaryDto() {
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public long getStar5Count() {
        return star5Count;
    }

    public void setStar5Count(long star5Count) {
        this.star5Count = star5Count;
    }

    public long getStar4Count() {
        return star4Count;
    }

    public void setStar4Count(long star4Count) {
        this.star4Count = star4Count;
    }

    public long getStar3Count() {
        return star3Count;
    }

    public void setStar3Count(long star3Count) {
        this.star3Count = star3Count;
    }

    public long getStar2Count() {
        return star2Count;
    }

    public void setStar2Count(long star2Count) {
        this.star2Count = star2Count;
    }

    public long getStar1Count() {
        return star1Count;
    }

    public void setStar1Count(long star1Count) {
        this.star1Count = star1Count;
    }

    public int getStar5Percent() {
        return star5Percent;
    }

    public void setStar5Percent(int star5Percent) {
        this.star5Percent = star5Percent;
    }

    public int getStar4Percent() {
        return star4Percent;
    }

    public void setStar4Percent(int star4Percent) {
        this.star4Percent = star4Percent;
    }

    public int getStar3Percent() {
        return star3Percent;
    }

    public void setStar3Percent(int star3Percent) {
        this.star3Percent = star3Percent;
    }

    public int getStar2Percent() {
        return star2Percent;
    }

    public void setStar2Percent(int star2Percent) {
        this.star2Percent = star2Percent;
    }

    public int getStar1Percent() {
        return star1Percent;
    }

    public void setStar1Percent(int star1Percent) {
        this.star1Percent = star1Percent;
    }

    public List<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDto> reviews) {
        this.reviews = reviews;
    }
}

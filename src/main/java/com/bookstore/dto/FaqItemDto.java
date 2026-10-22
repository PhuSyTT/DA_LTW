package com.bookstore.dto;

public class FaqItemDto {

    private String id;
    private String icon;
    private String question;
    private String shortTitle;
    private String category;
    private String answer;

    public FaqItemDto() {
    }

    public FaqItemDto(String id, String icon, String question, String shortTitle, String category, String answer) {
        this.id = id;
        this.icon = icon;
        this.question = question;
        this.shortTitle = shortTitle;
        this.category = category;
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}


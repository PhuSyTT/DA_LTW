package com.bookstore.dto;

import java.time.LocalDateTime;

public class ChatResponseDto {
    private String reply;
    private String sender = "ASSISTANT";
    private LocalDateTime createdAt = LocalDateTime.now();
    private String status = "SUCCESS";

    public ChatResponseDto() {
    }

    public ChatResponseDto(String reply) {
        this.reply = reply;
        this.createdAt = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public ChatResponseDto(String reply, String status) {
        this.reply = reply;
        this.createdAt = LocalDateTime.now();
        this.status = status;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


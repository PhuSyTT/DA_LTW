package com.bookstore.dto;

import java.time.LocalDateTime;

public class OrderTimelineStepDto {
    private int stepIndex;
    private String statusCode;
    private String title;
    private String description;
    private String icon;
    private LocalDateTime timestamp;
    private String formattedTime;
    private boolean completed;
    private boolean current;
    private boolean cancelled;

    public OrderTimelineStepDto() {
    }

    public OrderTimelineStepDto(int stepIndex, String statusCode, String title, String description,
                                String icon, LocalDateTime timestamp, String formattedTime,
                                boolean completed, boolean current, boolean cancelled) {
        this.stepIndex = stepIndex;
        this.statusCode = statusCode;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.timestamp = timestamp;
        this.formattedTime = formattedTime;
        this.completed = completed;
        this.current = current;
        this.cancelled = cancelled;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}


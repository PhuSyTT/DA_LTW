package com.bookstore.dto;

import java.time.LocalDateTime;

public class OrderStatusHistoryDto {
    private Long id;
    private Long subOrderId;
    private String subOrderCode;
    private Long masterOrderId;
    private String masterOrderCode;
    private String fromStatus;
    private String toStatus;
    private String toStatusLabel;
    private String note;
    private Long updatedByUserId;
    private String updatedByUserName;
    private String updatedByUserRole;
    private Long fromBranchId;
    private String fromBranchName;
    private Long toBranchId;
    private String toBranchName;
    private String actionType; // STATUS_CHANGE, BRANCH_REASSIGN, ORDER_CANCELLED
    private String actionTypeLabel;
    private LocalDateTime createdAt;

    public OrderStatusHistoryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubOrderId() {
        return subOrderId;
    }

    public void setSubOrderId(Long subOrderId) {
        this.subOrderId = subOrderId;
    }

    public String getSubOrderCode() {
        return subOrderCode;
    }

    public void setSubOrderCode(String subOrderCode) {
        this.subOrderCode = subOrderCode;
    }

    public Long getMasterOrderId() {
        return masterOrderId;
    }

    public void setMasterOrderId(Long masterOrderId) {
        this.masterOrderId = masterOrderId;
    }

    public String getMasterOrderCode() {
        return masterOrderCode;
    }

    public void setMasterOrderCode(String masterOrderCode) {
        this.masterOrderCode = masterOrderCode;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getToStatusLabel() {
        return toStatusLabel;
    }

    public void setToStatusLabel(String toStatusLabel) {
        this.toStatusLabel = toStatusLabel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    public String getUpdatedByUserName() {
        return updatedByUserName;
    }

    public void setUpdatedByUserName(String updatedByUserName) {
        this.updatedByUserName = updatedByUserName;
    }

    public String getUpdatedByUserRole() {
        return updatedByUserRole;
    }

    public void setUpdatedByUserRole(String updatedByUserRole) {
        this.updatedByUserRole = updatedByUserRole;
    }

    public Long getFromBranchId() {
        return fromBranchId;
    }

    public void setFromBranchId(Long fromBranchId) {
        this.fromBranchId = fromBranchId;
    }

    public String getFromBranchName() {
        return fromBranchName;
    }

    public void setFromBranchName(String fromBranchName) {
        this.fromBranchName = fromBranchName;
    }

    public Long getToBranchId() {
        return toBranchId;
    }

    public void setToBranchId(Long toBranchId) {
        this.toBranchId = toBranchId;
    }

    public String getToBranchName() {
        return toBranchName;
    }

    public void setToBranchName(String toBranchName) {
        this.toBranchName = toBranchName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionTypeLabel() {
        return actionTypeLabel;
    }

    public void setActionTypeLabel(String actionTypeLabel) {
        this.actionTypeLabel = actionTypeLabel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


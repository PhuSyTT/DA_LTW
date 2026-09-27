package com.bookstore.dto;

import java.util.List;

public class TransferRequestDto {
    private Long fromBranchId;
    private Long toBranchId;
    private String reason;
    private List<TransferItemDto> items;

    public Long getFromBranchId() {
        return fromBranchId;
    }

    public void setFromBranchId(Long fromBranchId) {
        this.fromBranchId = fromBranchId;
    }

    public Long getToBranchId() {
        return toBranchId;
    }

    public void setToBranchId(Long toBranchId) {
        this.toBranchId = toBranchId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<TransferItemDto> getItems() {
        return items;
    }

    public void setItems(List<TransferItemDto> items) {
        this.items = items;
    }

    // Class con lưu chi tiết từng cuốn sách luân chuyển
    public static class TransferItemDto {
        private Long bookItemId;
        private Integer quantity;

        public Long getBookItemId() {
            return bookItemId;
        }

        public void setBookItemId(Long bookItemId) {
            this.bookItemId = bookItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
package com.bookstore.dto;

public class TradeInCustomerResponseDto {
    private boolean accepted; // true = đồng ý bán, false = từ chối bán
    private String deliveryMethod; // BRING_TO_STORE, POSTAL_SHIPPING

    public TradeInCustomerResponseDto() {
    }

    public TradeInCustomerResponseDto(boolean accepted, String deliveryMethod) {
        this.accepted = accepted;
        this.deliveryMethod = deliveryMethod;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }
}

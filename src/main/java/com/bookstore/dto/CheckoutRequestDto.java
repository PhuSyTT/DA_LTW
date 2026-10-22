package com.bookstore.dto;

import java.util.List;

public class CheckoutRequestDto {
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String paymentMethod;
    private boolean clickAndCollect; // Khách chọn giữ sách tại quầy (FR-ORD-04)
    private Long preferredBranchId; // Chi nhánh khách chọn nhận sách hoặc ưu tiên xử lý
    private List<Long> bookItemIds; // Danh sách ID sách cũ khách muốn mua
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public String getReceiverPhone() {
		return receiverPhone;
	}
	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}
	public String getShippingAddress() {
		return shippingAddress;
	}
	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public boolean isClickAndCollect() {
		return clickAndCollect;
	}
	public void setClickAndCollect(boolean clickAndCollect) {
		this.clickAndCollect = clickAndCollect;
	}
	public Long getPreferredBranchId() {
		return preferredBranchId;
	}
	public void setPreferredBranchId(Long preferredBranchId) {
		this.preferredBranchId = preferredBranchId;
	}
	public List<Long> getBookItemIds() {
		return bookItemIds;
	}
	public void setBookItemIds(List<Long> bookItemIds) {
		this.bookItemIds = bookItemIds;
	}

}
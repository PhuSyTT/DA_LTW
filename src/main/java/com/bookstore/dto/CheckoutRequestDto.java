package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CheckoutRequestDto {
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private String orderNote;
    private String paymentMethod;
    private boolean clickAndCollect; // Khách chọn giữ sách tại quầy (FR-ORD-04)
    private Long preferredBranchId; // Chi nhánh khách chọn nhận sách hoặc ưu tiên xử lý
    private List<Long> bookItemIds; // Danh sách ID sách cũ khách muốn mua
    private Map<Long, Integer> itemQuantities; // Số lượng mua theo từng bookItemId
    private BigDecimal totalItemsAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal shippingDiscountAmount;
    private BigDecimal finalAmount;
    private String couponCode;
    private String shippingCouponCode;

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
	public Map<Long, Integer> getItemQuantities() {
		return itemQuantities;
	}
	public void setItemQuantities(Map<Long, Integer> itemQuantities) {
		this.itemQuantities = itemQuantities;
	}
	public BigDecimal getTotalItemsAmount() {
		return totalItemsAmount;
	}
	public void setTotalItemsAmount(BigDecimal totalItemsAmount) {
		this.totalItemsAmount = totalItemsAmount;
	}
	public BigDecimal getShippingFee() {
		return shippingFee;
	}
	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}
	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}
	public BigDecimal getShippingDiscountAmount() {
		return shippingDiscountAmount;
	}
	public void setShippingDiscountAmount(BigDecimal shippingDiscountAmount) {
		this.shippingDiscountAmount = shippingDiscountAmount;
	}
	public BigDecimal getFinalAmount() {
		return finalAmount;
	}
	public void setFinalAmount(BigDecimal finalAmount) {
		this.finalAmount = finalAmount;
	}
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	public String getShippingCouponCode() {
		return shippingCouponCode;
	}
	public void setShippingCouponCode(String shippingCouponCode) {
		this.shippingCouponCode = shippingCouponCode;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getWard() {
		return ward;
	}
	public void setWard(String ward) {
		this.ward = ward;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	public String getOrderNote() {
		return orderNote;
	}
	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}
}
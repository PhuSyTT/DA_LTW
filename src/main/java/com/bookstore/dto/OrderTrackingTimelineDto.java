package com.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderTrackingTimelineDto {
    private Long subOrderId;
    private String subOrderCode;
    private Long masterOrderId;
    private String masterOrderCode;

    // Trạng thái hiện tại & badge
    private String currentStatus;
    private String statusLabel;
    private String statusBadgeClass;

    // Chi nhánh chịu trách nhiệm
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String branchPhone;

    // Thông tin giao hàng & khách hàng
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String trackingNumber;
    private String paymentMethod;
    private String paymentStatus;

    // Thông tin tài xế & giao vận
    private Long driverId;
    private String shipperName;
    private String shipperPhone;
    private String shipperVehicle;
    private String shippingCarrier;
    private String deliveryStatus;
    private LocalDateTime estimatedDeliveryTime;
    private String deliveryNotes;

    // Giá tiền
    private BigDecimal branchSubtotal = BigDecimal.ZERO;
    private BigDecimal branchShippingFee = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Mốc thời gian
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    // Điều kiện thao tác
    private boolean canCancel;
    private boolean canConfirmDelivery;

    // Danh sách mặt hàng sách trong đơn
    private List<SubOrderItemDto> items = new ArrayList<>();

    // Các bước tiến trình timeline trực quan
    private List<OrderTimelineStepDto> timelineSteps = new ArrayList<>();

    // Toàn bộ nhật ký lịch sử thay đổi trạng thái
    private List<OrderStatusHistoryDto> historyLogs = new ArrayList<>();

    public OrderTrackingTimelineDto() {
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

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

    public void setStatusBadgeClass(String statusBadgeClass) {
        this.statusBadgeClass = statusBadgeClass;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getBranchPhone() {
        return branchPhone;
    }

    public void setBranchPhone(String branchPhone) {
        this.branchPhone = branchPhone;
    }

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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getBranchSubtotal() {
        return branchSubtotal;
    }

    public void setBranchSubtotal(BigDecimal branchSubtotal) {
        this.branchSubtotal = branchSubtotal;
    }

    public BigDecimal getBranchShippingFee() {
        return branchShippingFee;
    }

    public void setBranchShippingFee(BigDecimal branchShippingFee) {
        this.branchShippingFee = branchShippingFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public boolean isCanConfirmDelivery() {
        return canConfirmDelivery;
    }

    public void setCanConfirmDelivery(boolean canConfirmDelivery) {
        this.canConfirmDelivery = canConfirmDelivery;
    }

    public List<SubOrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<SubOrderItemDto> items) {
        this.items = items;
    }

    public List<OrderTimelineStepDto> getTimelineSteps() {
        return timelineSteps;
    }

    public void setTimelineSteps(List<OrderTimelineStepDto> timelineSteps) {
        this.timelineSteps = timelineSteps;
    }

    public List<OrderStatusHistoryDto> getHistoryLogs() {
        return historyLogs;
    }

    public void setHistoryLogs(List<OrderStatusHistoryDto> historyLogs) {
        this.historyLogs = historyLogs;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public String getShipperVehicle() {
        return shipperVehicle;
    }

    public void setShipperVehicle(String shipperVehicle) {
        this.shipperVehicle = shipperVehicle;
    }

    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(String shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
}


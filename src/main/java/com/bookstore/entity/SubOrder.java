package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_orders")
public class SubOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String subOrderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_order_id")
    private MasterOrder masterOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    private BigDecimal branchSubtotal;
    private BigDecimal branchShippingFee;
    private String trackingNumber;
    private String status = "PENDING"; // PENDING, CONFIRMED, PACKING, SHIPPED, COMPLETED, CANCELLED

    // Driver & Delivery Management
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "shipper_name", length = 100)
    private String shipperName;

    @Column(name = "shipper_phone", length = 20)
    private String shipperPhone;

    @Column(name = "shipper_vehicle", length = 50)
    private String shipperVehicle;

    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;

    @Column(name = "delivery_status", length = 30)
    private String deliveryStatus; // WAITING_SHIPPER, PICKED_UP, DELIVERING, DELIVERED, FAILED

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "delivery_notes", length = 500)
    private String deliveryNotes;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subOrder", cascade = CascadeType.ALL)
    private List<SubOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "subOrder", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubOrderCode() {
        return subOrderCode;
    }

    public void setSubOrderCode(String subOrderCode) {
        this.subOrderCode = subOrderCode;
    }

    public MasterOrder getMasterOrder() {
        return masterOrder;
    }

    public void setMasterOrder(MasterOrder masterOrder) {
        this.masterOrder = masterOrder;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    public List<SubOrderItem> getItems() {
        return items;
    }

    public void setItems(List<SubOrderItem> items) {
        this.items = items;
    }

    public List<OrderStatusHistory> getStatusHistories() {
        return statusHistories;
    }

    public void setStatusHistories(List<OrderStatusHistory> statusHistories) {
        this.statusHistories = statusHistories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
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
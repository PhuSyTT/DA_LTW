package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "sub_orders")
public class SubOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String subOrderCode;

    @ManyToOne
    @JoinColumn(name = "master_order_id")
    private MasterOrder masterOrder;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    private BigDecimal branchSubtotal;
    private BigDecimal branchShippingFee;
    private String trackingNumber;
    private String status = "PENDING";

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subOrder", cascade = CascadeType.ALL)
    private List<SubOrderItem> items;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
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

	public List<SubOrderItem> getItems() {
		return items;
	}

	public void setItems(List<SubOrderItem> items) {
		this.items = items;
	}

	public java.time.LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(java.time.LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public java.time.LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
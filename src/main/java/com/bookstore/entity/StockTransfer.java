package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stock_transfers")
public class StockTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transferCode;

    @ManyToOne
    @JoinColumn(name = "from_branch_id")
    private Branch fromBranch;

    @ManyToOne
    @JoinColumn(name = "to_branch_id")
    private Branch toBranch;

    @ManyToOne
    @JoinColumn(name = "requested_by_user_id")
    private User requestedByUser;

    private String status; // PENDING_APPROVAL, IN_TRANSIT, COMPLETED, REJECTED
    
    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL)
    private List<StockTransferItem> items;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTransferCode() {
		return transferCode;
	}

	public void setTransferCode(String transferCode) {
		this.transferCode = transferCode;
	}

	public Branch getFromBranch() {
		return fromBranch;
	}

	public void setFromBranch(Branch fromBranch) {
		this.fromBranch = fromBranch;
	}

	public Branch getToBranch() {
		return toBranch;
	}

	public void setToBranch(Branch toBranch) {
		this.toBranch = toBranch;
	}

	public User getRequestedByUser() {
		return requestedByUser;
	}

	public void setRequestedByUser(User requestedByUser) {
		this.requestedByUser = requestedByUser;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public List<StockTransferItem> getItems() {
		return items;
	}

	public void setItems(List<StockTransferItem> items) {
		this.items = items;
	}

}
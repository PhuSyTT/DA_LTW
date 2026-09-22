package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;


@Entity
@Table(name = "master_orders")
public class MasterOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String masterOrderCode;

    // Quan hệ với User (đã có trong entity)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal totalItemsAmount;
    private String paymentMethod;
    private String paymentStatus;

    // Cascade để khi lưu MasterOrder sẽ lưu luôn các SubOrder
    @OneToMany(mappedBy = "masterOrder", cascade = CascadeType.ALL)
    private List<SubOrder> subOrders;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMasterOrderCode() {
		return masterOrderCode;
	}

	public void setMasterOrderCode(String masterOrderCode) {
		this.masterOrderCode = masterOrderCode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getTotalItemsAmount() {
		return totalItemsAmount;
	}

	public void setTotalItemsAmount(BigDecimal totalItemsAmount) {
		this.totalItemsAmount = totalItemsAmount;
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

	public List<SubOrder> getSubOrders() {
		return subOrders;
	}

	public void setSubOrders(List<SubOrder> subOrders) {
		this.subOrders = subOrders;
	}

    // Getters, Setters...
}
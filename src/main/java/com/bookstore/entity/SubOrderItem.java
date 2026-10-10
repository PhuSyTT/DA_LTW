package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sub_order_items")
public class SubOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sub_order_id")
    private SubOrder subOrder;

    @ManyToOne
    @JoinColumn(name = "book_item_id")
    private BookItem bookItem;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public SubOrder getSubOrder() {
		return subOrder;
	}
	public void setSubOrder(SubOrder subOrder) {
		this.subOrder = subOrder;
	}
	public BookItem getBookItem() {
		return bookItem;
	}
	public void setBookItem(BookItem bookItem) {
		this.bookItem = bookItem;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}
	public BigDecimal getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}
}
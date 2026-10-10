package com.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> staffMembers = new ArrayList<>();

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookItem> inventoryItems = new ArrayList<>();

    public Branch() {
    }

    public Branch(Long id, String branchName, String address, String phone, String city,
                  BigDecimal latitude, BigDecimal longitude, Boolean isActive) {
        this.id = id;
        this.branchName = branchName;
        this.address = address;
        this.phone = phone;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = (isActive != null) ? isActive : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public List<User> getStaffMembers() {
        return staffMembers;
    }

    public void setStaffMembers(List<User> staffMembers) {
        this.staffMembers = staffMembers;
    }

    public List<BookItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<BookItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String branchName;
        private String address;
        private String phone;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Boolean isActive = true;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder branchName(String branchName) {
            this.branchName = branchName;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder latitude(BigDecimal latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(BigDecimal longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Branch build() {
            return new Branch(id, branchName, address, phone, city, latitude, longitude, isActive);
        }
    }
}

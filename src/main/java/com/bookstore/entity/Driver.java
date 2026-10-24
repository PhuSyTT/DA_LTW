package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "license_plate", length = 30)
    private String licensePlate;

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType = "MOTORBIKE"; // MOTORBIKE, TRUCK, VAN

    @Column(name = "carrier_name", length = 100)
    private String carrierName = "Đội xe Book4Life"; // Đội xe Book4Life, GHTK, GHN, AhaMove, GrabExpress

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, BUSY, OFFLINE

    @Column(name = "active_orders_count")
    private Integer activeOrdersCount = 0;

    @Column(name = "rating")
    private Double rating = 5.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Driver() {
    }

    public Driver(Long id, String fullName, String phone, String licensePlate,
                  String vehicleType, String carrierName, Branch branch, String status) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.carrierName = carrierName != null ? carrierName : "Đội xe Book4Life";
        this.branch = branch;
        this.status = status != null ? status : "AVAILABLE";
        this.activeOrdersCount = 0;
        this.rating = 5.0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getActiveOrdersCount() {
        return activeOrdersCount;
    }

    public void setActiveOrdersCount(Integer activeOrdersCount) {
        this.activeOrdersCount = activeOrdersCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


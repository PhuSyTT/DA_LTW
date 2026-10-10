package com.bookstore.repository;

import com.bookstore.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
}
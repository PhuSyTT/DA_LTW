package com.bookstore.repository;

import com.bookstore.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    List<Coupon> findAllByOrderByIdDesc();
    List<Coupon> findByApplicableBranchIdOrApplicableBranchIsNullOrderByIdDesc(Long branchId);
}

package com.bookstore.repository;

import com.bookstore.entity.MasterOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterOrderRepository extends JpaRepository<MasterOrder, Long> {

    Optional<MasterOrder> findByMasterOrderCode(String masterOrderCode);

    // Lịch sử đơn hàng của 1 khách hàng, mới nhất lên trước
    @Query("SELECT mo FROM MasterOrder mo WHERE mo.user.id = :userId ORDER BY mo.id DESC")
    List<MasterOrder> findByUserIdOrderByIdDesc(@Param("userId") Long userId);
}
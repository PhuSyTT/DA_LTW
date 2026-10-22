package com.bookstore.repository;

import com.bookstore.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    // Lịch sử trạng thái của 1 sub-order theo thứ tự thời gian (tăng dần để vẽ timeline)
    List<OrderStatusHistory> findBySubOrderIdOrderByCreatedAtAsc(Long subOrderId);

    // Lịch sử trạng thái của 1 sub-order mới nhất trước (audit log)
    List<OrderStatusHistory> findBySubOrderIdOrderByCreatedAtDesc(Long subOrderId);

    // Toàn bộ lịch sử liên quan đến 1 đơn tổng MasterOrder
    List<OrderStatusHistory> findByMasterOrderIdOrderByCreatedAtDesc(Long masterOrderId);

    // Toàn bộ audit log cho Admin với phân trang hoặc top mới nhất
    @Query("SELECT h FROM OrderStatusHistory h ORDER BY h.createdAt DESC")
    List<OrderStatusHistory> findTop100ByOrderByCreatedAtDesc();

    // Lịch sử thao tác theo branch (cả khi branch là nguồn hoặc đích)
    @Query("SELECT h FROM OrderStatusHistory h WHERE h.fromBranch.id = :branchId OR h.toBranch.id = :branchId ORDER BY h.createdAt DESC")
    List<OrderStatusHistory> findByBranchIdOrderByCreatedAtDesc(@Param("branchId") Long branchId);
}


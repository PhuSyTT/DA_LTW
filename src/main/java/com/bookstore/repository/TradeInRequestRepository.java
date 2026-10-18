package com.bookstore.repository;

import com.bookstore.entity.TradeInRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeInRequestRepository extends JpaRepository<TradeInRequest, Long> {

    Optional<TradeInRequest> findByRequestCode(String requestCode);

    List<TradeInRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<TradeInRequest> findByTargetBranchIdOrderByCreatedAtDesc(Long targetBranchId);

    List<TradeInRequest> findByTargetBranchIdAndStatusOrderByCreatedAtDesc(Long targetBranchId, String status);

    List<TradeInRequest> findByStatusOrderByCreatedAtDesc(String status);

    long countByTargetBranchIdAndStatus(Long targetBranchId, String status);
}

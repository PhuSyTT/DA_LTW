package com.bookstore.repository;

import com.bookstore.entity.SubOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder, Long> {

    // Tìm đơn hàng con theo mã đơn (Ví dụ: SUB-1-171...)
    Optional<SubOrder> findBySubOrderCode(String subOrderCode);

    // Lấy danh sách đơn hàng con được phân bổ về một Chi nhánh cụ thể (mới nhất lên trước)
    List<SubOrder> findByBranchIdOrderByIdDesc(Long branchId);

    // Lấy danh sách các đơn hàng con thuộc về một Đơn hàng tổng (Master Order)
    List<SubOrder> findByMasterOrderId(Long masterOrderId);
}
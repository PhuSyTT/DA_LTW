package com.bookstore.repository;

import com.bookstore.entity.SubOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // Lấy danh sách đơn hàng con theo chi nhánh và trạng thái
    List<SubOrder> findByBranchIdAndStatusOrderByIdDesc(Long branchId, String status);

    // Đếm số lượng đơn hàng theo trạng thái tại chi nhánh
    long countByBranchIdAndStatus(Long branchId, String status);

    // Tìm theo ID và Chi nhánh
    Optional<SubOrder> findByIdAndBranchId(Long id, Long branchId);

    // Lấy tất cả đơn hàng con của 1 khách hàng
    @Query("SELECT s FROM SubOrder s WHERE s.masterOrder.user.id = :userId ORDER BY s.id DESC")
    List<SubOrder> findByUserIdOrderByIdDesc(@Param("userId") Long userId);

    // Thống kê nhanh theo ngày tại chi nhánh: đếm số đơn theo status tạo từ sau mốc startDate
    @Query("SELECT COUNT(s) FROM SubOrder s WHERE s.branch.id = :branchId AND s.status = :status AND s.createdAt >= :startDate")
    long countByBranchIdAndStatusAndCreatedAtAfter(@Param("branchId") Long branchId,
                                                  @Param("status") String status,
                                                  @Param("startDate") LocalDateTime startDate);

    // Thống kê đơn đang xử lý tại chi nhánh (PENDING, CONFIRMED, PACKING, SHIPPED)
    @Query("SELECT COUNT(s) FROM SubOrder s WHERE s.branch.id = :branchId AND s.status IN ('PENDING', 'CONFIRMED', 'PACKING', 'SHIPPED')")
    long countProcessingByBranchId(@Param("branchId") Long branchId);

    // Tìm kiếm / lọc đơn hàng tại chi nhánh theo từ khóa (mã đơn, tên người nhận, SĐT) và trạng thái
    @Query("SELECT s FROM SubOrder s " +
           "LEFT JOIN s.masterOrder mo " +
           "WHERE s.branch.id = :branchId " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(s.subOrderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR (mo IS NOT NULL AND LOWER(mo.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "     OR (mo IS NOT NULL AND mo.receiverPhone LIKE CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.id DESC")
    List<SubOrder> filterBranchOrders(@Param("branchId") Long branchId,
                                      @Param("status") String status,
                                      @Param("keyword") String keyword);

    // Admin lọc toàn bộ đơn hàng của tất cả chi nhánh
    @Query("SELECT s FROM SubOrder s " +
           "LEFT JOIN s.branch br " +
           "LEFT JOIN s.masterOrder mo " +
           "WHERE (:branchId IS NULL OR br.id = :branchId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(s.subOrderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR (mo IS NOT NULL AND LOWER(mo.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "     OR (mo IS NOT NULL AND mo.receiverPhone LIKE CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.id DESC")
    List<SubOrder> filterAllOrders(@Param("branchId") Long branchId,
                                   @Param("status") String status,
                                   @Param("keyword") String keyword);

    @Query("SELECT COALESCE(SUM(s.branchSubtotal), 0) FROM SubOrder s WHERE (:branchId IS NULL OR s.branch.id = :branchId) AND s.status IN ('COMPLETED', 'DELIVERED', 'SHIPPED')")
    java.math.BigDecimal sumCompletedRevenue(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(s) FROM SubOrder s WHERE (:branchId IS NULL OR s.branch.id = :branchId) AND s.status IN ('COMPLETED', 'DELIVERED', 'SHIPPED')")
    long countCompletedOrders(@Param("branchId") Long branchId);
}
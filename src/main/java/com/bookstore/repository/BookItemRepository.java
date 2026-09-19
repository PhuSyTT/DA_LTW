package com.bookstore.repository;

import com.bookstore.entity.BookItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookItemRepository extends JpaRepository<BookItem, Long> {

    List<BookItem> findByBookId(Long bookId);

    List<BookItem> findByBranchId(Long branchId);

    List<BookItem> findByBookIdAndBranchId(Long bookId, Long branchId);

    @Query("SELECT bi FROM BookItem bi " +
           "JOIN FETCH bi.branch br " +
           "WHERE bi.book.id = :bookId AND bi.status = 'AVAILABLE' " +
           "ORDER BY br.id ASC, bi.sellingPrice ASC")
    List<BookItem> findAvailableItemsByBookId(@Param("bookId") Long bookId);

    long countByBookId(Long bookId);

    boolean existsBySkuBarcode(String skuBarcode);

    boolean existsBySkuBarcodeAndIdNot(String skuBarcode, Long id);

    Optional<BookItem> findBySkuBarcode(String skuBarcode);

    @Query(value = "SELECT bi FROM BookItem bi " +
                   "JOIN FETCH bi.book b " +
                   "JOIN FETCH bi.branch br " +
                   "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                   "       LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "       LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "       LOWER(bi.skuBarcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "       LOWER(bi.shelfLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "AND (:branchId IS NULL OR br.id = :branchId) " +
                   "AND (:conditionGrade IS NULL OR :conditionGrade = '' OR bi.conditionGrade = :conditionGrade) " +
                   "AND (:status IS NULL OR :status = '' OR bi.status = :status)",
           countQuery = "SELECT COUNT(bi) FROM BookItem bi " +
                        "JOIN bi.book b " +
                        "JOIN bi.branch br " +
                        "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                        "       LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "       LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "       LOWER(bi.skuBarcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "       LOWER(bi.shelfLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:branchId IS NULL OR br.id = :branchId) " +
                        "AND (:conditionGrade IS NULL OR :conditionGrade = '' OR bi.conditionGrade = :conditionGrade) " +
                        "AND (:status IS NULL OR :status = '' OR bi.status = :status)")
    Page<BookItem> searchBookItems(
            @Param("keyword") String keyword,
            @Param("branchId") Long branchId,
            @Param("conditionGrade") String conditionGrade,
            @Param("status") String status,
            Pageable pageable
    );

    long countByStatus(String status);

    long countByBranchId(Long branchId);
}

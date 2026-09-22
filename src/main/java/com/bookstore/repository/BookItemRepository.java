package com.bookstore.repository;

import com.bookstore.entity.BookItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bi FROM BookItem bi WHERE bi.id = :id")
    Optional<BookItem> findByIdWithLock(@Param("id") Long id);
}

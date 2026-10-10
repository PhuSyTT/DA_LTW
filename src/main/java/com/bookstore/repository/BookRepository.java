package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b " +
           "WHERE REPLACE(b.isbn, '-', '') = :cleanIsbn " +
           "AND (:excludeId IS NULL OR b.id != :excludeId)")
    boolean existsByCleanIsbn(@Param("cleanIsbn") String cleanIsbn, @Param("excludeId") Long excludeId);

    Optional<Book> findByIsbn(String isbn);

    @Query("SELECT b FROM Book b " +
           "JOIN FETCH b.author a " +
           "JOIN FETCH b.category c " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "       LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId OR c.parent.id = :categoryId) " +
           "AND (:authorId IS NULL OR a.id = :authorId)")
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("authorId") Long authorId,
                           Pageable pageable);

    @Query("SELECT COUNT(bi) FROM BookItem bi WHERE bi.book.id = :bookId")
    long countInventoryItems(@Param("bookId") Long bookId);
}

package com.bookstore.repository;

import com.bookstore.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Author> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    @Query("SELECT a FROM Author a WHERE :keyword IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.name ASC")
    Page<Author> searchAuthors(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a.id, a.name, COUNT(b.id) FROM Author a LEFT JOIN a.books b GROUP BY a.id, a.name ORDER BY a.name ASC")
    List<Object[]> countBooksByAuthor();
}

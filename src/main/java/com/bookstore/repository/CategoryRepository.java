package com.bookstore.repository;

import com.bookstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByParentIsNullOrderByNameAsc();

    List<Category> findByParentIdOrderByNameAsc(Long parentId);

    @Query("SELECT c.id, c.name, COUNT(b.id) FROM Category c LEFT JOIN c.books b GROUP BY c.id, c.name ORDER BY c.name ASC")
    List<Object[]> countBooksByCategory();
}

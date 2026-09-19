package com.bookstore.repository;

import com.bookstore.entity.BookItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookItemImageRepository extends JpaRepository<BookItemImage, Long> {

    List<BookItemImage> findByBookItemId(Long bookItemId);

    void deleteByBookItemId(Long bookItemId);
}

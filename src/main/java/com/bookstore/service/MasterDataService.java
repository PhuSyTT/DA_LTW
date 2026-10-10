package com.bookstore.service;

import com.bookstore.dto.BookDetailResponseDto;
import com.bookstore.dto.BookDto;
import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MasterDataService {
    Page<BookDto> searchBooks(String keyword, Long categoryId, Long authorId, Pageable pageable);
    BookDto getBookDtoById(Long id);
    Book getBookEntityById(Long id);
    BookDetailResponseDto getBookDetail(Long id);
    BookDto createBook(BookDto bookDto);
    BookDto updateBook(Long id, BookDto bookDto);
    void deleteBook(Long id);
    boolean isIsbnAvailable(String isbn, Long excludeBookId);
    java.util.List<BookDto> getAllBooks();
    long getTotalBooksCount();
    long getTotalInventoryCount();
}


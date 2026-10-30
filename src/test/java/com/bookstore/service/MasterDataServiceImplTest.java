package com.bookstore.service;

import com.bookstore.dto.BookDto;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.service.impl.MasterDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterDataServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookItemRepository bookItemRepository;

    private MasterDataServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MasterDataServiceImpl(bookRepository, authorRepository, categoryRepository, bookItemRepository);
    }

    @Test
    @DisplayName("createBook saves book with correct title")
    void testCreateBook() {
        BookDto dto = new BookDto();
        dto.setTitle("   Tôi Thấy Hoa Vàng Trên Cỏ Xanh   ");
        dto.setIsbn("978-604-1-12345-8");
        dto.setAuthorId(1L);
        dto.setCategoryId(2L);
        dto.setOriginalPrice(new BigDecimal("125000"));

        Author author = new Author(1L, "Nguyễn Nhật Ánh", "Bio", "url");
        Category category = new Category(2L, "Văn học", null);

        when(bookRepository.existsByCleanIsbn(any(), any())).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookDto saved = service.createBook(dto);

        assertEquals("Tôi Thấy Hoa Vàng Trên Cỏ Xanh", saved.getTitle());
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertEquals("Tôi Thấy Hoa Vàng Trên Cỏ Xanh", captor.getValue().getTitle());
    }
}


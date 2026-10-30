package com.bookstore.controller;

import com.bookstore.controller.admin.AdminMasterDataController;
import com.bookstore.dto.BookDto;
import com.bookstore.entity.Author;
import com.bookstore.entity.Category;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.service.AuthorService;
import com.bookstore.service.CategoryService;
import com.bookstore.service.CloudinaryService;
import com.bookstore.service.MasterDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMasterDataControllerTest {

    @Mock
    private MasterDataService masterDataService;
    @Mock
    private AuthorService authorService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuthorRepository authorRepository;

    private AdminMasterDataController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminMasterDataController(
                masterDataService,
                authorService,
                categoryService,
                cloudinaryService,
                categoryRepository,
                authorRepository
        );
    }

    @Test
    @DisplayName("Create Book: title should be preserved exactly as entered")
    void testCreateBook_PreservesTitle() {
        BookDto dto = new BookDto();
        dto.setTitle("Mắt Biếc");
        dto.setIsbn("978-604-1-99999-1");
        dto.setAuthorId(1L);
        dto.setCategoryId(2L);
        dto.setOriginalPrice(new BigDecimal("110000"));
        dto.setPublisher("NXB Trẻ");
        dto.setPublishYear(2021);

        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "bookDto");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ConcurrentModel();

        when(masterDataService.isIsbnAvailable(any(), any())).thenReturn(true);
        when(masterDataService.createBook(any(BookDto.class))).thenAnswer(inv -> inv.getArgument(0));

        String view = controller.createBook(dto, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/admin/books", view);
        assertFalse(bindingResult.hasErrors());

        ArgumentCaptor<BookDto> captor = ArgumentCaptor.forClass(BookDto.class);
        verify(masterDataService).createBook(captor.capture());
        assertEquals("Mắt Biếc", captor.getValue().getTitle());
    }
}


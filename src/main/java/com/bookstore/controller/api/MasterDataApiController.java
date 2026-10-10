package com.bookstore.controller.api;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDetailResponseDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.CategoryDto;
import com.bookstore.service.AuthorService;
import com.bookstore.service.CategoryService;
import com.bookstore.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/master")
@CrossOrigin(origins = "*")
public class MasterDataApiController {

    private final MasterDataService masterDataService;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    public MasterDataApiController(MasterDataService masterDataService,
                                   AuthorService authorService,
                                   CategoryService categoryService) {
        this.masterDataService = masterDataService;
        this.authorService = authorService;
        this.categoryService = categoryService;
    }

    @GetMapping("/books/check-isbn")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkIsbn(
            @RequestParam("isbn") String isbn,
            @RequestParam(name = "excludeId", required = false) Long excludeId
    ) {
        boolean available = masterDataService.isIsbnAvailable(isbn, excludeId);
        Map<String, Object> result = new HashMap<>();
        result.put("isbn", isbn);
        result.put("available", available);
        result.put("message", available ? "Mã ISBN hợp lệ và chưa được sử dụng." : "Mã ISBN đã tồn tại trong Master Data!");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponseDto>> getBookDetail(@PathVariable("id") Long id) {
        try {
            BookDetailResponseDto detail = masterDataService.getBookDetail(id);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy đầu sách: " + e.getMessage()));
        }
    }

    @GetMapping("/authors/search")
    public ResponseEntity<ApiResponse<List<AuthorDto>>> searchAuthors(@RequestParam(name = "q", defaultValue = "") String query) {
        List<AuthorDto> authors = authorService.searchAuthors(query);
        return ResponseEntity.ok(ApiResponse.success(authors));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        List<CategoryDto> categories = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}

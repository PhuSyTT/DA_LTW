package com.bookstore.service.impl;

import com.bookstore.dto.BookDetailResponseDto;
import com.bookstore.dto.BookDto;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Category;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.service.MasterDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MasterDataServiceImpl implements MasterDataService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookItemRepository bookItemRepository;

    public MasterDataServiceImpl(BookRepository bookRepository,
                                  AuthorRepository authorRepository,
                                  CategoryRepository categoryRepository,
                                  BookItemRepository bookItemRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookItemRepository = bookItemRepository;
    }

    @Override
    public Page<BookDto> searchBooks(String keyword, Long categoryId, Long authorId, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Book> bookPage = bookRepository.searchBooks(cleanKeyword, categoryId, authorId, pageable);
        return bookPage.map(this::mapToDto);
    }

    @Override
    public BookDto getBookDtoById(Long id) {
        Book book = getBookEntityById(id);
        return mapToDto(book);
    }

    @Override
    public Book getBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đầu sách với ID: " + id));
    }

    @Override
    public BookDetailResponseDto getBookDetail(Long id) {
        Book book = getBookEntityById(id);
        List<BookItem> items = bookItemRepository.findByBookId(id);

        long totalStock = items.stream()
                .mapToLong(BookItem::getStockQuantity)
                .sum();

        List<BookDetailResponseDto.BranchStockDto> branchStocks = items.stream()
                .map(item -> BookDetailResponseDto.BranchStockDto.builder()
                        .id(item.getId())
                        .branchId(item.getBranch().getId())
                        .branchName(item.getBranch().getBranchName())
                        .skuBarcode(item.getSkuBarcode())
                        .conditionGrade(item.getConditionGrade())
                        .sellingPrice(item.getSellingPrice())
                        .stockQuantity(item.getStockQuantity())
                        .shelfLocation(item.getShelfLocation())
                        .status(item.getStatus())
                        .build())
                .collect(Collectors.toList());

        return BookDetailResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())
                .authorBio(book.getAuthor().getBiography())
                .categoryId(book.getCategory().getId())
                .categoryName(book.getCategory().getName())
                .publisher(book.getPublisher())
                .publishYear(book.getPublishYear())
                .originalPrice(book.getOriginalPrice())
                .coverImageUrl(book.getCoverImageUrl())
                .description(book.getDescription())
                .totalInventory(totalStock)
                .branchStocks(branchStocks)
                .build();
    }

    @Override
    @Transactional
    public BookDto createBook(BookDto bookDto) {
        String cleanIsbn = bookDto.getIsbn().trim().replaceAll("-", "");
        if (bookRepository.existsByCleanIsbn(cleanIsbn, null)) {
            throw new IllegalArgumentException("Mã ISBN '" + bookDto.getIsbn() + "' đã tồn tại trong danh mục Master Data!");
        }

        Author author = authorRepository.findById(bookDto.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Tác giả không hợp lệ!"));
        Category category = categoryRepository.findById(bookDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Thể loại không hợp lệ!"));

        Book book = Book.builder()
                .title(bookDto.getTitle().trim())
                .isbn(bookDto.getIsbn().trim())
                .author(author)
                .category(category)
                .publisher(bookDto.getPublisher())
                .publishYear(bookDto.getPublishYear())
                .originalPrice(bookDto.getOriginalPrice())
                .coverImageUrl(bookDto.getCoverImageUrl())
                .description(bookDto.getDescription())
                .build();

        Book saved = bookRepository.save(book);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public BookDto updateBook(Long id, BookDto bookDto) {
        Book book = getBookEntityById(id);
        String cleanIsbn = bookDto.getIsbn().trim().replaceAll("-", "");

        if (bookRepository.existsByCleanIsbn(cleanIsbn, id)) {
            throw new IllegalArgumentException("Mã ISBN '" + bookDto.getIsbn() + "' đã thuộc về một đầu sách khác!");
        }

        Author author = authorRepository.findById(bookDto.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Tác giả không hợp lệ!"));
        Category category = categoryRepository.findById(bookDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Thể loại không hợp lệ!"));

        book.setTitle(bookDto.getTitle().trim());
        book.setIsbn(bookDto.getIsbn().trim());
        book.setAuthor(author);
        book.setCategory(category);
        book.setPublisher(bookDto.getPublisher());
        book.setPublishYear(bookDto.getPublishYear());
        book.setOriginalPrice(bookDto.getOriginalPrice());
        book.setCoverImageUrl(bookDto.getCoverImageUrl());
        book.setDescription(bookDto.getDescription());

        Book saved = bookRepository.save(book);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookEntityById(id);
        long stockCount = bookItemRepository.countByBookId(id);
        if (stockCount > 0) {
            throw new IllegalStateException("Không thể xóa đầu sách '" + book.getTitle() 
                    + "' vì đang có " + stockCount + " bản ghi tồn kho/mặt hàng tại các chi nhánh!");
        }
        bookRepository.delete(book);
    }

    @Override
    public boolean isIsbnAvailable(String isbn, Long excludeBookId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        String cleanIsbn = isbn.trim().replaceAll("-", "");
        return !bookRepository.existsByCleanIsbn(cleanIsbn, excludeBookId);
    }

    @Override
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll(org.springframework.data.domain.Sort.by("title").ascending())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalBooksCount() {
        return bookRepository.count();
    }

    @Override
    public long getTotalInventoryCount() {
        return bookItemRepository.count();
    }

    private BookDto mapToDto(Book book) {
        long inventoryCount = (book.getBookItems() != null) ? book.getBookItems().size() : 0;
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())
                .categoryId(book.getCategory().getId())
                .categoryName(book.getCategory().getName())
                .publisher(book.getPublisher())
                .publishYear(book.getPublishYear())
                .originalPrice(book.getOriginalPrice())
                .coverImageUrl(book.getCoverImageUrl())
                .description(book.getDescription())
                .inventoryCount(inventoryCount)
                .build();
    }
}

package com.bookstore.service.impl;

import com.bookstore.dto.*;
import com.bookstore.entity.Book;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.BookItemImage;
import com.bookstore.entity.Branch;
import com.bookstore.repository.BookItemImageRepository;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.service.CloudinaryService;
import com.bookstore.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final BookItemRepository bookItemRepository;
    private final BookItemImageRepository bookItemImageRepository;
    private final BookRepository bookRepository;
    private final BranchRepository branchRepository;
    private final CloudinaryService cloudinaryService;

    public InventoryServiceImpl(BookItemRepository bookItemRepository,
                                BookItemImageRepository bookItemImageRepository,
                                BookRepository bookRepository,
                                BranchRepository branchRepository,
                                CloudinaryService cloudinaryService) {
        this.bookItemRepository = bookItemRepository;
        this.bookItemImageRepository = bookItemImageRepository;
        this.bookRepository = bookRepository;
        this.branchRepository = branchRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookItemDto> searchInventory(String keyword, Long branchId, String conditionGrade, String status, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanGrade = (conditionGrade != null && !conditionGrade.trim().isEmpty()) ? conditionGrade.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        Page<BookItem> pageResult = bookItemRepository.searchBookItems(cleanKeyword, branchId, cleanGrade, cleanStatus, pageable);
        return pageResult.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookItemDto getBookItemById(Long id) {
        BookItem item = bookItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mặt hàng sách cũ với mã: " + id));
        return mapToDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public BookItemDto getBookItemByBarcode(String barcode) {
        BookItem item = bookItemRepository.findBySkuBarcode(barcode)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mặt hàng với mã SKU: " + barcode));
        return mapToDto(item);
    }

    @Override
    public BookItemDto createBookItem(BookItemCreateDto dto) {
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đầu sách Master ID: " + dto.getBookId()));

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh ID: " + dto.getBranchId()));

        if (bookItemRepository.existsBySkuBarcode(dto.getSkuBarcode())) {
            throw new IllegalArgumentException("Mã SKU / Barcode '" + dto.getSkuBarcode() + "' đã tồn tại!");
        }

        BookItem item = BookItem.builder()
                .book(book)
                .branch(branch)
                .skuBarcode(dto.getSkuBarcode().trim().toUpperCase())
                .conditionGrade(dto.getConditionGrade())
                .sellingPrice(dto.getSellingPrice())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 1)
                .shelfLocation(dto.getShelfLocation() != null ? dto.getShelfLocation().trim() : null)
                .conditionNote(dto.getConditionNote() != null ? dto.getConditionNote().trim() : null)
                .status(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE")
                .importDate(LocalDateTime.now())
                .build();

        BookItem savedItem = bookItemRepository.save(item);

        // Upload physical images if provided (FR-INV-03)
        if (dto.getImageFiles() != null && !dto.getImageFiles().isEmpty()) {
            int primaryIdx = (dto.getPrimaryImageIndex() != null) ? dto.getPrimaryImageIndex() : 0;
            int currentIdx = 0;
            for (MultipartFile file : dto.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file, "bookstore/book_items");
                        boolean isPrimary = (currentIdx == primaryIdx);
                        BookItemImage img = BookItemImage.builder()
                                .bookItem(savedItem)
                                .imageUrl(result.imageUrl())
                                .cloudinaryPublicId(result.publicId())
                                .isPrimary(isPrimary)
                                .build();
                        bookItemImageRepository.save(img);
                        savedItem.addImage(img);
                        currentIdx++;
                    } catch (Exception e) {
                        log.error("Lỗi khi upload ảnh cho SKU {}: {}", savedItem.getSkuBarcode(), e.getMessage());
                    }
                }
            }
        }

        return mapToDto(savedItem);
    }

    @Override
    public BookItemDto updateBookItem(Long id, BookItemCreateDto dto) {
        BookItem item = bookItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mặt hàng với ID: " + id));

        if (bookItemRepository.existsBySkuBarcodeAndIdNot(dto.getSkuBarcode(), id)) {
            throw new IllegalArgumentException("Mã SKU / Barcode '" + dto.getSkuBarcode() + "' đã được sử dụng bởi mặt hàng khác!");
        }

        item.setSkuBarcode(dto.getSkuBarcode().trim().toUpperCase());
        item.setConditionGrade(dto.getConditionGrade());
        item.setSellingPrice(dto.getSellingPrice());
        if (dto.getStockQuantity() != null) {
            item.setStockQuantity(dto.getStockQuantity());
        }
        item.setShelfLocation(dto.getShelfLocation() != null ? dto.getShelfLocation().trim() : null);
        item.setConditionNote(dto.getConditionNote() != null ? dto.getConditionNote().trim() : null);
        if (dto.getStatus() != null) {
            item.setStatus(dto.getStatus());
        }

        // Upload any newly added images
        if (dto.getImageFiles() != null && !dto.getImageFiles().isEmpty()) {
            for (MultipartFile file : dto.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file, "bookstore/book_items");
                        BookItemImage img = BookItemImage.builder()
                                .bookItem(item)
                                .imageUrl(result.imageUrl())
                                .cloudinaryPublicId(result.publicId())
                                .isPrimary(false)
                                .build();
                        bookItemImageRepository.save(img);
                        item.addImage(img);
                    } catch (Exception e) {
                        log.error("Lỗi khi upload ảnh bổ sung cho SKU {}: {}", item.getSkuBarcode(), e.getMessage());
                    }
                }
            }
        }

        BookItem updated = bookItemRepository.save(item);
        return mapToDto(updated);
    }

    @Override
    public boolean updateShelfLocation(Long id, String shelfLocation) {
        BookItem item = bookItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mặt hàng với ID: " + id));
        item.setShelfLocation(shelfLocation != null ? shelfLocation.trim() : null);
        bookItemRepository.save(item);
        log.info("Cập nhật vị trí kệ thành công cho SKU: {} -> {}", item.getSkuBarcode(), shelfLocation);
        return true;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        BookItem item = bookItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mặt hàng với ID: " + id));
        item.setStatus(status);
        bookItemRepository.save(item);
        return true;
    }

    @Override
    public void deleteBookItem(Long id) {
        BookItem item = bookItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mặt hàng với ID: " + id));
        // Soft delete / change status or hard delete
        item.setStatus("SOLD");
        bookItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateSkuBarcode(Long branchId, Long bookId, String conditionGrade) {
        String branchPrefix = "BR";
        if (branchId != null) {
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isPresent()) {
                String name = branchOpt.get().getBranchName().toUpperCase();
                if (name.contains("QUẬN 1") || name.contains("QUAN 1") || name.contains("Q1")) {
                    branchPrefix = "Q1";
                } else if (name.contains("HÀ NỘI") || name.contains("HA NOI") || name.contains("HN")) {
                    branchPrefix = "HN";
                } else if (name.contains("THỦ ĐỨC") || name.contains("THU DUC") || name.contains("TD")) {
                    branchPrefix = "TD";
                } else if (name.contains("ĐÀ NẴNG") || name.contains("DA NANG") || name.contains("DN")) {
                    branchPrefix = "DN";
                } else {
                    branchPrefix = "B" + branchId;
                }
            }
        }

        String bookCode = "BK" + (bookId != null ? bookId : 1);
        if (bookId != null) {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                String title = bookOpt.get().getTitle();
                // Get initials of words, e.g. "Tôi Thấy Hoa Vàng" -> "TTHV"
                StringBuilder initials = new StringBuilder();
                for (String w : title.trim().split("\\s+")) {
                    if (!w.isEmpty() && Character.isLetterOrDigit(w.charAt(0))) {
                        initials.append(Character.toUpperCase(w.charAt(0)));
                    }
                }
                if (initials.length() >= 2) {
                    bookCode = initials.substring(0, Math.min(initials.length(), 4));
                }
            }
        }

        String gradeCode = switch (conditionGrade != null ? conditionGrade : "GOOD") {
            case "LIKE_NEW" -> "99";
            case "VERY_GOOD" -> "90";
            case "GOOD" -> "80";
            case "ACCEPTABLE" -> "60";
            case "COLLECTIBLE" -> "CL";
            default -> "80";
        };

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int seq = 1 + random.nextInt(999);
            String candidate = String.format("%s-%s-%s-%03d", branchPrefix, bookCode, gradeCode, seq);
            if (!bookItemRepository.existsBySkuBarcode(candidate)) {
                return candidate;
            }
        }
        return String.format("%s-%s-%s-%d", branchPrefix, bookCode, gradeCode, System.currentTimeMillis() % 10000);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSkuBarcodeAvailable(String skuBarcode, Long excludeId) {
        if (skuBarcode == null || skuBarcode.trim().isEmpty()) {
            return false;
        }
        if (excludeId == null) {
            return !bookItemRepository.existsBySkuBarcode(skuBarcode.trim());
        }
        return !bookItemRepository.existsBySkuBarcodeAndIdNot(skuBarcode.trim(), excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal suggestSellingPrice(Long bookId, String conditionGrade) {
        PriceRangeDto range = getSuggestedPriceRange(bookId, conditionGrade);
        return range.getSuggestedPrice();
    }

    @Override
    @Transactional(readOnly = true)
    public PriceRangeDto getSuggestedPriceRange(Long bookId, String conditionGrade) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đầu sách ID: " + bookId));

        BigDecimal originalPrice = book.getOriginalPrice() != null ? book.getOriginalPrice() : BigDecimal.ZERO;

        int minRate;
        int maxRate;
        int suggestedRate;

        switch (conditionGrade != null ? conditionGrade : "GOOD") {
            case "LIKE_NEW" -> {
                minRate = 70;
                maxRate = 85;
                suggestedRate = 75;
            }
            case "VERY_GOOD" -> {
                minRate = 55;
                maxRate = 70;
                suggestedRate = 60;
            }
            case "GOOD" -> {
                minRate = 40;
                maxRate = 55;
                suggestedRate = 45;
            }
            case "ACCEPTABLE" -> {
                minRate = 25;
                maxRate = 40;
                suggestedRate = 30;
            }
            case "COLLECTIBLE" -> {
                minRate = 100;
                maxRate = 200;
                suggestedRate = 120;
            }
            default -> {
                minRate = 40;
                maxRate = 55;
                suggestedRate = 45;
            }
        }

        BigDecimal minPrice = roundToThousands(originalPrice.multiply(BigDecimal.valueOf(minRate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        BigDecimal maxPrice = roundToThousands(originalPrice.multiply(BigDecimal.valueOf(maxRate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        BigDecimal suggestedPrice = roundToThousands(originalPrice.multiply(BigDecimal.valueOf(suggestedRate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        return new PriceRangeDto(originalPrice, minPrice, maxPrice, suggestedPrice, minRate, maxRate, suggestedRate);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStatsDto getInventoryStats() {
        long total = bookItemRepository.count();
        long available = bookItemRepository.countByStatus("AVAILABLE");
        long reserved = bookItemRepository.countByStatus("RESERVED");
        long sold = bookItemRepository.countByStatus("SOLD");
        return new InventoryStatsDto(total, available, reserved, sold);
    }

    private BigDecimal roundToThousands(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        long val = value.longValue();
        long remainder = val % 1000;
        if (remainder >= 500) {
            val = val + (1000 - remainder);
        } else {
            val = val - remainder;
        }
        return BigDecimal.valueOf(Math.max(1000, val));
    }

    private BookItemDto mapToDto(BookItem item) {
        BookItemDto dto = new BookItemDto();
        dto.setId(item.getId());

        if (item.getBook() != null) {
            dto.setBookId(item.getBook().getId());
            dto.setBookTitle(item.getBook().getTitle());
            dto.setBookIsbn(item.getBook().getIsbn());
            dto.setBookCoverImageUrl(item.getBook().getCoverImageUrl());
            dto.setBookOriginalPrice(item.getBook().getOriginalPrice());
            if (item.getBook().getAuthor() != null) {
                dto.setAuthorName(item.getBook().getAuthor().getName());
            }
            if (item.getBook().getCategory() != null) {
                dto.setCategoryName(item.getBook().getCategory().getName());
            }
        }

        if (item.getBranch() != null) {
            dto.setBranchId(item.getBranch().getId());
            dto.setBranchName(item.getBranch().getBranchName());
            dto.setBranchCity(item.getBranch().getCity());
        }

        dto.setSkuBarcode(item.getSkuBarcode());
        dto.setConditionGrade(item.getConditionGrade());
        dto.setSellingPrice(item.getSellingPrice());
        dto.setStockQuantity(item.getStockQuantity());
        dto.setShelfLocation(item.getShelfLocation());
        dto.setConditionNote(item.getConditionNote());
        dto.setImportDate(item.getImportDate());
        dto.setStatus(item.getStatus());

        List<BookItemImage> images = bookItemImageRepository.findByBookItemId(item.getId());
        List<BookItemImageDto> imageDtos = new ArrayList<>();
        String primaryUrl = null;

        for (BookItemImage img : images) {
            BookItemImageDto imgDto = new BookItemImageDto(
                    img.getId(),
                    item.getId(),
                    img.getImageUrl(),
                    img.getCloudinaryPublicId(),
                    img.getIsPrimary()
            );
            imageDtos.add(imgDto);
            if (Boolean.TRUE.equals(img.getIsPrimary())) {
                primaryUrl = img.getImageUrl();
            }
        }

        dto.setImages(imageDtos);
        if (primaryUrl != null) {
            dto.setPrimaryImageUrl(primaryUrl);
        } else if (!imageDtos.isEmpty()) {
            dto.setPrimaryImageUrl(imageDtos.get(0).getImageUrl());
        }

        return dto;
    }
}

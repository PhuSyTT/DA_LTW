package com.bookstore.service;

import com.bookstore.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface InventoryService {

    Page<BookItemDto> searchInventory(String keyword, Long branchId, String conditionGrade, String status, Pageable pageable);

    BookItemDto getBookItemById(Long id);

    BookItemDto getBookItemByBarcode(String barcode);

    BookItemDto createBookItem(BookItemCreateDto dto);

    BookItemDto updateBookItem(Long id, BookItemCreateDto dto);

    boolean updateShelfLocation(Long id, String shelfLocation);

    boolean updateStatus(Long id, String status);

    void deleteBookItem(Long id);

    String generateSkuBarcode(Long branchId, Long bookId, String conditionGrade);

    boolean isSkuBarcodeAvailable(String skuBarcode, Long excludeId);

    BigDecimal suggestSellingPrice(Long bookId, String conditionGrade);

    PriceRangeDto getSuggestedPriceRange(Long bookId, String conditionGrade);

    InventoryStatsDto getInventoryStats();
}

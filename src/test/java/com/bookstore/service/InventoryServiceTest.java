package com.bookstore.service;

import com.bookstore.dto.PriceRangeDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.Branch;
import com.bookstore.repository.BookItemImageRepository;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private BookItemImageRepository bookItemImageRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Book sampleBook;
    private Branch sampleBranch;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Tôi Thấy Hoa Vàng Trên Cỏ Xanh")
                .isbn("9786041180010")
                .originalPrice(new BigDecimal("100000"))
                .build();

        sampleBranch = new Branch(1L, "Chi nhánh Quận 1", "123 Lê Lợi", "0901234567", "Hồ Chí Minh", null, null, true);
    }

    @Test
    @DisplayName("FR-INV-02: Kiểm tra gợi ý dải giá theo thang đo LIKE_NEW (70% - 85%)")
    void testPriceRecommendation_LikeNew() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        PriceRangeDto range = inventoryService.getSuggestedPriceRange(1L, "LIKE_NEW");

        assertNotNull(range);
        assertEquals(70, range.getRateMinPercent());
        assertEquals(85, range.getRateMaxPercent());
        assertEquals(75, range.getSuggestedPercent());
        assertEquals(0, new BigDecimal("70000").compareTo(range.getMinPrice()));
        assertEquals(0, new BigDecimal("85000").compareTo(range.getMaxPrice()));
        assertEquals(0, new BigDecimal("75000").compareTo(range.getSuggestedPrice()));
    }

    @Test
    @DisplayName("FR-INV-02: Kiểm tra gợi ý dải giá theo thang đo VERY_GOOD (55% - 70%)")
    void testPriceRecommendation_VeryGood() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        PriceRangeDto range = inventoryService.getSuggestedPriceRange(1L, "VERY_GOOD");

        assertNotNull(range);
        assertEquals(55, range.getRateMinPercent());
        assertEquals(70, range.getRateMaxPercent());
        assertEquals(60, range.getSuggestedPercent());
        assertEquals(0, new BigDecimal("55000").compareTo(range.getMinPrice()));
        assertEquals(0, new BigDecimal("70000").compareTo(range.getMaxPrice()));
        assertEquals(0, new BigDecimal("60000").compareTo(range.getSuggestedPrice()));
    }

    @Test
    @DisplayName("FR-INV-02: Kiểm tra gợi ý dải giá theo thang đo GOOD (40% - 55%)")
    void testPriceRecommendation_Good() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        PriceRangeDto range = inventoryService.getSuggestedPriceRange(1L, "GOOD");

        assertNotNull(range);
        assertEquals(40, range.getRateMinPercent());
        assertEquals(55, range.getRateMaxPercent());
        assertEquals(45, range.getSuggestedPercent());
        assertEquals(0, new BigDecimal("40000").compareTo(range.getMinPrice()));
        assertEquals(0, new BigDecimal("55000").compareTo(range.getMaxPrice()));
        assertEquals(0, new BigDecimal("45000").compareTo(range.getSuggestedPrice()));
    }

    @Test
    @DisplayName("FR-INV-02: Kiểm tra gợi ý dải giá theo thang đo ACCEPTABLE (25% - 40%)")
    void testPriceRecommendation_Acceptable() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        PriceRangeDto range = inventoryService.getSuggestedPriceRange(1L, "ACCEPTABLE");

        assertNotNull(range);
        assertEquals(25, range.getRateMinPercent());
        assertEquals(40, range.getRateMaxPercent());
        assertEquals(30, range.getSuggestedPercent());
        assertEquals(0, new BigDecimal("25000").compareTo(range.getMinPrice()));
        assertEquals(0, new BigDecimal("40000").compareTo(range.getMaxPrice()));
        assertEquals(0, new BigDecimal("30000").compareTo(range.getSuggestedPrice()));
    }

    @Test
    @DisplayName("FR-INV-01: Sinh mã vạch SKU độc nhất có tiền tố chi nhánh và độ cũ")
    void testGenerateSkuBarcode() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sampleBranch));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookItemRepository.existsBySkuBarcode(anyString())).thenReturn(false);

        String sku = inventoryService.generateSkuBarcode(1L, 1L, "VERY_GOOD");

        assertNotNull(sku);
        assertTrue(sku.startsWith("Q1-"));
        assertTrue(sku.contains("-90-"));
    }

    @Test
    @DisplayName("Kiểm tra tính khả dụng của mã SKU")
    void testIsSkuBarcodeAvailable() {
        when(bookItemRepository.existsBySkuBarcode("Q1-TEST-001")).thenReturn(false);
        when(bookItemRepository.existsBySkuBarcode("Q1-EXIST-001")).thenReturn(true);

        assertTrue(inventoryService.isSkuBarcodeAvailable("Q1-TEST-001", null));
        assertFalse(inventoryService.isSkuBarcodeAvailable("Q1-EXIST-001", null));
    }
}

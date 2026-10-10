package com.bookstore.controller.admin;

import com.bookstore.dto.BookDto;
import com.bookstore.dto.BookItemCreateDto;
import com.bookstore.dto.BookItemDto;
import com.bookstore.dto.InventoryStatsDto;
import com.bookstore.service.BranchService;
import com.bookstore.service.InventoryService;
import com.bookstore.service.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final MasterDataService masterDataService;
    private final BranchService branchService;

    public AdminInventoryController(InventoryService inventoryService,
                                  MasterDataService masterDataService,
                                  BranchService branchService) {
        this.inventoryService = inventoryService;
        this.masterDataService = masterDataService;
        this.branchService = branchService;
    }

    @GetMapping
    public String listInventory(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "conditionGrade", required = false) String conditionGrade,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir,
            Model model
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookItemDto> inventoryPage = inventoryService.searchInventory(keyword, branchId, conditionGrade, status, pageable);
        InventoryStatsDto stats = inventoryService.getInventoryStats();

        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("items", inventoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventoryPage.getTotalPages());
        model.addAttribute("totalElements", inventoryPage.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("branchId", branchId);
        model.addAttribute("conditionGrade", conditionGrade);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("branches", branchService.getAllActiveBranches());
        model.addAttribute("stats", stats);
        model.addAttribute("activeMenu", "inventory");

        return "admin/inventory/list";
    }

    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(name = "bookId", required = false) Long bookId,
            @RequestParam(name = "branchId", required = false) Long branchId,
            Model model
    ) {
        BookItemCreateDto dto = new BookItemCreateDto();
        dto.setConditionGrade("GOOD");
        dto.setStockQuantity(1);
        dto.setStatus("AVAILABLE");

        List<BookDto> books = masterDataService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("branches", branchService.getAllActiveBranches());

        if (bookId != null) {
            dto.setBookId(bookId);
            try {
                BookDto book = masterDataService.getBookDtoById(bookId);
                model.addAttribute("selectedBook", book);
                dto.setSellingPrice(inventoryService.suggestSellingPrice(bookId, dto.getConditionGrade()));
            } catch (Exception ignored) {
            }
        }

        if (branchId != null) {
            dto.setBranchId(branchId);
        } else if (!branchService.getAllActiveBranches().isEmpty()) {
            dto.setBranchId(branchService.getAllActiveBranches().get(0).getId());
        }

        // Auto-generate suggested SKU
        if (dto.getBookId() != null && dto.getBranchId() != null) {
            dto.setSkuBarcode(inventoryService.generateSkuBarcode(dto.getBranchId(), dto.getBookId(), dto.getConditionGrade()));
        }

        model.addAttribute("bookItemDto", dto);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", "inventory");

        return "admin/inventory/form";
    }

    @PostMapping("/create")
    public String createBookItem(
            @Valid @ModelAttribute("bookItemDto") BookItemCreateDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!inventoryService.isSkuBarcodeAvailable(dto.getSkuBarcode(), null)) {
            bindingResult.rejectValue("skuBarcode", "duplicate.skuBarcode",
                    "Mã SKU / Barcode '" + dto.getSkuBarcode() + "' đã tồn tại trong hệ thống kho!");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("books", masterDataService.getAllBooks());
            model.addAttribute("branches", branchService.getAllActiveBranches());
            if (dto.getBookId() != null) {
                try {
                    model.addAttribute("selectedBook", masterDataService.getBookDtoById(dto.getBookId()));
                } catch (Exception ignored) {}
            }
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", "inventory");
            return "admin/inventory/form";
        }

        try {
            BookItemDto saved = inventoryService.createBookItem(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Nhập kho thành công cuốn sách cũ! Mã SKU: " + saved.getSkuBarcode() +
                    " tại " + saved.getBranchName());
            return "redirect:/admin/inventory/detail/" + saved.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi nhập kho: " + e.getMessage());
            model.addAttribute("books", masterDataService.getAllBooks());
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", "inventory");
            return "admin/inventory/form";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable("id") Long id, Model model) {
        BookItemDto item = inventoryService.getBookItemById(id);
        model.addAttribute("item", item);
        model.addAttribute("activeMenu", "inventory");
        return "admin/inventory/detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        BookItemDto item = inventoryService.getBookItemById(id);

        BookItemCreateDto dto = new BookItemCreateDto();
        dto.setId(item.getId());
        dto.setBookId(item.getBookId());
        dto.setBranchId(item.getBranchId());
        dto.setSkuBarcode(item.getSkuBarcode());
        dto.setConditionGrade(item.getConditionGrade());
        dto.setSellingPrice(item.getSellingPrice());
        dto.setStockQuantity(item.getStockQuantity());
        dto.setShelfLocation(item.getShelfLocation());
        dto.setConditionNote(item.getConditionNote());
        dto.setStatus(item.getStatus());

        model.addAttribute("bookItemDto", dto);
        model.addAttribute("item", item);
        model.addAttribute("branches", branchService.getAllActiveBranches());
        model.addAttribute("books", masterDataService.getAllBooks());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeMenu", "inventory");

        return "admin/inventory/form";
    }

    @PostMapping("/edit/{id}")
    public String updateBookItem(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("bookItemDto") BookItemCreateDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!inventoryService.isSkuBarcodeAvailable(dto.getSkuBarcode(), id)) {
            bindingResult.rejectValue("skuBarcode", "duplicate.skuBarcode",
                    "Mã SKU / Barcode '" + dto.getSkuBarcode() + "' đã tồn tại trên một mặt hàng khác!");
        }

        if (bindingResult.hasErrors()) {
            BookItemDto item = inventoryService.getBookItemById(id);
            model.addAttribute("item", item);
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("books", masterDataService.getAllBooks());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "inventory");
            return "admin/inventory/form";
        }

        try {
            inventoryService.updateBookItem(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mặt hàng SKU " + dto.getSkuBarcode() + " thành công!");
            return "redirect:/admin/inventory/detail/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            BookItemDto item = inventoryService.getBookItemById(id);
            model.addAttribute("item", item);
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("books", masterDataService.getAllBooks());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "inventory");
            return "admin/inventory/form";
        }
    }

    @PostMapping("/{id}/shelf-location")
    public String updateShelfLocation(
            @PathVariable("id") Long id,
            @RequestParam("shelfLocation") String shelfLocation,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        try {
            inventoryService.updateShelfLocation(id, shelfLocation);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật vị trí kệ sách thành: " + shelfLocation);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật vị trí kệ: " + e.getMessage());
        }
        return referer != null ? "redirect:" + referer : "redirect:/admin/inventory";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        try {
            inventoryService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái mặt hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật trạng thái: " + e.getMessage());
        }
        return referer != null ? "redirect:" + referer : "redirect:/admin/inventory";
    }

    @PostMapping("/{id}/delete")
    public String deleteBookItem(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            inventoryService.deleteBookItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mặt hàng sang trạng thái ĐÃ BÁN (SOLD).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thao tác: " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }
}

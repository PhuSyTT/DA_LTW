package com.bookstore.controller.admin;

import com.bookstore.service.BranchService;
import com.bookstore.service.MasterDataService;
import com.bookstore.service.OrderFulfillmentService;
import com.bookstore.service.TradeInService;
import com.bookstore.service.WishlistService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.bookstore.service.TransferService;
import com.bookstore.repository.CouponRepository;

/**
 * MVC Controller phục vụ render giao diện Thymeleaf cho:
 * - Nhóm 4: Xử lý đơn hàng đa chi nhánh (Order Fulfillment & Realtime Updates)
 * - Nhóm 5: Luân chuyển kho liên chi nhánh (Internal Stock Transfer)
 * - Nhóm 6: Danh sách mong muốn Săn sách (Wishlist & Restock Alert)
 * - Nhóm 7: Quản lý Khuyến mãi & Mã giảm giá đa điều kiện (Coupons & Discounts)
 * - Nhóm 8: Thu mua sách cũ từ người dùng (Trade-in / Buy-back Workflow)
 */
@Controller
public class StaffPortalController {

    private final OrderFulfillmentService fulfillmentService;
    private final TradeInService tradeInService;
    private final WishlistService wishlistService;
    private final BranchService branchService;
    private final MasterDataService masterDataService;
    private final CategoryService categoryService;
    private final TransferService transferService;
    private final CouponRepository couponRepository;

    public StaffPortalController(OrderFulfillmentService fulfillmentService,
                                 TradeInService tradeInService,
                                 WishlistService wishlistService,
                                 BranchService branchService,
                                 MasterDataService masterDataService,
                                 CategoryService categoryService,
                                 TransferService transferService,
                                 CouponRepository couponRepository) {
        this.fulfillmentService = fulfillmentService;
        this.tradeInService = tradeInService;
        this.wishlistService = wishlistService;
        this.branchService = branchService;
        this.masterDataService = masterDataService;
        this.categoryService = categoryService;
        this.transferService = transferService;
        this.couponRepository = couponRepository;
    }

    // =================================================================
    // NHÓM 4: XỬ LÝ ĐƠN HÀNG ĐA CHI NHÁNH (Order Fulfillment)
    // =================================================================

    /**
     * Trang danh sách đơn hàng con theo chi nhánh (FR-FUL-01, FR-FUL-02)
     * URL: /admin/fulfillment?branchId=1&status=PENDING
     */
    @GetMapping("/admin/fulfillment")
    public String fulfillmentList(
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "status", required = false) String status,
            Model model) {
        try {
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("selectedBranchId", branchId);
            model.addAttribute("selectedStatus", status != null ? status : "ALL");

            if (branchId != null) {
                model.addAttribute("orders", fulfillmentService.getOrdersByBranch(branchId, status));
                model.addAttribute("stats", fulfillmentService.getBranchFulfillmentStats(branchId));
            }
        } catch (Exception ignored) {}

        model.addAttribute("activeMenu", "fulfillment");
        model.addAttribute("pageTitle", "Quản Lý Đơn Hàng Chi Nhánh (Nhóm 4)");
        return "admin/fulfillment/list";
    }

    /**
     * Trang chi tiết đơn hàng con + vị trí kệ sách để lấy hàng (FR-FUL-02)
     * URL: /admin/fulfillment/orders/{id}
     */
    @GetMapping("/admin/fulfillment/orders/{id}")
    public String fulfillmentDetail(@PathVariable("id") Long subOrderId, Model model) {
        try {
            model.addAttribute("order", fulfillmentService.getSubOrderDetail(subOrderId));
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("activeMenu", "fulfillment");
        model.addAttribute("pageTitle", "Chi Tiết Đơn Hàng - Xử Lý Đóng Gói");
        return "admin/fulfillment/detail";
    }

    // =================================================================
    // NHÓM 6: DANH SÁCH MONG MUỐN & SĂN SÁCH (Wishlist & Restock Alert)
    // =================================================================

    /**
     * Trang quản lý toàn bộ danh sách Săn sách của admin (FR-WSH-02, FR-WSH-03)
     * URL: /admin/wishlists?userId=1
     */
    @GetMapping("/admin/wishlists")
    public String wishlistManagement(
            @RequestParam(name = "userId", required = false) Long userId,
            Model model) {
        try {
            model.addAttribute("books", masterDataService.searchBooks(null, null, null, PageRequest.of(0, 100)).getContent());
            if (userId != null) {
                model.addAttribute("wishlists", wishlistService.getUserWishlists(userId));
                model.addAttribute("filterUserId", userId);
            }
        } catch (Exception ignored) {}

        model.addAttribute("activeMenu", "wishlists");
        model.addAttribute("pageTitle", "Quản Lý Danh Sách Săn Sách (Nhóm 6)");
        return "admin/wishlist/list";
    }

    // =================================================================
    // NHÓM 8: THU MUA SÁCH CŨ (Trade-in / Buy-back Workflow)
    // =================================================================

    /**
     * Trang danh sách phiếu thu mua sách cũ theo chi nhánh (FR-TRD-04)
     * URL: /admin/trade-in?branchId=1&status=PENDING_REVIEW
     */
    @GetMapping("/admin/trade-in")
    public String tradeInList(
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "status", required = false) String status,
            Model model) {
        try {
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("selectedBranchId", branchId);
            model.addAttribute("selectedStatus", status != null ? status : "ALL");

            if (branchId != null) {
                model.addAttribute("requests", tradeInService.getRequestsByBranch(branchId, status));
            }
        } catch (Exception ignored) {}

        model.addAttribute("activeMenu", "tradeIn");
        model.addAttribute("pageTitle", "Quản Lý Thu Mua Sách Cũ (Nhóm 8)");
        return "admin/trade-in/list";
    }

    /**
     * Trang chi tiết phiếu thu mua để thẩm định (FR-TRD-04, FR-TRD-06)
     * URL: /admin/trade-in/{id}
     */
    @GetMapping("/admin/trade-in/{id}")
    public String tradeInDetail(@PathVariable("id") Long requestId, Model model) {
        try {
            model.addAttribute("request", tradeInService.getRequestById(requestId));
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("activeMenu", "tradeIn");
        model.addAttribute("pageTitle", "Chi Tiết Phiếu Thu Mua - Thẩm Định");
        return "admin/trade-in/detail";
    }

    /**
     * Trang lịch sử phiếu thu mua của khách hàng (FR-TRD-05)
     * URL: /my-trade-in?userId=1
     */
    @GetMapping("/my-trade-in")
    public String myTradeInRequests(
            @RequestParam(name = "userId", required = false) Long userId,
            Model model) {
        try {
            if (userId != null) {
                model.addAttribute("requests", tradeInService.getUserRequests(userId));
            }
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("books", masterDataService.searchBooks(null, null, null, PageRequest.of(0, 200)).getContent());
        } catch (Exception ignored) {}
        model.addAttribute("pageTitle", "Lịch Sử Bán Sách Cũ - Trade-in");
        return "trade-in/my-requests";
    }

    /**
     * Trang form gửi yêu cầu bán sách cũ (FR-TRD-01, FR-TRD-02, FR-TRD-03)
     * URL: /trade-in/new
     */
    @GetMapping("/trade-in/new")
    public String newTradeInForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("books", masterDataService.searchBooks(null, null, null, PageRequest.of(0, 200)).getContent());
            if (userDetails != null && userDetails.getUser() != null) {
                model.addAttribute("currentUser", userDetails.getUser());
            }
        } catch (Exception ignored) {}
        model.addAttribute("pageTitle", "Đăng Ký Bán Sách Cũ - Book4Life Trade-in");
        return "trade-in/form";
    }

    /**
     * Trang săn sách - khách hàng đăng ký thông báo khi có hàng (FR-WSH-01, FR-WSH-03)
     * URL: /my-wishlist?userId=1
     */
    @GetMapping("/my-wishlist")
    public String myWishlist(
            @RequestParam(name = "userId", required = false) Long userId,
            Model model) {
        try {
            if (userId != null) {
                model.addAttribute("wishlists", wishlistService.getUserWishlists(userId));
            }
            model.addAttribute("branches", branchService.getAllActiveBranches());
            model.addAttribute("books", masterDataService.searchBooks(null, null, null, PageRequest.of(0, 200)).getContent());
        } catch (Exception ignored) {}
        model.addAttribute("pageTitle", "Danh Sách Săn Sách - Book4Life");
        return "wishlist/my-wishlist";
    }

    // =================================================================
    // NHÓM 5: LUÂN CHUYỂN KHO LIÊN CHI NHÁNH (Internal Stock Transfer)
    // =================================================================

    /**
     * Trang quản lý luân chuyển kho liên chi nhánh (FR-TRF-01, FR-TRF-03, FR-TRF-04)
     * URL: /admin/transfers
     */
    @GetMapping("/admin/transfers")
    public String transferList(Model model) {
        try {
            model.addAttribute("transfers", transferService.getAllTransfersForAdmin());
            model.addAttribute("stats", transferService.getTransferStatistics());
            model.addAttribute("branches", branchService.getAllActiveBranches());
        } catch (Exception ignored) {}
        model.addAttribute("activeMenu", "transfers");
        model.addAttribute("pageTitle", "Luân Chuyển Kho Liên Chi Nhánh (Nhóm 5) - Book4Life");
        return "admin/transfers/list";
    }

    // =================================================================
    // NHÓM 7: QUẢN LÝ KHUYẾN MÃI & MÃ GIẢM GIÁ (Coupons & Discounts)
    // =================================================================

    /**
     * Trang quản lý mã giảm giá đa điều kiện (FR-CPN-01, FR-CPN-02, FR-CPN-03)
     * URL: /admin/coupons
     */
    @GetMapping("/admin/coupons")
    public String couponList(Model model) {
        try {
            model.addAttribute("coupons", couponRepository.findAll());
            model.addAttribute("branches", branchService.getAllActiveBranches());
        } catch (Exception ignored) {}
        model.addAttribute("activeMenu", "coupons");
        model.addAttribute("pageTitle", "Quản Lý Khuyến Mãi & Mã Giảm Giá (Nhóm 7) - Book4Life");
        return "admin/coupons/list";
    }
}

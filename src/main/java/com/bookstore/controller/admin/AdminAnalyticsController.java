package com.bookstore.controller.admin;

import com.bookstore.entity.Branch;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.SubOrderRepository;
import com.bookstore.repository.TradeInRequestRepository;
import com.bookstore.security.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class AdminAnalyticsController {

    private final SubOrderRepository subOrderRepository;
    private final BookItemRepository bookItemRepository;
    private final TradeInRequestRepository tradeInRequestRepository;
    private final BranchRepository branchRepository;

    public AdminAnalyticsController(SubOrderRepository subOrderRepository,
                                    BookItemRepository bookItemRepository,
                                    TradeInRequestRepository tradeInRequestRepository,
                                    BranchRepository branchRepository) {
        this.subOrderRepository = subOrderRepository;
        this.bookItemRepository = bookItemRepository;
        this.tradeInRequestRepository = tradeInRequestRepository;
        this.branchRepository = branchRepository;
    }

    /**
     * RBAC Báo Cáo Thống Kê Tài Chính & Vận Hành Chuỗi:
     * - ADMIN: Toàn quyền xem thống kê toàn chuỗi hoặc lọc từng chi nhánh.
     * - BRANCH_MANAGER: Bị khóa cứng xem số liệu của riêng chi nhánh mình phụ trách.
     * - Chặn: CASHIER, STAFF_GENERAL, STAFF_APPRAISER, CUSTOMER.
     */
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER')")
    public String showAnalyticsDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "branchId", required = false) Long requestedBranchId,
            Model model
    ) {
        boolean isAdmin = userDetails != null && (
                "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRoleName()) ||
                "ADMIN".equalsIgnoreCase(userDetails.getRoleName())
        );

        List<Branch> allBranches = branchRepository.findAll();
        Long effectiveBranchId = null;
        Branch currentBranch = null;

        if (isAdmin) {
            // Admin có thể xem toàn chuỗi (null) hoặc chọn 1 chi nhánh
            if (requestedBranchId != null && requestedBranchId > 0) {
                effectiveBranchId = requestedBranchId;
                currentBranch = branchRepository.findById(requestedBranchId).orElse(null);
            }
        } else {
            // Quản lý chi nhánh bị giới hạn nghiêm ngặt ở chi nhánh của họ
            effectiveBranchId = (userDetails != null) ? userDetails.getBranchId() : null;
            if (effectiveBranchId != null) {
                currentBranch = branchRepository.findById(effectiveBranchId).orElse(null);
            }
        }

        // 1. KPI Cards
        BigDecimal totalRevenue = subOrderRepository.sumCompletedRevenue(effectiveBranchId);
        long completedOrders = subOrderRepository.countCompletedOrders(effectiveBranchId);
        long totalInventory = bookItemRepository.countAvailableItems(effectiveBranchId);
        BigDecimal tradeInSpending = tradeInRequestRepository.sumTradeInSpending(effectiveBranchId);

        // 2. Phân bổ tồn kho theo độ cũ (Condition Grade)
        long countLikeNew = bookItemRepository.countAvailableByGrade(effectiveBranchId, "LIKE_NEW");
        long countVeryGood = bookItemRepository.countAvailableByGrade(effectiveBranchId, "VERY_GOOD");
        long countGood = bookItemRepository.countAvailableByGrade(effectiveBranchId, "GOOD");
        long countAcceptable = bookItemRepository.countAvailableByGrade(effectiveBranchId, "ACCEPTABLE");

        // 3. So sánh doanh thu giữa các chi nhánh (Dành cho Admin khi xem toàn chuỗi)
        List<String> branchNames = new ArrayList<>();
        List<BigDecimal> branchRevenues = new ArrayList<>();
        if (isAdmin && effectiveBranchId == null) {
            for (Branch b : allBranches) {
                branchNames.add(b.getBranchName());
                branchRevenues.add(subOrderRepository.sumCompletedRevenue(b.getId()));
            }
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("branches", allBranches);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("effectiveBranchId", effectiveBranchId);

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("totalInventory", totalInventory);
        model.addAttribute("tradeInSpending", tradeInSpending);

        model.addAttribute("countLikeNew", countLikeNew);
        model.addAttribute("countVeryGood", countVeryGood);
        model.addAttribute("countGood", countGood);
        model.addAttribute("countAcceptable", countAcceptable);

        model.addAttribute("branchNames", branchNames);
        model.addAttribute("branchRevenues", branchRevenues);

        model.addAttribute("activeMenu", "analytics");

        return "admin/analytics/dashboard";
    }
}


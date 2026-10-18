package com.bookstore.service.impl;

import com.bookstore.dto.WishlistRequestDto;
import com.bookstore.dto.WishlistResponseDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.User;
import com.bookstore.entity.Wishlist;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final BranchRepository branchRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               BookRepository bookRepository,
                               BranchRepository branchRepository) {
        this.wishlistRepository = wishlistRepository;
        this.bookRepository = bookRepository;
        this.branchRepository = branchRepository;
    }

    // Nghiệp vụ FR-WSH-01: Đăng ký "Săn sách" khi hết hàng
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WishlistResponseDto addToWishlist(WishlistRequestDto dto, User user) {
        if (user == null) {
            throw new RuntimeException("Vui lòng đăng nhập để đăng ký săn sách!");
        }

        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tựa sách với ID: " + dto.getBookId()));

        Branch preferredBranch = null;
        if (dto.getPreferredBranchId() != null) {
            preferredBranch = branchRepository.findById(dto.getPreferredBranchId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh với ID: " + dto.getPreferredBranchId()));
        }

        // Kiểm tra xem khách hàng đã đăng ký cuốn này chưa
        Wishlist wishlist = wishlistRepository.findByUserIdAndBookId(user.getId(), book.getId())
                .orElse(null);

        if (wishlist == null) {
            wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setBook(book);
        }

        wishlist.setPreferredBranch(preferredBranch);
        wishlist.setMinConditionGrade(dto.getMinConditionGrade() != null ? dto.getMinConditionGrade() : "GOOD");
        wishlist.setIsNotified(false);
        wishlist.setNotifiedAt(null);
        wishlist.setCreatedAt(LocalDateTime.now());

        Wishlist saved = wishlistRepository.save(wishlist);
        return mapToDto(saved);
    }

    // Nghiệp vụ FR-WSH-03: Quản lý danh sách Săn sách cá nhân
    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponseDto> getUserWishlists(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromWishlist(Long wishlistId, Long userId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục săn sách với ID: " + wishlistId));

        if (!wishlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa mục săn sách này!");
        }

        wishlistRepository.delete(wishlist);
    }

    // Nghiệp vụ FR-WSH-02: Tự động quét và Bắn Email thông báo có hàng khi sách về kho
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndTriggerRestockAlerts(BookItem newItem) {
        if (newItem == null || newItem.getBook() == null) {
            return 0;
        }

        Long bookId = newItem.getBook().getId();
        List<Wishlist> pendingWishlists = wishlistRepository.findByBookIdAndIsNotifiedFalse(bookId);

        int notifiedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Wishlist wishlist : pendingWishlists) {
            // 1. Kiểm tra ràng buộc chi nhánh nếu có đăng ký chi nhánh cụ thể
            if (wishlist.getPreferredBranch() != null && newItem.getBranch() != null) {
                if (!wishlist.getPreferredBranch().getId().equals(newItem.getBranch().getId())) {
                    continue; // Bỏ qua nếu khác chi nhánh khách chọn
                }
            }

            // 2. Kiểm tra độ cũ đáp ứng điều kiện tối thiểu
            if (!isConditionGradeAcceptable(newItem.getConditionGrade(), wishlist.getMinConditionGrade())) {
                continue; // Bỏ qua nếu độ cũ thấp hơn kỳ vọng của khách
            }

            // 3. Đánh dấu đã bắn thông báo
            wishlist.setIsNotified(true);
            wishlist.setNotifiedAt(now);
            wishlistRepository.save(wishlist);
            notifiedCount++;

            logger.info("Restock Alert kích hoạt: Gửi thông báo tới User [{}] cho cuốn sách [{}] tại chi nhánh [{}]",
                    wishlist.getUser().getEmail(),
                    newItem.getBook().getTitle(),
                    newItem.getBranch() != null ? newItem.getBranch().getBranchName() : "Toàn chuỗi");
        }

        return notifiedCount;
    }

    private boolean isConditionGradeAcceptable(String actualGrade, String minGrade) {
        if (minGrade == null || minGrade.isBlank()) {
            return true;
        }
        return getGradeRank(actualGrade) >= getGradeRank(minGrade);
    }

    private int getGradeRank(String grade) {
        if (grade == null) return 0;
        switch (grade.toUpperCase()) {
            case "COLLECTIBLE":
                return 5;
            case "LIKE_NEW":
                return 4;
            case "VERY_GOOD":
                return 3;
            case "GOOD":
                return 2;
            case "ACCEPTABLE":
                return 1;
            default:
                return 0;
        }
    }

    private WishlistResponseDto mapToDto(Wishlist entity) {
        WishlistResponseDto dto = new WishlistResponseDto();
        dto.setId(entity.getId());
        dto.setMinConditionGrade(entity.getMinConditionGrade());
        dto.setIsNotified(entity.getIsNotified());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setNotifiedAt(entity.getNotifiedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserEmail(entity.getUser().getEmail());
        }

        if (entity.getBook() != null) {
            Book book = entity.getBook();
            dto.setBookId(book.getId());
            dto.setBookTitle(book.getTitle());
            dto.setCoverImageUrl(book.getCoverImageUrl());
            if (book.getAuthor() != null) {
                dto.setAuthorName(book.getAuthor().getName());
            }
        }

        if (entity.getPreferredBranch() != null) {
            dto.setPreferredBranchId(entity.getPreferredBranch().getId());
            dto.setPreferredBranchName(entity.getPreferredBranch().getBranchName());
        }

        return dto;
    }
}

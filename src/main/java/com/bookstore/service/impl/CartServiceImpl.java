package com.bookstore.service.impl;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.CartItemDto;
import com.bookstore.dto.CouponValidateRequestDto;
import com.bookstore.dto.CouponValidateResponseDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.BookItem;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.CartService;
import com.bookstore.service.CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    public static final String SESSION_CART_KEY = "BOOKSTORE_SESSION_CART";
    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200000"); // >= 200k: Freeship 100%
    public static final BigDecimal DISCOUNT_SHIPPING_THRESHOLD = new BigDecimal("100000"); // 100k - 199k: Giảm 40% phí ship (còn 15k)
    public static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("25000"); // < 100k: Phí chuẩn 25k
    public static final BigDecimal TIER2_SHIPPING_FEE = new BigDecimal("15000"); // Phí ưu đãi 15k

    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;
    private final CouponService couponService;

    public CartServiceImpl(BookRepository bookRepository,
                           BookItemRepository bookItemRepository,
                           CouponService couponService) {
        this.bookRepository = bookRepository;
        this.bookItemRepository = bookItemRepository;
        this.couponService = couponService;
    }

    @Override
    public CartDto getCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        refreshBestPriceRecommendations(cart);
        updateShippingAndTotals(cart);
        return cart;
    }

    @Override
    public CartDto addToCart(Long bookId, Long bookItemId, int quantity, HttpSession session) {
        CartDto cart = getCart(session);
        int qtyToAdd = Math.max(1, quantity);

        Long effectiveBookId = bookId;
        Long effectiveBookItemId = bookItemId;
        String title = "Sách Tuyển Chọn";
        String isbn = "";
        String authorName = "Nhiều tác giả";
        String categoryName = "Văn học";
        String coverImageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=350&q=80";
        String conditionGrade = "LIKE_NEW";
        Long branchId = null;
        String branchName = "Kho Tổng TP.HCM";
        BigDecimal price = new BigDecimal("75000");

        if (effectiveBookItemId != null) {
            Optional<BookItem> optItem = bookItemRepository.findById(effectiveBookItemId);
            if (optItem.isPresent()) {
                BookItem item = optItem.get();
                effectiveBookId = item.getBook().getId();
                title = item.getBook().getTitle();
                isbn = item.getBook().getIsbn();
                authorName = item.getBook().getAuthor() != null ? item.getBook().getAuthor().getName() : "";
                categoryName = item.getBook().getCategory() != null ? item.getBook().getCategory().getName() : "";
                coverImageUrl = item.getBook().getCoverImageUrl() != null ? item.getBook().getCoverImageUrl() : coverImageUrl;
                conditionGrade = item.getConditionGrade();
                branchId = item.getBranch() != null ? item.getBranch().getId() : null;
                branchName = item.getBranch() != null ? item.getBranch().getBranchName() : "Kho Tổng";
                price = item.getSellingPrice();
            }
        } else if (effectiveBookId != null) {
            Optional<Book> optBook = bookRepository.findById(effectiveBookId);
            if (optBook.isPresent()) {
                Book b = optBook.get();
                title = b.getTitle();
                isbn = b.getIsbn();
                authorName = b.getAuthor() != null ? b.getAuthor().getName() : "";
                categoryName = b.getCategory() != null ? b.getCategory().getName() : "";
                coverImageUrl = b.getCoverImageUrl() != null ? b.getCoverImageUrl() : coverImageUrl;

                // Look for available physical item - prioritize best price
                List<BookItem> items = bookItemRepository.findAvailableItemsByBookId(effectiveBookId);
                if (!items.isEmpty()) {
                    BookItem bestItem = items.stream()
                            .filter(i -> i.getSellingPrice() != null && i.getStockQuantity() != null && i.getStockQuantity() > 0)
                            .min(java.util.Comparator.comparing(BookItem::getSellingPrice))
                            .orElse(items.get(0));

                    effectiveBookItemId = bestItem.getId();
                    conditionGrade = bestItem.getConditionGrade();
                    branchId = bestItem.getBranch() != null ? bestItem.getBranch().getId() : null;
                    branchName = bestItem.getBranch() != null ? bestItem.getBranch().getBranchName() : branchName;
                    price = bestItem.getSellingPrice() != null ? bestItem.getSellingPrice() : b.getOriginalPrice();
                } else {
                    price = b.getOriginalPrice() != null ? b.getOriginalPrice() : price;
                }
            }
        }

        // Check if item already exists in cart by bookId
        boolean found = false;
        for (CartItemDto item : cart.getItems()) {
            if (item.getBookId() != null && item.getBookId().equals(effectiveBookId)) {
                item.setQuantity(item.getQuantity() + qtyToAdd);
                found = true;
                break;
            }
        }

        if (!found) {
            CartItemDto newItem = new CartItemDto(
                    effectiveBookId,
                    effectiveBookItemId,
                    title,
                    isbn,
                    authorName,
                    categoryName,
                    coverImageUrl,
                    conditionGrade,
                    branchId,
                    branchName,
                    price,
                    qtyToAdd
            );
            cart.getItems().add(newItem);
        }

        refreshBestPriceRecommendations(cart);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto updateQuantity(Long bookId, int quantity, HttpSession session) {
        CartDto cart = getCart(session);
        if (quantity <= 0) {
            return removeFromCart(bookId, session);
        }

        for (CartItemDto item : cart.getItems()) {
            if (item.getBookId() != null && item.getBookId().equals(bookId)) {
                item.setQuantity(quantity);
                break;
            }
        }

        refreshBestPriceRecommendations(cart);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto removeFromCart(Long bookId, HttpSession session) {
        CartDto cart = getCart(session);
        cart.getItems().removeIf(item -> item.getBookId() != null && item.getBookId().equals(bookId));
        refreshBestPriceRecommendations(cart);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public void clearCart(HttpSession session) {
        CartDto cart = new CartDto();
        session.setAttribute(SESSION_CART_KEY, cart);
    }

    @Override
    public int getTotalQuantity(HttpSession session) {
        return getCart(session).getTotalQuantity();
    }

    @Override
    public CartDto switchItemBranch(Long bookId, Long targetBookItemId, HttpSession session) {
        CartDto cart = getCart(session);
        if (bookId == null || targetBookItemId == null || cart.isEmpty()) {
            return cart;
        }

        CartItemDto targetItem = cart.getItems().stream()
                .filter(i -> bookId.equals(i.getBookId()))
                .findFirst()
                .orElse(null);

        if (targetItem != null) {
            Optional<BookItem> optBi = bookItemRepository.findById(targetBookItemId);
            if (optBi.isPresent()) {
                BookItem bi = optBi.get();
                if ("AVAILABLE".equalsIgnoreCase(bi.getStatus()) && bi.getStockQuantity() != null && bi.getStockQuantity() > 0) {
                    targetItem.setBookItemId(bi.getId());
                    targetItem.setPrice(bi.getSellingPrice());
                    targetItem.setConditionGrade(bi.getConditionGrade());
                    targetItem.setBranchId(bi.getBranch() != null ? bi.getBranch().getId() : null);
                    targetItem.setBranchName(bi.getBranch() != null ? bi.getBranch().getBranchName() : "Kho Chi Nhánh");
                }
            }
        }

        refreshBestPriceRecommendations(cart);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto optimizeCartForBestPrice(HttpSession session) {
        CartDto cart = getCart(session);
        if (cart.isEmpty()) {
            return cart;
        }

        refreshBestPriceRecommendations(cart);
        for (CartItemDto item : cart.getItems()) {
            if (item.hasBetterPrice() && item.getBestPriceBookItemId() != null) {
                Optional<BookItem> optBi = bookItemRepository.findById(item.getBestPriceBookItemId());
                if (optBi.isPresent()) {
                    BookItem bi = optBi.get();
                    if ("AVAILABLE".equalsIgnoreCase(bi.getStatus()) && bi.getStockQuantity() != null && bi.getStockQuantity() > 0) {
                        item.setBookItemId(bi.getId());
                        item.setPrice(bi.getSellingPrice());
                        item.setConditionGrade(bi.getConditionGrade());
                        item.setBranchId(bi.getBranch() != null ? bi.getBranch().getId() : null);
                        item.setBranchName(bi.getBranch() != null ? bi.getBranch().getBranchName() : "Kho Chi Nhánh");
                    }
                }
            }
        }

        refreshBestPriceRecommendations(cart);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    private void refreshBestPriceRecommendations(CartDto cart) {
        if (cart == null || cart.isEmpty()) return;
        for (CartItemDto item : cart.getItems()) {
            if (item.getBookId() == null) continue;
            List<BookItem> availableItems = bookItemRepository.findAvailableItemsByBookId(item.getBookId());
            if (availableItems == null || availableItems.isEmpty()) {
                item.setBestPrice(null);
                item.setBestPriceBranchName(null);
                item.setBestPriceBookItemId(null);
                item.setSavings(BigDecimal.ZERO);
                continue;
            }
            BookItem lowestItem = availableItems.stream()
                    .filter(bi -> bi.getSellingPrice() != null && bi.getStockQuantity() != null && bi.getStockQuantity() > 0)
                    .min(java.util.Comparator.comparing(BookItem::getSellingPrice))
                    .orElse(null);

            if (lowestItem != null && item.getPrice() != null && lowestItem.getSellingPrice().compareTo(item.getPrice()) < 0) {
                item.setBestPrice(lowestItem.getSellingPrice());
                item.setBestPriceBranchName(lowestItem.getBranch() != null ? lowestItem.getBranch().getBranchName() : "");
                item.setBestPriceBookItemId(lowestItem.getId());
                BigDecimal diff = item.getPrice().subtract(lowestItem.getSellingPrice());
                item.setSavings(diff.multiply(BigDecimal.valueOf(item.getQuantity())));
            } else {
                item.setBestPrice(null);
                item.setBestPriceBranchName(null);
                item.setBestPriceBookItemId(null);
                item.setSavings(BigDecimal.ZERO);
            }
        }
    }

    @Override
    public CartDto applyCoupon(String code, HttpSession session) {
        CartDto cart = getCart(session);
        if (code == null || code.trim().isEmpty() || cart.isEmpty()) {
            cart.setLastCouponSuccess(false);
            cart.setLastCouponMessage("Vui lòng nhập mã ưu đãi hợp lệ!");
            updateShippingAndTotals(cart);
            return cart;
        }

        try {
            CouponValidateRequestDto request = new CouponValidateRequestDto();
            request.setCode(code.trim().toUpperCase());
            List<CouponValidateRequestDto.CartItemDto> cItems = new ArrayList<>();
            for (CartItemDto ci : cart.getItems()) {
                CouponValidateRequestDto.CartItemDto itemDto = new CouponValidateRequestDto.CartItemDto();
                itemDto.setPrice(ci.getPrice());
                itemDto.setConditionGrade(ci.getConditionGrade());
                itemDto.setQuantity(ci.getQuantity());
                cItems.add(itemDto);
            }
            request.setCartItems(cItems);

            CouponValidateResponseDto res = couponService.applyCoupon(request);
            if (res.isValid()) {
                cart.setLastCouponSuccess(true);
                cart.setLastCouponMessage(res.getMessage());

                if (res.isShippingCoupon()) {
                    // Áp dụng vào slot Mã Vận Chuyển
                    cart.setShippingCouponCode(res.getCode());
                    cart.setShippingCouponType(res.getDiscountType());
                    cart.setShippingDiscountAmount(res.getShippingDiscountAmount());
                } else {
                    // Áp dụng vào slot Mã Giảm Giá Đơn Hàng
                    cart.setOrderCouponCode(res.getCode());
                    cart.setOrderCouponType(res.getDiscountType());
                    cart.setDiscountAmount(res.getDiscountAmount());
                }
            } else {
                cart.setLastCouponSuccess(false);
                cart.setLastCouponMessage(res.getMessage());
            }
        } catch (Exception e) {
            cart.setLastCouponSuccess(false);
            cart.setLastCouponMessage("Không thể áp dụng mã: " + e.getMessage());
        }

        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto removeCoupon(HttpSession session) {
        CartDto cart = getCart(session);
        cart.setOrderCouponCode(null);
        cart.setOrderCouponType(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setShippingCouponCode(null);
        cart.setShippingCouponType(null);
        cart.setShippingDiscountAmount(BigDecimal.ZERO);
        cart.setShippingPromoMessage(null);
        cart.setLastCouponMessage("Đã gỡ bỏ toàn bộ mã ưu đãi.");
        cart.setLastCouponSuccess(true);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto removeShippingCoupon(HttpSession session) {
        CartDto cart = getCart(session);
        cart.setShippingCouponCode(null);
        cart.setShippingCouponType(null);
        cart.setShippingDiscountAmount(BigDecimal.ZERO);
        cart.setLastCouponMessage("Đã gỡ mã vận chuyển.");
        cart.setLastCouponSuccess(true);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto removeOrderCoupon(HttpSession session) {
        CartDto cart = getCart(session);
        cart.setOrderCouponCode(null);
        cart.setOrderCouponType(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setLastCouponMessage("Đã gỡ mã giảm giá đơn hàng.");
        cart.setLastCouponSuccess(true);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    private void updateShippingAndTotals(CartDto cart) {
        if (cart.isEmpty()) {
            cart.setShippingFee(BigDecimal.ZERO);
            cart.setBaseShippingFee(BigDecimal.ZERO);
            cart.setShippingDiscountAmount(BigDecimal.ZERO);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setOrderCouponCode(null);
            cart.setOrderCouponType(null);
            cart.setShippingCouponCode(null);
            cart.setShippingCouponType(null);
            cart.setShippingPromoMessage(null);
            return;
        }

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal baseShipping;
        String promoMsg;

        // Công thức tính phí ưu đãi bậc thang (Tiered Shipping Formula)
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            baseShipping = BigDecimal.ZERO;
            promoMsg = "Đơn từ 200.000₫: Bạn được MIỄN PHÍ VẬN CHUYỂN 100% toàn chuỗi! 🎉";
        } else if (subtotal.compareTo(DISCOUNT_SHIPPING_THRESHOLD) >= 0) {
            baseShipping = TIER2_SHIPPING_FEE; // 15.000₫
            BigDecimal remaining = FREE_SHIPPING_THRESHOLD.subtract(subtotal);
            promoMsg = "Đơn từ 100.000₫: Ưu đãi GIẢM 40% phí ship (còn 15.000₫)! Mua thêm " + formatVnd(remaining) + " để nhận Freeship 100%.";
        } else {
            baseShipping = STANDARD_SHIPPING_FEE; // 25.000₫
            BigDecimal remaining = DISCOUNT_SHIPPING_THRESHOLD.subtract(subtotal);
            promoMsg = "Phí giao hàng tiêu chuẩn 25.000₫. Mua thêm " + formatVnd(remaining) + " để được giảm phí ship còn 15.000₫!";
        }

        cart.setBaseShippingFee(baseShipping);

        // Áp dụng Mã Freeship / Giảm ship nếu có
        if (cart.hasShippingCoupon()) {
            BigDecimal couponShipDiscount = cart.getShippingDiscountAmount();
            if (couponShipDiscount == null || couponShipDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                couponShipDiscount = STANDARD_SHIPPING_FEE;
            }

            if (baseShipping.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal effectiveDiscount = baseShipping.min(couponShipDiscount);
                cart.setShippingDiscountAmount(effectiveDiscount);
                cart.setShippingFee(baseShipping.subtract(effectiveDiscount).max(BigDecimal.ZERO));
                promoMsg = "Đã áp dụng mã Freeship [" + cart.getShippingCouponCode() + "]: Giảm " + formatVnd(effectiveDiscount) + " phí vận chuyển! 🚀";
            } else {
                cart.setShippingFee(BigDecimal.ZERO);
                cart.setShippingDiscountAmount(BigDecimal.ZERO);
                promoMsg = "Đơn hàng đã được Freeship 100% tự động theo chính sách đơn từ 200.000₫! 🎉";
            }
        } else {
            cart.setShippingDiscountAmount(BigDecimal.ZERO);
            cart.setShippingFee(baseShipping);
        }

        // Đảm bảo mức giảm giá đơn hàng không vượt quá tổng tiền hàng
        if (cart.getDiscountAmount().compareTo(subtotal) > 0) {
            cart.setDiscountAmount(subtotal);
        }

        cart.setShippingPromoMessage(promoMsg);
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0₫";
        return String.format("%,d₫", amount.longValue()).replace(',', '.');
    }
}

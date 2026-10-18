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
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200000");
    private static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("25000");

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

                // Look for available physical item
                List<BookItem> items = bookItemRepository.findAvailableItemsByBookId(effectiveBookId);
                if (!items.isEmpty()) {
                    BookItem firstItem = items.get(0);
                    effectiveBookItemId = firstItem.getId();
                    conditionGrade = firstItem.getConditionGrade();
                    branchName = firstItem.getBranch() != null ? firstItem.getBranch().getBranchName() : branchName;
                    price = firstItem.getSellingPrice() != null ? firstItem.getSellingPrice() : b.getOriginalPrice();
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
                    branchName,
                    price,
                    qtyToAdd
            );
            cart.getItems().add(newItem);
        }

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

        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto removeFromCart(Long bookId, HttpSession session) {
        CartDto cart = getCart(session);
        cart.getItems().removeIf(item -> item.getBookId() != null && item.getBookId().equals(bookId));
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
    public CartDto applyCoupon(String code, HttpSession session) {
        CartDto cart = getCart(session);
        if (code == null || code.trim().isEmpty() || cart.isEmpty()) {
            cart.setCouponCode(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
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
                cart.setCouponCode(code.trim().toUpperCase());
                cart.setDiscountAmount(res.getDiscountAmount());
            } else {
                cart.setCouponCode(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            cart.setCouponCode(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
        }

        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    @Override
    public CartDto removeCoupon(HttpSession session) {
        CartDto cart = getCart(session);
        cart.setCouponCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        updateShippingAndTotals(cart);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    private void updateShippingAndTotals(CartDto cart) {
        if (cart.isEmpty()) {
            cart.setShippingFee(BigDecimal.ZERO);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setCouponCode(null);
            return;
        }

        BigDecimal subtotal = cart.getSubtotal();
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            cart.setShippingFee(BigDecimal.ZERO);
        } else {
            cart.setShippingFee(STANDARD_SHIPPING_FEE);
        }
    }
}

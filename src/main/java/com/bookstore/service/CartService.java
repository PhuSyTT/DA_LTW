package com.bookstore.service;

import com.bookstore.dto.CartDto;
import jakarta.servlet.http.HttpSession;

public interface CartService {

    CartDto getCart(HttpSession session);

    CartDto addToCart(Long bookId, Long bookItemId, int quantity, HttpSession session);

    CartDto updateQuantity(Long bookId, int quantity, HttpSession session);

    CartDto removeFromCart(Long bookId, HttpSession session);

    void clearCart(HttpSession session);

    int getTotalQuantity(HttpSession session);

    CartDto applyCoupon(String code, HttpSession session);

    CartDto removeCoupon(HttpSession session);

    CartDto switchItemBranch(Long bookId, Long targetBookItemId, HttpSession session);

    CartDto optimizeCartForBestPrice(HttpSession session);
}


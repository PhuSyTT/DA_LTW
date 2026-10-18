package com.bookstore.service;

import com.bookstore.dto.WishlistRequestDto;
import com.bookstore.dto.WishlistResponseDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.User;

import java.util.List;

public interface WishlistService {

    WishlistResponseDto addToWishlist(WishlistRequestDto dto, User user);

    List<WishlistResponseDto> getUserWishlists(Long userId);

    void removeFromWishlist(Long wishlistId, Long userId);

    int checkAndTriggerRestockAlerts(BookItem newItem);
}

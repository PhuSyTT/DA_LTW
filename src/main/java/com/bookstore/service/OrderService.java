package com.bookstore.service;

import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.MasterOrder;
import com.bookstore.entity.User;

public interface OrderService {
    MasterOrder processCheckout(CheckoutRequestDto request, User user);
}
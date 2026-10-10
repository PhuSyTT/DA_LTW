package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface TradeInService {

    AutoEstimateResponseDto calculateAutoEstimate(AutoEstimateRequestDto request);

    TradeInResponseDto createTradeInRequest(TradeInCreateDto dto, User user);

    TradeInResponseDto reviewTradeInRequest(Long requestId, TradeInReviewDto dto, User staff);

    TradeInResponseDto customerRespondToOffer(Long requestId, TradeInCustomerResponseDto dto, User user);

    TradeInResponseDto confirmPhysicalReceiptAndStock(Long requestId, String shelfLocation, User staff);

    List<TradeInResponseDto> getRequestsByBranch(Long branchId, String status);

    List<TradeInResponseDto> getUserRequests(Long userId);

    TradeInResponseDto getRequestById(Long requestId);
}

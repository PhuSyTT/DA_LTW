package com.bookstore.service;

import com.bookstore.dto.FulfillmentStatsDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.dto.UpdateSubOrderStatusDto;
import com.bookstore.entity.User;

import java.util.List;

public interface OrderFulfillmentService {

    List<SubOrderDto> getOrdersByBranch(Long branchId, String status);

    SubOrderDto getSubOrderDetail(Long subOrderId);

    SubOrderDto updateSubOrderStatus(Long subOrderId, UpdateSubOrderStatusDto dto, User staff);

    FulfillmentStatsDto getBranchFulfillmentStats(Long branchId);

    void cancelAndRestockSubOrder(Long subOrderId, String reason, User staff);
}

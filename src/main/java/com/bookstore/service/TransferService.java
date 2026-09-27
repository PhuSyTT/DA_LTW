package com.bookstore.service;

import com.bookstore.dto.TransferRequestDto;
import com.bookstore.dto.TransferStatsDto;
import com.bookstore.entity.StockTransfer;
import com.bookstore.entity.User;
import java.util.List;

public interface TransferService {
    
    // Tạo phiếu luân chuyển (Nghiệp vụ 1)
    StockTransfer createTransferRequest(TransferRequestDto request, User manager);
    
    // Xuất kho tại chi nhánh nguồn (Nghiệp vụ 2)
    void exportTransfer(Long transferId);
    
    // Nhận hàng tại chi nhánh đích (Nghiệp vụ 3)
    void receiveTransfer(Long transferId);
    
    List<StockTransfer> getAllTransfersForAdmin();
    TransferStatsDto getTransferStatistics();
}
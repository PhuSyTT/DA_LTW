package com.bookstore.service.impl;

import com.bookstore.dto.TransferRequestDto;
import com.bookstore.dto.TransferStatsDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.StockTransfer;
import com.bookstore.entity.StockTransferItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.StockTransferRepository;
import com.bookstore.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferServiceImpl implements TransferService {

    private final StockTransferRepository transferRepository;
    private final BookItemRepository bookItemRepository;
    private final BranchRepository branchRepository;

    public TransferServiceImpl(StockTransferRepository transferRepository, 
                               BookItemRepository bookItemRepository,
                               BranchRepository branchRepository) {
        this.transferRepository = transferRepository;
        this.bookItemRepository = bookItemRepository;
        this.branchRepository = branchRepository;
    }

    // Nghiệp vụ 1: Tạo phiếu và Kiểm tra ràng buộc
    @Transactional(rollbackFor = Exception.class)
    @Override
    public StockTransfer createTransferRequest(TransferRequestDto request, User manager) {
        Branch fromBranch = branchRepository.findById(request.getFromBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh nguồn"));
        Branch toBranch = branchRepository.findById(request.getToBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh đích"));

        StockTransfer transfer = new StockTransfer();
        transfer.setTransferCode("TRF-" + System.currentTimeMillis());
        transfer.setFromBranch(fromBranch);
        transfer.setToBranch(toBranch);
        transfer.setRequestedByUser(manager);
        transfer.setReason(request.getReason());
        transfer.setStatus("PENDING_APPROVAL");
        transfer.setCreatedAt(LocalDateTime.now());

        List<StockTransferItem> transferItems = new ArrayList<>();

        for (TransferRequestDto.TransferItemDto itemDto : request.getItems()) {
            BookItem bookItem = bookItemRepository.findById(itemDto.getBookItemId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

            // Áp dụng Quy tắc nghiệp vụ 3.3: Chặn luân chuyển sách độc bản < 6 tháng
            long daysInStock = ChronoUnit.DAYS.between(bookItem.getImportDate(), LocalDateTime.now());
            if (daysInStock < 180 && bookItem.getStockQuantity() <= 1) {
                throw new RuntimeException("Vi phạm ràng buộc: Sách " + bookItem.getSkuBarcode() + 
                                           " là độc bản và mới nhập kho dưới 6 tháng!");
            }

            if (bookItem.getStockQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Sách " + bookItem.getSkuBarcode() + " không đủ số lượng tồn kho");
            }

            StockTransferItem transferItem = new StockTransferItem();
            transferItem.setStockTransfer(transfer);
            transferItem.setBookItem(bookItem);
            transferItem.setQuantity(itemDto.getQuantity());
            transferItems.add(transferItem);
        }

        transfer.setItems(transferItems);
        return transferRepository.save(transfer);
    }

    // Nghiệp vụ 2: Xác nhận xuất kho
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void exportTransfer(Long transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        
        if (!transfer.getStatus().equals("PENDING_APPROVAL")) {
            throw new RuntimeException("Phiếu không ở trạng thái hợp lệ để xuất kho");
        }

        // Tạm trừ tồn kho tại chi nhánh nguồn
        for (StockTransferItem item : transfer.getItems()) {
            BookItem bookItem = item.getBookItem();
            bookItem.setStockQuantity(bookItem.getStockQuantity() - item.getQuantity());
            bookItemRepository.save(bookItem);
        }

        transfer.setStatus("IN_TRANSIT");
        transferRepository.save(transfer);
    }

    // Nghiệp vụ 3: Xác nhận nhận hàng tại đích
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void receiveTransfer(Long transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        if (!transfer.getStatus().equals("IN_TRANSIT")) {
            throw new RuntimeException("Hàng chưa được xuất kho");
        }

        // Chuyển quyền sở hữu BookItem sang chi nhánh đích và cộng kho
        for (StockTransferItem item : transfer.getItems()) {
            BookItem bookItem = item.getBookItem();
            bookItem.setBranch(transfer.getToBranch()); // Đổi chi nhánh lưu trữ
            bookItem.setStockQuantity(bookItem.getStockQuantity() + item.getQuantity());
            bookItemRepository.save(bookItem);
        }

        transfer.setStatus("COMPLETED");
        transfer.setCompletedAt(LocalDateTime.now());
        transferRepository.save(transfer);
    }
    
    // Phục vụ FR-TRF-04: Lấy danh sách toàn bộ phiếu
    @Override
    public List<StockTransfer> getAllTransfersForAdmin() {
        // Sắp xếp phiếu mới nhất lên đầu (id DESC)
        return transferRepository.findAll().stream()
                .sorted(Comparator.comparing(StockTransfer::getId, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    // Tính toán thống kê trung bình
    @Override
    public TransferStatsDto getTransferStatistics() {
        List<StockTransfer> allTransfers = transferRepository.findAll();
        long total = allTransfers.size();
        
        List<StockTransfer> completed = allTransfers.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .collect(Collectors.toList());
        
        long completedCount = completed.size();
        
        // Tính thời gian vận chuyển trung bình (tính bằng Giờ)
        double avgHours = 0;
        if (completedCount > 0) {
            long totalMinutes = 0;
            for (StockTransfer t : completed) {
                if (t.getCreatedAt() != null && t.getCompletedAt() != null) {
                    totalMinutes += ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getCompletedAt());
                }
            }
            avgHours = (double) totalMinutes / 60.0 / completedCount;
        }
        
        // Tính tỷ lệ thất thoát (Giả sử dựa trên số phiếu bị REJECTED)
        long lostCount = allTransfers.stream()
                .filter(t -> "REJECTED".equals(t.getStatus()))
                .count();
            
        double lossRate = total > 0 ? ((double) lostCount / total) * 100 : 0;
        
        TransferStatsDto stats = new TransferStatsDto();
        stats.setTotalTransfers(total);
        stats.setCompletedTransfers(completedCount);
        // Làm tròn 1 chữ số thập phân
        stats.setAverageTransitTimeHours(Math.round(avgHours * 10.0) / 10.0);
        stats.setLossRatePercentage(Math.round(lossRate * 10.0) / 10.0);
        
        return stats;
    }
}
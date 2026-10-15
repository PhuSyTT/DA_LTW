package com.bookstore.service.impl;

import com.bookstore.dto.FulfillmentStatsDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.dto.SubOrderItemDto;
import com.bookstore.dto.UpdateSubOrderStatusDto;
import com.bookstore.entity.*;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.SubOrderRepository;
import com.bookstore.service.OrderFulfillmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderFulfillmentServiceImpl implements OrderFulfillmentService {

    private final SubOrderRepository subOrderRepository;
    private final BookItemRepository bookItemRepository;
    private final BranchRepository branchRepository;

    public OrderFulfillmentServiceImpl(SubOrderRepository subOrderRepository,
                                      BookItemRepository bookItemRepository,
                                      BranchRepository branchRepository) {
        this.subOrderRepository = subOrderRepository;
        this.bookItemRepository = bookItemRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubOrderDto> getOrdersByBranch(Long branchId, String status) {
        List<SubOrder> subOrders;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            subOrders = subOrderRepository.findByBranchIdAndStatusOrderByIdDesc(branchId, status.toUpperCase());
        } else {
            subOrders = subOrderRepository.findByBranchIdOrderByIdDesc(branchId);
        }

        return subOrders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubOrderDto getSubOrderDetail(Long subOrderId) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng con với ID: " + subOrderId));
        return mapToDto(subOrder);
    }

    // Nghiệp vụ FR-FUL-02: Quy trình xử lý đóng gói & Giao vận
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubOrderDto updateSubOrderStatus(Long subOrderId, UpdateSubOrderStatusDto dto, User staff) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng con với ID: " + subOrderId));

        String targetStatus = dto.getStatus().toUpperCase();

        if ("CANCELLED".equals(targetStatus)) {
            // Nghiệp vụ FR-FUL-04: Xử lý hủy đơn và hoàn kho tự động
            cancelAndRestockSubOrder(subOrderId, dto.getNote(), staff);
            subOrder = subOrderRepository.findById(subOrderId).get();
            return mapToDto(subOrder);
        }

        // Kiểm tra luồng chuyển trạng thái hợp lệ
        String currentStatus = subOrder.getStatus();
        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new RuntimeException("Đơn hàng đã ở trạng thái kết thúc (" + currentStatus + "), không thể thay đổi!");
        }

        switch (targetStatus) {
            case "PACKING":
                // Nhân viên bắt đầu nhặt sách trên kệ và đóng gói
                subOrder.setStatus("PACKING");
                break;

            case "SHIPPED":
                // Gắn mã vận đơn đối tác vận chuyển và chuyển trạng thái
                if (dto.getTrackingNumber() != null && !dto.getTrackingNumber().isBlank()) {
                    subOrder.setTrackingNumber(dto.getTrackingNumber().trim());
                } else if (subOrder.getTrackingNumber() == null || subOrder.getTrackingNumber().isBlank()) {
                    subOrder.setTrackingNumber("VNPOST-" + System.currentTimeMillis());
                }
                subOrder.setStatus("SHIPPED");
                break;

            case "COMPLETED":
                subOrder.setStatus("COMPLETED");
                break;

            default:
                throw new RuntimeException("Trạng thái không hợp lệ: " + targetStatus);
        }

        subOrder.setUpdatedAt(LocalDateTime.now());
        SubOrder saved = subOrderRepository.save(subOrder);
        return mapToDto(saved);
    }

    // Nghiệp vụ FR-FUL-04: Xử lý Hủy đơn & Hoàn kho tự động
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAndRestockSubOrder(Long subOrderId, String reason, User staff) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng con với ID: " + subOrderId));

        if ("CANCELLED".equals(subOrder.getStatus())) {
            throw new RuntimeException("Đơn hàng đã bị hủy trước đó!");
        }

        if ("COMPLETED".equals(subOrder.getStatus())) {
            throw new RuntimeException("Đơn hàng đã hoàn thành, không thể hủy đơn!");
        }

        // Hoàn lại số lượng tồn kho cho từng mặt hàng trong đơn
        if (subOrder.getItems() != null) {
            for (SubOrderItem item : subOrder.getItems()) {
                BookItem bookItem = item.getBookItem();
                if (bookItem != null) {
                    int restoredQuantity = (bookItem.getStockQuantity() != null ? bookItem.getStockQuantity() : 0) + item.getQuantity();
                    bookItem.setStockQuantity(restoredQuantity);
                    bookItem.setStatus("AVAILABLE");
                    bookItemRepository.save(bookItem);
                }
            }
        }

        subOrder.setStatus("CANCELLED");
        subOrder.setUpdatedAt(LocalDateTime.now());
        subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public FulfillmentStatsDto getBranchFulfillmentStats(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh với ID: " + branchId));

        long pending = subOrderRepository.countByBranchIdAndStatus(branchId, "PENDING");
        long packing = subOrderRepository.countByBranchIdAndStatus(branchId, "PACKING");
        long shipped = subOrderRepository.countByBranchIdAndStatus(branchId, "SHIPPED");
        long completed = subOrderRepository.countByBranchIdAndStatus(branchId, "COMPLETED");
        long cancelled = subOrderRepository.countByBranchIdAndStatus(branchId, "CANCELLED");

        return new FulfillmentStatsDto(branchId, branch.getBranchName(), pending, packing, shipped, completed, cancelled);
    }

    private SubOrderDto mapToDto(SubOrder entity) {
        SubOrderDto dto = new SubOrderDto();
        dto.setId(entity.getId());
        dto.setSubOrderCode(entity.getSubOrderCode());
        dto.setBranchSubtotal(entity.getBranchSubtotal());
        dto.setBranchShippingFee(entity.getBranchShippingFee());

        BigDecimal subtotal = entity.getBranchSubtotal() != null ? entity.getBranchSubtotal() : BigDecimal.ZERO;
        BigDecimal shipping = entity.getBranchShippingFee() != null ? entity.getBranchShippingFee() : BigDecimal.ZERO;
        dto.setTotalAmount(subtotal.add(shipping));

        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getBranch() != null) {
            dto.setBranchId(entity.getBranch().getId());
            dto.setBranchName(entity.getBranch().getBranchName());
            dto.setBranchAddress(entity.getBranch().getAddress());
        }

        if (entity.getMasterOrder() != null) {
            dto.setMasterOrderId(entity.getMasterOrder().getId());
            dto.setMasterOrderCode(entity.getMasterOrder().getMasterOrderCode());
            User customer = entity.getMasterOrder().getUser();
            if (customer != null) {
                dto.setCustomerName(customer.getFullName());
                dto.setCustomerEmail(customer.getEmail());
                dto.setCustomerPhone(customer.getPhone());
            }
        }

        List<SubOrderItemDto> itemDtos = new ArrayList<>();
        if (entity.getItems() != null) {
            for (SubOrderItem item : entity.getItems()) {
                SubOrderItemDto itemDto = new SubOrderItemDto();
                itemDto.setId(item.getId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setUnitPrice(item.getUnitPrice());
                itemDto.setSubtotal(item.getSubtotal());

                BookItem bookItem = item.getBookItem();
                if (bookItem != null) {
                    itemDto.setBookItemId(bookItem.getId());
                    itemDto.setSkuBarcode(bookItem.getSkuBarcode());
                    itemDto.setConditionGrade(bookItem.getConditionGrade());
                    itemDto.setShelfLocation(bookItem.getShelfLocation());

                    Book book = bookItem.getBook();
                    if (book != null) {
                        itemDto.setBookTitle(book.getTitle());
                        if (book.getAuthor() != null) {
                            itemDto.setAuthorName(book.getAuthor().getName());
                        }
                    }
                }
                itemDtos.add(itemDto);
            }
        }
        dto.setItems(itemDtos);

        return dto;
    }
}

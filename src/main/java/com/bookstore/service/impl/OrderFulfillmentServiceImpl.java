package com.bookstore.service.impl;

import com.bookstore.dto.FulfillmentStatsDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.dto.SubOrderItemDto;
import com.bookstore.dto.UpdateSubOrderStatusDto;
import com.bookstore.entity.*;
import com.bookstore.entity.Driver;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.DriverRepository;
import com.bookstore.repository.OrderStatusHistoryRepository;
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
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final DriverRepository driverRepository;

    public OrderFulfillmentServiceImpl(SubOrderRepository subOrderRepository,
                                      BookItemRepository bookItemRepository,
                                      BranchRepository branchRepository,
                                      OrderStatusHistoryRepository orderStatusHistoryRepository,
                                      DriverRepository driverRepository) {
        this.subOrderRepository = subOrderRepository;
        this.bookItemRepository = bookItemRepository;
        this.branchRepository = branchRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.driverRepository = driverRepository;
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

        LocalDateTime now = LocalDateTime.now();

        switch (targetStatus) {
            case "CONFIRMED":
                subOrder.setStatus("CONFIRMED");
                subOrder.setConfirmedAt(now);
                break;

            case "PACKING":
                // Nhân viên bắt đầu nhặt sách trên kệ và đóng gói
                subOrder.setStatus("PACKING");
                if (subOrder.getConfirmedAt() == null) {
                    subOrder.setConfirmedAt(now);
                }
                break;

            case "SHIPPED":
                // Gắn mã vận đơn đối tác vận chuyển và chuyển trạng thái
                if (dto.getTrackingNumber() != null && !dto.getTrackingNumber().isBlank()) {
                    subOrder.setTrackingNumber(dto.getTrackingNumber().trim());
                } else if (subOrder.getTrackingNumber() == null || subOrder.getTrackingNumber().isBlank()) {
                    subOrder.setTrackingNumber("B4L-SHIP-" + System.currentTimeMillis());
                }

                // Cập nhật tài xế nếu có
                if (dto.getDriverId() != null) {
                    Driver driver = driverRepository.findById(dto.getDriverId()).orElse(null);
                    if (driver != null) {
                        subOrder.setDriver(driver);
                        subOrder.setShipperName(driver.getFullName());
                        subOrder.setShipperPhone(driver.getPhone());
                        subOrder.setShipperVehicle(driver.getVehicleType() + " - " + driver.getLicensePlate());
                        subOrder.setShippingCarrier(driver.getCarrierName());
                        driver.setActiveOrdersCount(driver.getActiveOrdersCount() != null ? driver.getActiveOrdersCount() + 1 : 1);
                        driverRepository.save(driver);
                    }
                } else {
                    if (dto.getShipperName() != null && !dto.getShipperName().isBlank()) {
                        subOrder.setShipperName(dto.getShipperName().trim());
                    }
                    if (dto.getShipperPhone() != null && !dto.getShipperPhone().isBlank()) {
                        subOrder.setShipperPhone(dto.getShipperPhone().trim());
                    }
                    if (dto.getShipperVehicle() != null && !dto.getShipperVehicle().isBlank()) {
                        subOrder.setShipperVehicle(dto.getShipperVehicle().trim());
                    }
                    if (dto.getShippingCarrier() != null && !dto.getShippingCarrier().isBlank()) {
                        subOrder.setShippingCarrier(dto.getShippingCarrier().trim());
                    }
                }

                subOrder.setDeliveryStatus("DELIVERING");
                subOrder.setEstimatedDeliveryTime(now.plusHours(2));
                if (dto.getDeliveryNotes() != null && !dto.getDeliveryNotes().isBlank()) {
                    subOrder.setDeliveryNotes(dto.getDeliveryNotes().trim());
                }

                subOrder.setStatus("SHIPPED");
                subOrder.setShippedAt(now);
                break;

            case "COMPLETED":
                subOrder.setStatus("COMPLETED");
                subOrder.setDeliveryStatus("DELIVERED");
                subOrder.setCompletedAt(now);
                if (subOrder.getDriver() != null) {
                    Driver d = subOrder.getDriver();
                    if (d.getActiveOrdersCount() != null && d.getActiveOrdersCount() > 0) {
                        d.setActiveOrdersCount(d.getActiveOrdersCount() - 1);
                        driverRepository.save(d);
                    }
                }
                break;

            default:
                throw new RuntimeException("Trạng thái không hợp lệ: " + targetStatus);
        }

        subOrder.setUpdatedAt(now);
        SubOrder saved = subOrderRepository.save(subOrder);

        // Ghi log lịch sử thay đổi trạng thái
        OrderStatusHistory history = new OrderStatusHistory(
                saved,
                saved.getMasterOrder(),
                currentStatus,
                targetStatus,
                dto.getNote() != null ? dto.getNote() : ("Chuyển trạng thái sang " + targetStatus),
                staff,
                saved.getBranch(),
                saved.getBranch(),
                "STATUS_CHANGE"
        );
        orderStatusHistoryRepository.save(history);

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

        String oldStatus = subOrder.getStatus();
        LocalDateTime now = LocalDateTime.now();

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
        subOrder.setCancelledAt(now);
        subOrder.setCancelReason(reason != null ? reason : "Nhân viên chi nhánh hủy đơn");
        subOrder.setUpdatedAt(now);
        SubOrder saved = subOrderRepository.save(subOrder);

        // Ghi log lịch sử hủy đơn
        OrderStatusHistory history = new OrderStatusHistory(
                saved,
                saved.getMasterOrder(),
                oldStatus,
                "CANCELLED",
                "Hủy đơn: " + (reason != null ? reason : "Hủy đơn và hoàn tồn kho"),
                staff,
                saved.getBranch(),
                saved.getBranch(),
                "ORDER_CANCELLED"
        );
        orderStatusHistoryRepository.save(history);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubOrderDto assignDriver(Long subOrderId, Long driverId, String customShipperName,
                                   String customShipperPhone, String customLicensePlate,
                                   String carrierName, String trackingNumber, String notes, User staff) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng con với ID: " + subOrderId));

        String currentStatus = subOrder.getStatus();
        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
            throw new RuntimeException("Đơn hàng đã kết thúc (" + currentStatus + "), không thể phân công tài xế!");
        }

        if (driverId != null) {
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver != null) {
                subOrder.setDriver(driver);
                subOrder.setShipperName(driver.getFullName());
                subOrder.setShipperPhone(driver.getPhone());
                subOrder.setShipperVehicle(driver.getVehicleType() + " - " + driver.getLicensePlate());
                subOrder.setShippingCarrier(driver.getCarrierName());
                driver.setActiveOrdersCount(driver.getActiveOrdersCount() != null ? driver.getActiveOrdersCount() + 1 : 1);
                driverRepository.save(driver);
            }
        } else {
            if (customShipperName != null && !customShipperName.isBlank()) {
                subOrder.setShipperName(customShipperName.trim());
            }
            if (customShipperPhone != null && !customShipperPhone.isBlank()) {
                subOrder.setShipperPhone(customShipperPhone.trim());
            }
            if (customLicensePlate != null && !customLicensePlate.isBlank()) {
                subOrder.setShipperVehicle(customLicensePlate.trim());
            }
            if (carrierName != null && !carrierName.isBlank()) {
                subOrder.setShippingCarrier(carrierName.trim());
            } else {
                subOrder.setShippingCarrier("Đội xe Book4Life");
            }
        }

        if (trackingNumber != null && !trackingNumber.isBlank()) {
            subOrder.setTrackingNumber(trackingNumber.trim());
        } else if (subOrder.getTrackingNumber() == null || subOrder.getTrackingNumber().isBlank()) {
            subOrder.setTrackingNumber("B4L-SHIP-" + System.currentTimeMillis());
        }

        LocalDateTime now = LocalDateTime.now();
        subOrder.setStatus("SHIPPED");
        subOrder.setShippedAt(now);
        subOrder.setDeliveryStatus("DELIVERING");
        subOrder.setEstimatedDeliveryTime(now.plusHours(2));
        if (notes != null && !notes.isBlank()) {
            subOrder.setDeliveryNotes(notes.trim());
        }
        subOrder.setUpdatedAt(now);

        SubOrder saved = subOrderRepository.save(subOrder);

        String logNote = "Đã phân công tài xế: " + (saved.getShipperName() != null ? saved.getShipperName() : "Shipper đối tác")
                + " (" + (saved.getShipperPhone() != null ? saved.getShipperPhone() : "") + ")"
                + " - Đơn vị: " + (saved.getShippingCarrier() != null ? saved.getShippingCarrier() : "Book4Life Express");
        if (notes != null && !notes.isBlank()) {
            logNote += ". Ghi chú: " + notes;
        }

        OrderStatusHistory history = new OrderStatusHistory(
                saved,
                saved.getMasterOrder(),
                currentStatus,
                "SHIPPED",
                logNote,
                staff,
                saved.getBranch(),
                saved.getBranch(),
                "STATUS_CHANGE"
        );
        orderStatusHistoryRepository.save(history);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getAvailableDrivers(Long branchId) {
        return driverRepository.findAvailableDriversForBranch(branchId);
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

        // Driver & Delivery Info
        if (entity.getDriver() != null) {
            dto.setDriverId(entity.getDriver().getId());
        }
        dto.setShipperName(entity.getShipperName());
        dto.setShipperPhone(entity.getShipperPhone());
        dto.setShipperVehicle(entity.getShipperVehicle());
        dto.setShippingCarrier(entity.getShippingCarrier());
        dto.setDeliveryStatus(entity.getDeliveryStatus());
        dto.setEstimatedDeliveryTime(entity.getEstimatedDeliveryTime() != null ? entity.getEstimatedDeliveryTime().toString() : null);
        dto.setDeliveryNotes(entity.getDeliveryNotes());

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

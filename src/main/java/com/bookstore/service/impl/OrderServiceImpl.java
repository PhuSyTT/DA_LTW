package com.bookstore.service.impl;

import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.MasterOrder;
import com.bookstore.entity.SubOrder;
import com.bookstore.entity.SubOrderItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.MasterOrderRepository;
import com.bookstore.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final BookItemRepository bookItemRepository;
    private final MasterOrderRepository masterOrderRepository;

    public OrderServiceImpl(BookItemRepository bookItemRepository, MasterOrderRepository masterOrderRepository) {
        this.bookItemRepository = bookItemRepository;
        this.masterOrderRepository = masterOrderRepository;
    }

    // Đảm bảo tính toàn vẹn dữ liệu khi trừ kho
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MasterOrder processCheckout(CheckoutRequestDto request, User user) {
        List<BookItem> selectedItems = new ArrayList<>();

        // 1. Kiểm tra tồn kho an toàn bằng Lock
        for (Long itemId : request.getBookItemIds()) {
            BookItem item = bookItemRepository.findByIdWithLock(itemId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
            
            if (item.getStockQuantity() < 1 || !"AVAILABLE".equals(item.getStatus())) {
                throw new RuntimeException("Sách " + item.getSkuBarcode() + " đã hết hàng.");
            }
            selectedItems.add(item);
        }

        // 2. Thuật toán tách đơn (FR-ORD-02): Nhóm sách theo Chi nhánh (Branch)
        Map<Branch, List<BookItem>> itemsGroupedByBranch = selectedItems.stream()
                .collect(Collectors.groupingBy(BookItem::getBranch));

        MasterOrder masterOrder = new MasterOrder();
        masterOrder.setMasterOrderCode("MO-" + System.currentTimeMillis());
        masterOrder.setUser(user);
        masterOrder.setPaymentStatus("PENDING");
        
        List<SubOrder> subOrders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Tạo các đơn hàng con
        for (Map.Entry<Branch, List<BookItem>> entry : itemsGroupedByBranch.entrySet()) {
            Branch branch = entry.getKey();
            List<BookItem> branchItems = entry.getValue();

            SubOrder subOrder = new SubOrder();
            subOrder.setMasterOrder(masterOrder);
            subOrder.setBranch(branch);
            subOrder.setSubOrderCode("SUB-" + branch.getId() + "-" + System.currentTimeMillis());
            
            // Xử lý giữ sách tại quầy (FR-ORD-04)
            String nextStatus = request.isClickAndCollect() ? "RESERVED" : "SOLD";
            subOrder.setStatus("PENDING");

            BigDecimal subtotal = BigDecimal.ZERO;
            List<SubOrderItem> subOrderItems = new ArrayList<>();

            for (BookItem item : branchItems) {
                // Trừ kho và đổi trạng thái
                item.setStockQuantity(item.getStockQuantity() - 1);
                item.setStatus(nextStatus);
                bookItemRepository.save(item);

                SubOrderItem orderItem = new SubOrderItem();
                orderItem.setSubOrder(subOrder);
                orderItem.setBookItem(item);
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(item.getSellingPrice());
                
                subOrderItems.add(orderItem);
                subtotal = subtotal.add(item.getSellingPrice());
            }

            subOrder.setItems(subOrderItems);
            subOrder.setBranchSubtotal(subtotal);
            subOrders.add(subOrder);
            totalAmount = totalAmount.add(subtotal);
        }

        masterOrder.setSubOrders(subOrders);
        masterOrder.setTotalItemsAmount(totalAmount);

        // Lưu MasterOrder sẽ kéo theo lưu SubOrder và SubOrderItem nhờ CascadeType.ALL
        return masterOrderRepository.save(masterOrder);
    }
}
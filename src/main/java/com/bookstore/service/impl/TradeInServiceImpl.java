package com.bookstore.service.impl;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.service.TradeInService;
import com.bookstore.service.WishlistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TradeInServiceImpl implements TradeInService {

    private final TradeInRequestRepository tradeInRequestRepository;
    private final TradeInImageRepository tradeInImageRepository;
    private final BookRepository bookRepository;
    private final BranchRepository branchRepository;
    private final BookItemRepository bookItemRepository;
    private final BookItemImageRepository bookItemImageRepository;
    private final WishlistService wishlistService;

    public TradeInServiceImpl(TradeInRequestRepository tradeInRequestRepository,
                              TradeInImageRepository tradeInImageRepository,
                              BookRepository bookRepository,
                              BranchRepository branchRepository,
                              BookItemRepository bookItemRepository,
                              BookItemImageRepository bookItemImageRepository,
                              WishlistService wishlistService) {
        this.tradeInRequestRepository = tradeInRequestRepository;
        this.tradeInImageRepository = tradeInImageRepository;
        this.bookRepository = bookRepository;
        this.branchRepository = branchRepository;
        this.bookItemRepository = bookItemRepository;
        this.bookItemImageRepository = bookItemImageRepository;
        this.wishlistService = wishlistService;
    }

    // Nghiệp vụ FR-TRD-03 & Mục 3.2: Thuật toán tự động định giá sơ bộ thu mua
    @Override
    @Transactional(readOnly = true)
    public AutoEstimateResponseDto calculateAutoEstimate(AutoEstimateRequestDto request) {
        BigDecimal originalPrice = BigDecimal.ZERO;
        if (request.getCustomOriginalPrice() != null && request.getCustomOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            originalPrice = request.getCustomOriginalPrice();
        } else if (request.getBookId() != null) {
            Book book = bookRepository.findById(request.getBookId()).orElse(null);
            if (book != null && book.getOriginalPrice() != null) {
                originalPrice = book.getOriginalPrice();
            }
        }

        if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            originalPrice = new BigDecimal("80000"); // Giá niêm yết mặc định
        }

        // Tỷ lệ sàn theo độ mới tự đánh giá (Base Rate)
        String grade = request.getUserConditionGrade() != null ? request.getUserConditionGrade().toUpperCase() : "LIKE_NEW";
        BigDecimal baseRate;
        switch (grade) {
            case "LIKE_NEW":
                baseRate = new BigDecimal("0.50"); // 50%
                break;
            case "VERY_GOOD":
                baseRate = new BigDecimal("0.40"); // 40%
                break;
            case "GOOD":
                baseRate = new BigDecimal("0.30"); // 30%
                break;
            case "ACCEPTABLE":
                baseRate = new BigDecimal("0.20"); // 20%
                break;
            default:
                baseRate = new BigDecimal("0.30");
                break;
        }

        // Khấu trừ khuyết điểm từ bảng kiểm tra 10 tiêu chí (Penalty)
        Map<String, BigDecimal> penaltyBreakdown = new LinkedHashMap<>();
        BigDecimal multiplier = BigDecimal.ONE;
        BigDecimal totalPenalty = BigDecimal.ZERO;

        // 1. Bìa ngoài rách mép / trầy xước / gấp nếp: -5%
        if (request.isCoverTorn() || request.isCoverDamaged()) {
            penaltyBreakdown.put("coverTorn", new BigDecimal("0.05"));
            multiplier = multiplier.multiply(new BigDecimal("0.95"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.05"));
        }
        // 2. Mất màng bóng / bong tróc mép bìa: -5%
        if (request.isCoverPeeling()) {
            penaltyBreakdown.put("coverPeeling", new BigDecimal("0.05"));
            multiplier = multiplier.multiply(new BigDecimal("0.95"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.05"));
        }
        // 3. Gáy lỏng / rách / nứt keo chỉ: -10%
        if (request.isSpineLoose() || request.isSpineDamaged()) {
            penaltyBreakdown.put("spineLoose", new BigDecimal("0.10"));
            multiplier = multiplier.multiply(new BigDecimal("0.90"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.10"));
        }
        // 4. Trang ố vàng / thâm kim / ẩm nhẹ: -10%
        if (request.isYellowPages()) {
            penaltyBreakdown.put("yellowPages", new BigDecimal("0.10"));
            multiplier = multiplier.multiply(new BigDecimal("0.90"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.10"));
        }
        // 5. Viết vẽ / Highlight bút dạ: -10%
        if (request.isMarkingsPresent()) {
            penaltyBreakdown.put("markingsPresent", new BigDecimal("0.10"));
            multiplier = multiplier.multiply(new BigDecimal("0.90"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.10"));
        }
        // 6. Quăn mép / gập góc ("chó tai"): -5%
        if (request.isDogEared()) {
            penaltyBreakdown.put("dogEared", new BigDecimal("0.05"));
            multiplier = multiplier.multiply(new BigDecimal("0.95"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.05"));
        }
        // 7. Rách nhẹ / dán băng keo phục chế: -10%
        if (request.isTornPages()) {
            penaltyBreakdown.put("tornPages", new BigDecimal("0.10"));
            multiplier = multiplier.multiply(new BigDecimal("0.90"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.10"));
        }
        // 8. Thiếu bìa áo / bookmark gốc: -5%
        if (request.isMissingAccessory()) {
            penaltyBreakdown.put("missingAccessory", new BigDecimal("0.05"));
            multiplier = multiplier.multiply(new BigDecimal("0.95"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.05"));
        }
        // 9. Mùi ẩm mốc / phai màu chữ: -5%
        if (request.isMoldySmell()) {
            penaltyBreakdown.put("moldySmell", new BigDecimal("0.05"));
            multiplier = multiplier.multiply(new BigDecimal("0.95"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.05"));
        }
        // 10. Tiêu chí khác tự điền: -5%
        if (request.getOtherDefectDescription() != null && !request.getOtherDefectDescription().isBlank()) {
            penaltyBreakdown.put("otherDefect", new BigDecimal("0.05"));
            multiplier = multiplier.multiply(new BigDecimal("0.95"));
            totalPenalty = totalPenalty.add(new BigDecimal("0.05"));
        }

        // Auto Estimated Price = Original Price * Base Rate * Multiplier
        BigDecimal estimatedPrice = originalPrice.multiply(baseRate).multiply(multiplier);
        // Làm tròn đến hàng nghìn đồng
        estimatedPrice = estimatedPrice.divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP).multiply(new BigDecimal("1000"));

        AutoEstimateResponseDto response = new AutoEstimateResponseDto();
        response.setOriginalPrice(originalPrice);
        response.setBaseRate(baseRate);
        response.setTotalPenalty(totalPenalty);
        response.setEstimatedPrice(estimatedPrice);
        response.setUserConditionGrade(grade);
        response.setPenaltyBreakdown(penaltyBreakdown);
        return response;
    }

    // Nghiệp vụ FR-TRD-01 & FR-TRD-02: Điền khảo sát, tải ảnh và gửi yêu cầu thu mua
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeInResponseDto createTradeInRequest(TradeInCreateDto dto, User user) {
        if (user == null) {
            throw new RuntimeException("Vui lòng đăng nhập để gửi yêu cầu bán lại sách cũ!");
        }

        Book book = null;
        if (dto.getBookId() != null) {
            book = bookRepository.findById(dto.getBookId()).orElse(null);
        }
        if (book == null && dto.getBookTitle() != null && !dto.getBookTitle().isBlank()) {
            // Tìm theo tên sách nhập vào
            try {
                Page<Book> search = bookRepository.searchBooks(dto.getBookTitle().trim(), null, null, PageRequest.of(0, 1));
                if (search.hasContent()) {
                    book = search.getContent().get(0);
                }
            } catch (Exception ignored) {}
        }
        if (book == null) {
            book = bookRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Hệ thống chưa có đầu sách nào để tham chiếu!"));
        }

        Branch branch = branchRepository.findById(dto.getTargetBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh với ID: " + dto.getTargetBranchId()));

        // Tính giá ước tính sơ bộ
        AutoEstimateRequestDto estReq = new AutoEstimateRequestDto();
        estReq.setBookId(book.getId());
        estReq.setCustomOriginalPrice(dto.getEstimatedOriginalPrice());
        estReq.setUserConditionGrade(dto.getUserConditionGrade());
        estReq.setCoverTorn(dto.isCoverTorn());
        estReq.setCoverPeeling(dto.isCoverPeeling());
        estReq.setSpineLoose(dto.isSpineLoose());
        estReq.setYellowPages(dto.isYellowPages());
        estReq.setMarkingsPresent(dto.isMarkingsPresent());
        estReq.setDogEared(dto.isDogEared());
        estReq.setTornPages(dto.isTornPages());
        estReq.setMissingAccessory(dto.isMissingAccessory());
        estReq.setMoldySmell(dto.isMoldySmell());
        estReq.setOtherDefectDescription(dto.getOtherDefectDescription());

        AutoEstimateResponseDto estimate = calculateAutoEstimate(estReq);

        // Tạo mã biên lai chính thức
        String receiptCode = (dto.getReceiptCode() != null && !dto.getReceiptCode().isBlank())
                ? dto.getReceiptCode().trim()
                : "BL-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + (System.currentTimeMillis() % 10000);

        // Chuỗi JSON lưu toàn bộ thông tin 3 nhóm: Thông tin sách, 10 Tiêu chí khảo sát, Biên lai giao dịch
        Map<String, Object> surveyData = new LinkedHashMap<>();
        
        // Nhóm 1: Thông tin sách
        Map<String, Object> bookInfo = new LinkedHashMap<>();
        bookInfo.put("title", dto.getBookTitle() != null && !dto.getBookTitle().isBlank() ? dto.getBookTitle() : book.getTitle());
        bookInfo.put("category", dto.getCategoryName() != null ? dto.getCategoryName() : (book.getCategory() != null ? book.getCategory().getName() : ""));
        bookInfo.put("publisher", dto.getPublisher() != null ? dto.getPublisher() : book.getPublisher());
        bookInfo.put("publicationYear", dto.getPublicationYear() != null ? dto.getPublicationYear() : book.getPublicationYear());
        bookInfo.put("originalPrice", dto.getEstimatedOriginalPrice() != null ? dto.getEstimatedOriginalPrice() : book.getOriginalPrice());
        surveyData.put("bookInfo", bookInfo);

        // Nhóm 2: Khảo sát 10 tiêu chí
        Map<String, Object> conditionSurvey = new LinkedHashMap<>();
        conditionSurvey.put("coverTorn", dto.isCoverTorn());
        conditionSurvey.put("coverPeeling", dto.isCoverPeeling());
        conditionSurvey.put("spineLoose", dto.isSpineLoose());
        conditionSurvey.put("yellowPages", dto.isYellowPages());
        conditionSurvey.put("markingsPresent", dto.isMarkingsPresent());
        conditionSurvey.put("dogEared", dto.isDogEared());
        conditionSurvey.put("tornPages", dto.isTornPages());
        conditionSurvey.put("missingAccessory", dto.isMissingAccessory());
        conditionSurvey.put("moldySmell", dto.isMoldySmell());
        conditionSurvey.put("otherDefectDescription", dto.getOtherDefectDescription() != null ? dto.getOtherDefectDescription() : "");
        conditionSurvey.put("otherDefectImageUrl", dto.getOtherDefectImageUrl() != null ? dto.getOtherDefectImageUrl() : "");
        surveyData.put("conditionSurvey", conditionSurvey);

        // Nhóm 3: Biên lai bán sách
        Map<String, Object> receiptInfo = new LinkedHashMap<>();
        receiptInfo.put("receiptCode", receiptCode);
        receiptInfo.put("customerName", dto.getCustomerName() != null ? dto.getCustomerName() : user.getFullName());
        receiptInfo.put("customerPhone", dto.getCustomerPhone() != null ? dto.getCustomerPhone() : user.getPhone());
        receiptInfo.put("customerEmail", dto.getCustomerEmail() != null ? dto.getCustomerEmail() : user.getEmail());
        receiptInfo.put("customerAddress", dto.getCustomerAddress() != null ? dto.getCustomerAddress() : "");
        receiptInfo.put("targetBranchId", branch.getId());
        receiptInfo.put("targetBranchName", branch.getBranchName());
        receiptInfo.put("deliveryMethod", "BRING_TO_STORE");
        receiptInfo.put("expectedArrivalTime", dto.getExpectedArrivalTime() != null ? dto.getExpectedArrivalTime() : "");
        surveyData.put("receipt", receiptInfo);

        // Chuyển sang chuỗi JSON chuẩn
        String surveyJson = "{}";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            surveyJson = mapper.writeValueAsString(surveyData);
        } catch (Exception ex) {
            surveyJson = String.format("{\"receiptCode\":\"%s\",\"note\":\"%s\"}", receiptCode, dto.getUserNote() != null ? dto.getUserNote() : "");
        }

        TradeInRequest request = new TradeInRequest();
        request.setRequestCode(receiptCode);
        request.setUser(user);
        request.setBook(book);
        request.setTargetBranch(branch);
        request.setUserConditionGrade(dto.getUserConditionGrade() != null ? dto.getUserConditionGrade().toUpperCase() : "LIKE_NEW");
        request.setSurveyAnswersJson(surveyJson);
        request.setAutoEstimatedPrice(estimate.getEstimatedPrice());
        request.setStatus("PENDING_REVIEW");
        request.setDeliveryMethod("BRING_TO_STORE");
        request.setCreatedAt(LocalDateTime.now());

        TradeInRequest saved = tradeInRequestRepository.save(request);

        // Lưu danh sách ảnh thẩm định (6 mặt + ảnh lỗi khác)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<TradeInImage> imageEntities = new ArrayList<>();
            for (TradeInImageDto imgDto : dto.getImages()) {
                if (imgDto.getImageUrl() != null && !imgDto.getImageUrl().isBlank()) {
                    TradeInImage img = new TradeInImage(
                            saved,
                            imgDto.getImageUrl(),
                            imgDto.getCloudinaryPublicId() != null ? imgDto.getCloudinaryPublicId() : "pub_" + System.currentTimeMillis(),
                            imgDto.getImageType() != null ? imgDto.getImageType() : "COVER_FRONT"
                    );
                    imageEntities.add(img);
                }
            }
            if (!imageEntities.isEmpty()) {
                tradeInImageRepository.saveAll(imageEntities);
                saved.setImages(imageEntities);
            }
        }

        return mapToDto(saved);
    }

    // Nghiệp vụ FR-TRD-04: Nhân viên kiểm duyệt thẩm định & Định giá lại
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeInResponseDto reviewTradeInRequest(Long requestId, TradeInReviewDto dto, User staff) {
        TradeInRequest request = tradeInRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu thu mua với ID: " + requestId));

        if (!"PENDING_REVIEW".equals(request.getStatus())) {
            throw new RuntimeException("Phiếu không ở trạng thái chờ thẩm định (PENDING_REVIEW)!");
        }

        if (dto.isAcceptInitialEstimate()) {
            request.setStaffReviewedPrice(request.getAutoEstimatedPrice());
            request.setStaffConditionGrade(request.getUserConditionGrade());
        } else {
            if (dto.getStaffReviewedPrice() == null || dto.getStaffReviewedPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Vui lòng nhập mức giá thẩm định hợp lệ!");
            }
            request.setStaffReviewedPrice(dto.getStaffReviewedPrice());
            request.setStaffConditionGrade(dto.getStaffConditionGrade() != null ? dto.getStaffConditionGrade() : request.getUserConditionGrade());
        }

        request.setStaffNotes(dto.getStaffNotes());
        request.setReviewedByStaff(staff);
        request.setStatus("OFFERED"); // Chuyển trạng thái để gửi báo giá tới khách hàng

        TradeInRequest saved = tradeInRequestRepository.save(request);
        return mapToDto(saved);
    }

    // Nghiệp vụ FR-TRD-05: Khách hàng phản hồi kết quả định giá
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeInResponseDto customerRespondToOffer(Long requestId, TradeInCustomerResponseDto dto, User user) {
        TradeInRequest request = tradeInRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu thu mua với ID: " + requestId));

        if (!"OFFERED".equals(request.getStatus())) {
            throw new RuntimeException("Phiếu không ở trạng thái chờ khách hàng phản hồi!");
        }

        if (dto.isAccepted()) {
            // Nhánh 1: Khách hàng Đồng ý bán
            request.setStatus("ACCEPTED_AWAITING_DELIVERY");
            if (dto.getDeliveryMethod() != null && !dto.getDeliveryMethod().isBlank()) {
                request.setDeliveryMethod(dto.getDeliveryMethod());
            }
        } else {
            // Nhánh 2: Khách hàng Từ chối bán
            request.setStatus("CANCELLED");
        }

        TradeInRequest saved = tradeInRequestRepository.save(request);
        return mapToDto(saved);
    }

    // Nghiệp vụ FR-TRD-06: Nhận sách thực tế & Nhập kho - Tạo phiếu chi
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeInResponseDto confirmPhysicalReceiptAndStock(Long requestId, String shelfLocation, User staff) {
        TradeInRequest request = tradeInRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu thu mua với ID: " + requestId));

        if (!"ACCEPTED_AWAITING_DELIVERY".equals(request.getStatus())) {
            throw new RuntimeException("Chỉ có thể nhập kho phiếu đã được khách hàng đồng ý bán (ACCEPTED_AWAITING_DELIVERY)!");
        }

        // 1. Tạo BookItem mới nhập vào kho của Chi nhánh
        String conditionGrade = request.getStaffConditionGrade() != null ?
                request.getStaffConditionGrade() : request.getUserConditionGrade();

        BigDecimal purchasePrice = request.getStaffReviewedPrice() != null ?
                request.getStaffReviewedPrice() : request.getAutoEstimatedPrice();

        // Định giá bán lại: Tỷ lệ theo độ mới hoặc nhân hệ số so với giá thu mua
        BigDecimal sellingPrice = calculateSellingPriceFromGrade(request.getBook(), conditionGrade, purchasePrice);

        String skuBarcode = String.format("TI-B%d-%d", request.getTargetBranch().getId(), request.getId());

        BookItem bookItem = new BookItem();
        bookItem.setBook(request.getBook());
        bookItem.setBranch(request.getTargetBranch());
        bookItem.setSkuBarcode(skuBarcode);
        bookItem.setConditionGrade(conditionGrade);
        bookItem.setSellingPrice(sellingPrice);
        bookItem.setStockQuantity(1);
        bookItem.setShelfLocation(shelfLocation != null && !shelfLocation.isBlank() ? shelfLocation : "Kệ Thu Mua - Dãy T1");
        bookItem.setConditionNote("Thu mua từ khách hàng theo phiếu #" + request.getRequestCode());
        bookItem.setImportDate(LocalDateTime.now());
        bookItem.setStatus("AVAILABLE");

        BookItem savedItem = bookItemRepository.save(bookItem);

        // 2. Chuyển các ảnh thực tế từ TradeInImage sang BookItemImage
        List<TradeInImage> images = tradeInImageRepository.findByTradeInRequestId(requestId);
        if (images != null && !images.isEmpty()) {
            boolean isFirst = true;
            for (TradeInImage tiImg : images) {
                BookItemImage biImg = new BookItemImage(
                        null,
                        savedItem,
                        tiImg.getImageUrl(),
                        tiImg.getCloudinaryPublicId(),
                        isFirst
                );
                bookItemImageRepository.save(biImg);
                isFirst = false;
            }
        }

        // 3. Cập nhật trạng thái phiếu thu mua
        request.setStatus("RECEIVED_STOCKED");
        TradeInRequest savedRequest = tradeInRequestRepository.save(request);

        // 4. Kích hoạt thông báo Wishlist săn sách (Tự động quét khách hàng đang mong muốn cuốn sách này)
        try {
            wishlistService.checkAndTriggerRestockAlerts(savedItem);
        } catch (Exception e) {
            // Không làm gián đoạn việc nhập kho nếu wishlist alert lỗi
        }

        return mapToDto(savedRequest);
    }

    private BigDecimal calculateSellingPriceFromGrade(Book book, String grade, BigDecimal purchasePrice) {
        BigDecimal originalPrice = book.getOriginalPrice();
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate;
            switch (grade.toUpperCase()) {
                case "LIKE_NEW":
                    rate = new BigDecimal("0.80"); // 80% giá bìa
                    break;
                case "VERY_GOOD":
                    rate = new BigDecimal("0.65"); // 65% giá bìa
                    break;
                case "GOOD":
                    rate = new BigDecimal("0.50"); // 50% giá bìa
                    break;
                case "ACCEPTABLE":
                    rate = new BigDecimal("0.35"); // 35% giá bìa
                    break;
                default:
                    rate = new BigDecimal("0.50");
                    break;
            }
            return originalPrice.multiply(rate).divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP).multiply(new BigDecimal("1000"));
        }

        // Fallback: 1.6 lần giá mua
        return purchasePrice.multiply(new BigDecimal("1.6")).divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP).multiply(new BigDecimal("1000"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeInResponseDto> getRequestsByBranch(Long branchId, String status) {
        List<TradeInRequest> list;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            list = tradeInRequestRepository.findByTargetBranchIdAndStatusOrderByCreatedAtDesc(branchId, status.toUpperCase());
        } else {
            list = tradeInRequestRepository.findByTargetBranchIdOrderByCreatedAtDesc(branchId);
        }
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeInResponseDto> getUserRequests(Long userId) {
        return tradeInRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TradeInResponseDto getRequestById(Long requestId) {
        TradeInRequest request = tradeInRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu thu mua với ID: " + requestId));
        return mapToDto(request);
    }

    private TradeInResponseDto mapToDto(TradeInRequest entity) {
        TradeInResponseDto dto = new TradeInResponseDto();
        dto.setId(entity.getId());
        dto.setRequestCode(entity.getRequestCode());
        dto.setUserConditionGrade(entity.getUserConditionGrade());
        dto.setSurveyAnswersJson(entity.getSurveyAnswersJson());
        dto.setAutoEstimatedPrice(entity.getAutoEstimatedPrice());
        dto.setStaffReviewedPrice(entity.getStaffReviewedPrice());
        dto.setStaffConditionGrade(entity.getStaffConditionGrade());
        dto.setStatus(entity.getStatus());
        dto.setDeliveryMethod(entity.getDeliveryMethod());
        dto.setStaffNotes(entity.getStaffNotes());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setCustomerName(entity.getUser().getFullName());
            dto.setCustomerEmail(entity.getUser().getEmail());
            dto.setCustomerPhone(entity.getUser().getPhone());
        }

        if (entity.getBook() != null) {
            Book book = entity.getBook();
            dto.setBookId(book.getId());
            dto.setBookTitle(book.getTitle());
            dto.setBookIsbn(book.getIsbn());
            dto.setBookOriginalPrice(book.getOriginalPrice());
            dto.setBookCoverUrl(book.getCoverImageUrl());
        }

        if (entity.getTargetBranch() != null) {
            dto.setTargetBranchId(entity.getTargetBranch().getId());
            dto.setTargetBranchName(entity.getTargetBranch().getBranchName());
        }

        if (entity.getReviewedByStaff() != null) {
            dto.setReviewedByStaffId(entity.getReviewedByStaff().getId());
            dto.setReviewedByStaffName(entity.getReviewedByStaff().getFullName());
        }

        List<TradeInImage> images = entity.getImages();
        if (images == null || images.isEmpty()) {
            images = tradeInImageRepository.findByTradeInRequestId(entity.getId());
        }

        if (images != null) {
            List<TradeInImageDto> imgDtos = images.stream()
                    .map(img -> new TradeInImageDto(img.getImageUrl(), img.getCloudinaryPublicId(), img.getImageType()))
                    .collect(Collectors.toList());
            dto.setImages(imgDtos);
        }

        return dto;
    }
}

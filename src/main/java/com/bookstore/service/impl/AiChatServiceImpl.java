package com.bookstore.service.impl;

import com.bookstore.dto.ChatMessageDto;
import com.bookstore.dto.ChatRequestDto;
import com.bookstore.dto.ChatResponseDto;
import com.bookstore.dto.FaqItemDto;
import com.bookstore.entity.AiChatMessage;
import com.bookstore.entity.User;
import com.bookstore.repository.AiChatMessageRepository;
import com.bookstore.service.AiChatService;
import com.bookstore.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final AiChatMessageRepository aiChatMessageRepository;
    private final EmailService emailService;
    private final List<FaqItemDto> faqList;

    public AiChatServiceImpl(AiChatMessageRepository aiChatMessageRepository,
                             EmailService emailService) {
        this.aiChatMessageRepository = aiChatMessageRepository;
        this.emailService = emailService;
        this.faqList = initializeFaqList();
    }

    @Override
    public List<FaqItemDto> getFaqList() {
        return faqList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseDto processUserMessage(ChatRequestDto request, User currentUser) {
        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMessage.isEmpty()) {
            return new ChatResponseDto("Dạ em có thể hỗ trợ gì cho mình về sách và đơn hàng ạ?", "SUCCESS");
        }

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "session-" + UUID.randomUUID();
        }

        // 1. Lưu tin nhắn của User vào Database
        AiChatMessage userMsgEntity = new AiChatMessage(currentUser, sessionId, "USER", userMessage);
        aiChatMessageRepository.save(userMsgEntity);

        // 2. Kiểm tra xem câu hỏi có khớp với kịch bản có sẵn (Rule-based FAQ) hay không
        String botReply = findFaqAnswer(userMessage);
        String status;

        if (botReply != null) {
            // Câu hỏi có sẵn trong kịch bản FAQ
            status = "FAQ";
        } else {
            // Câu hỏi tự gõ của người dùng (không nằm trong danh sách FAQ)
            // Gửi email chuyển tiếp tới Email Quản trị (Hotline)
            log.info("Khách hàng gửi câu hỏi tùy chỉnh: '{}'. Đang gửi email chuyển tiếp tới Hotline...", userMessage);
            emailService.sendCustomerSupportQuery(userMessage, currentUser, sessionId);

            botReply = "Dạ, câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Chúng tôi sẽ phản hồi lại bạn qua Email đăng ký trong thời gian sớm nhất nhé!";
            status = "FORWARDED_EMAIL";
        }

        // 3. Lưu phản hồi vào Database
        AiChatMessage botMsgEntity = new AiChatMessage(currentUser, sessionId, "ASSISTANT", botReply);
        aiChatMessageRepository.save(botMsgEntity);

        return new ChatResponseDto(botReply, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(String sessionId, User currentUser) {
        List<AiChatMessage> list;
        if (currentUser != null && currentUser.getId() != null) {
            list = aiChatMessageRepository.findByUserIdOrderByCreatedAtAsc(currentUser.getId());
        } else if (sessionId != null && !sessionId.isBlank()) {
            list = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        } else {
            list = Collections.emptyList();
        }

        return list.stream()
                .map(m -> new ChatMessageDto(m.getId(), m.getSenderType(), m.getContent(), m.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearChatHistory(String sessionId, User currentUser) {
        if (currentUser != null && currentUser.getId() != null) {
            aiChatMessageRepository.deleteByUserId(currentUser.getId());
        }
        if (sessionId != null && !sessionId.isBlank()) {
            aiChatMessageRepository.deleteBySessionId(sessionId);
        }
    }

    /**
     * Tra cứu câu trả lời theo kịch bản (Rule-based FAQ)
     */
    private String findFaqAnswer(String userMessage) {
        String msg = userMessage.trim().toLowerCase();

        // 1. So khớp chính xác hoặc khớp ID / tiêu đề của danh sách FAQ
        for (FaqItemDto faq : faqList) {
            if (msg.equalsIgnoreCase(faq.getQuestion().trim()) ||
                msg.equalsIgnoreCase(faq.getShortTitle().trim()) ||
                msg.equalsIgnoreCase(faq.getId().trim())) {
                return faq.getAnswer();
            }
        }

        // 2. So khớp từ khóa với kịch bản FAQ 1: Thu mua & Đổi sách cũ (Trade-in)
        if (matchesAny(msg, "thu mua", "bán sách", "ban sach", "đổi sách", "doi sach", "trade-in", "trade in",
                "quy trình thu mua", "quy trinh thu mua", "thanh lý sách", "định giá sách")) {
            return getFaqById("trade-in").getAnswer();
        }

        // 3. So khớp từ khóa với kịch bản FAQ 2: Tiến độ đơn hàng & Hủy đơn
        if (matchesAny(msg, "đơn hàng", "don hang", "tiến độ", "tien do", "tra cứu đơn", "hủy đơn", "huy don", "timeline")) {
            return getFaqById("order-tracking").getAnswer();
        }

        // 4. So khớp từ khóa với kịch bản FAQ 3: Hệ thống chi nhánh & Địa chỉ
        if (matchesAny(msg, "chi nhánh", "chi nhanh", "địa chỉ", "dia chi", "cửa hàng ở đâu", "giờ mở cửa", "quận 1", "thủ đức", "bình thạnh")) {
            return getFaqById("branches").getAnswer();
        }

        // 5. So khớp từ khóa với kịch bản FAQ 4: Phân loại chất lượng sách cũ
        if (matchesAny(msg, "like new", "very good", "phân loại", "phan loai", "chất lượng sách", "tình trạng sách")) {
            return getFaqById("grading").getAnswer();
        }

        // 6. So khớp từ khóa với kịch bản FAQ 5: Mã giảm giá & Freeship
        if (matchesAny(msg, "mã giảm giá", "ma giam gia", "voucher", "coupon", "freeship", "khuyến mãi", "ưu đãi")) {
            return getFaqById("coupon").getAnswer();
        }

        // 7. So khớp từ khóa với kịch bản FAQ 6: Phương thức thanh toán & Giao hàng
        if (matchesAny(msg, "thanh toán", "thanh toan", "ship bao lâu", "giao hàng", "giao hang", "cod", "vnpay")) {
            return getFaqById("payment").getAnswer();
        }

        // 8. So khớp từ khóa với kịch bản FAQ 7: Gợi ý sách văn học & Tác giả
        if (matchesAny(msg, "gợi ý sách", "goi y sach", "sách hay", "văn học", "tác giả", "nam cao", "vũ trọng phụng", "xuân diệu", "phùng quán", "bảo ninh")) {
            return getFaqById("literature").getAnswer();
        }

        // 9. So khớp từ khóa với kịch bản FAQ 8: Hotline & Kênh hỗ trợ
        if (matchesAny(msg, "hotline", "tổng đài", "tong dai", "số điện thoại", "lien he", "liên hệ")) {
            return getFaqById("contact").getAnswer();
        }

        // 10. Chào hỏi thông thường
        if (matchesAny(msg, "chào", "chao", "hello", "hi", "alo", "bạn là ai")) {
            return "Dạ em chào bạn! 👋 Em là **Trợ lý ảo CSKH Book4Life** 🌿.\n\n" +
                   "Bạn có thể click trực tiếp vào ô nhập liệu bên dưới để xem menu **Câu hỏi thường gặp**, hoặc chọn nhanh các chủ đề:\n" +
                   "• ♻️ Quy trình thu mua & đổi sách cũ\n" +
                   "• 📦 Tra cứu tiến độ đơn hàng & timeline\n" +
                   "• 📍 Hệ thống chi nhánh & giờ mở cửa\n" +
                   "• 📚 Tủ sách văn học tiêu biểu\n\n" +
                   "Nếu có thắc mắc đặc biệt, bạn cứ gõ câu hỏi vào khung chat, em sẽ chuyển tiếp ngay đến bộ phận hỗ trợ khách hàng nhé!";
        }

        // 11. Cảm ơn & tạm biệt
        if (matchesAny(msg, "cảm ơn", "cam on", "thanks", "thank you", "tạm biệt", "tam biet", "bye", "ok", "được rồi")) {
            return "Dạ không có gì ạ! Book4Life rất vui được đồng hành cùng bạn 🌿. Chúc bạn luôn tìm thấy niềm vui và sự an nhiên bên những cuốn sách hay nhé! ✨";
        }

        // Không khớp bất kỳ kịch bản nào -> trả về null để chuyển tiếp email hotline
        return null;
    }

    private FaqItemDto getFaqById(String id) {
        for (FaqItemDto item : faqList) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return faqList.get(0);
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (kw.length() <= 3) {
                // Match standalone whole word for short keywords like "hi", "ok", "bye"
                if (text.matches("(?i).*\\b" + java.util.regex.Pattern.quote(kw) + "\\b.*")) {
                    return true;
                }
            } else {
                if (text.contains(kw)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<FaqItemDto> initializeFaqList() {
        List<FaqItemDto> list = new ArrayList<>();

        list.add(new FaqItemDto(
                "trade-in",
                "fa-recycle",
                "Quy trình thu mua và đổi sách cũ tại Book4Life như thế nào?",
                "Quy trình thu mua & đổi sách cũ",
                "Dịch vụ Trade-in",
                "🌿 **Quy Trình Thu Mua & Đổi Sách Cũ (Trade-in) tại Book4Life:**\n\n" +
                "Để trao đời mới cho những cuốn sách cũ và nhận lại tiền mặt hoặc điểm tích lũy, bạn thực hiện theo 4 bước:\n\n" +
                "• **Bước 1: Khảo sát định giá online**\n" +
                "  Truy cập mục **[Đăng Ký Thu Mua Sách](/thu-mua-sach)**, điền tên tác phẩm và hoàn thành 6 câu hỏi kiểm tra nhanh về độ mới (bìa, gáy, trang ố, chữ ký...).\n" +
                "• **Bước 2: Xem giá thu mua tự động**\n" +
                "  Hệ thống tính toán giá thu mua tức thì dựa trên tỷ lệ độ mới của sách (LIKE NEW đến ACCEPTABLE).\n" +
                "• **Bước 3: Gửi sách về chi nhánh**\n" +
                "  Mang sách đến chi nhánh gần nhất của Book4Life hoặc gửi bưu điện kèm mã biên lai thu gom.\n" +
                "• **Bước 4: Nhận thanh toán**\n" +
                "  Nhân viên đối soát thực tế và chuyển tiền mặt/chuyển khoản ngay, hoặc quy đổi sang điểm mua sách được **cộng thêm 10% giá trị**.\n\n" +
                "👉 Bạn có thể theo dõi tiến độ các yêu cầu tại **[Lịch Sử Bán Sách Cũ](/my-trade-in)**."
        ));

        list.add(new FaqItemDto(
                "order-tracking",
                "fa-box-open",
                "Làm sao để tôi theo dõi tiến độ đơn hàng và hủy đơn?",
                "Tra cứu đơn hàng & timeline",
                "Đơn hàng",
                "📦 **Tra Cứu Tiến Độ Đơn Hàng & Hướng Dẫn Hủy Đơn:**\n\n" +
                "1. **Xem hành trình đơn hàng trực quan:**\n" +
                "   • Đăng nhập tài khoản và vào mục **[Đơn Hàng Của Tôi](/my-orders)**.\n" +
                "   • Nhấn chọn **'Xem Tiến Độ (Timeline)'** tại đơn hàng muốn kiểm tra.\n" +
                "   • Hệ thống sẽ hiển thị timeline 5 bước: *Chờ xác nhận ➔ Đã xác nhận ➔ Đang đóng gói ➔ Đang giao hàng ➔ Giao thành công*.\n\n" +
                "2. **Chính sách Hủy đơn hàng:**\n" +
                "   • Quý khách có thể tự thao tác **'Hủy Đơn Hàng'** khi đơn đang ở trạng thái **Chờ xác nhận**.\n" +
                "   • Khi xác nhận hủy, số lượng sách sẽ được hệ thống **tự động hoàn tồn kho** ngay lập tức."
        ));

        list.add(new FaqItemDto(
                "branches",
                "fa-map-location-dot",
                "Nhà sách Book4Life có những chi nhánh nào và mở cửa đến mấy giờ?",
                "Hệ thống chi nhánh & giờ mở cửa",
                "Cửa hàng",
                "📍 **Hệ Thống 3 Chi Nhánh Book4Life tại TP.HCM:**\n\n" +
                "• **Chi nhánh Quận 1 (Trụ Sở Chính):**\n" +
                "  120 Nguyễn Huệ, P. Bến Nghé, Quận 1 • Hotline: 028 3822 1234\n" +
                "• **Chi nhánh TP. Thủ Đức:**\n" +
                "  45 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức • Hotline: 028 3722 5678\n" +
                "• **Chi nhánh Bình Thạnh:**\n" +
                "  235 Bạch Đằng, P. 15, Q. Bình Thạnh • Hotline: 028 3511 9012\n\n" +
                "⏰ **Thời gian hoạt động:** Mở cửa từ **08:00 – 21:30** tất cả các ngày trong tuần (kể cả Thứ 7, Chủ Nhật và ngày lễ).\n" +
                "Tại mỗi chi nhánh đều có góc đọc sách xưa và quầy tiếp nhận sách cũ ký gửi trực tiếp!"
        ));

        list.add(new FaqItemDto(
                "grading",
                "fa-book-bookmark",
                "Tiêu chuẩn phân loại chất lượng sách cũ (Like New, Good,...) như thế nào?",
                "Tiêu chuẩn phân loại sách cũ",
                "Sản phẩm",
                "📖 **Tiêu Chuẩn Phân Loại Sách Cũ tại Book4Life:**\n\n" +
                "Mọi cuốn sách khi nhập kho đều được thẩm định nghiêm ngặt theo 5 cấp độ chất lượng:\n" +
                "• **LIKE_NEW (Như mới 95-99%):** Sách đẹp hoàn hảo, bìa phẳng, gáy vuông vức, giấy trắng không tì vết.\n" +
                "• **VERY_GOOD (Rất tốt 85-94%):** Sách nguyên vẹn, gáy thẳng, không ghi chú, có thể có vệt ố vàng tự nhiên rất nhẹ ở mép ngoài.\n" +
                "• **GOOD (Tốt 70-84%):** Bìa hơi sờn nhẹ, giấy ngả màu thời gian nhưng trọn vẹn từng trang, đọc rất tốt.\n" +
                "• **ACCEPTABLE (Chấp nhận được 50-69%):** Sách có nếp gập gáy hoặc ghi chú bằng bút chì, nhưng nội dung đầy đủ.\n" +
                "• **COLLECTIBLE (Sưu tầm / Sách xưa quý hiếm):** Ấn bản xuất bản trước 1975, bản in đầu tiên (First Edition) hoặc có thủ bút, chữ ký của tác giả."
        ));

        list.add(new FaqItemDto(
                "coupon",
                "fa-ticket",
                "Chính sách miễn phí vận chuyển (Freeship) và mã giảm giá áp dụng ra sao?",
                "Mã giảm giá & Freeship",
                "Khuyến mãi",
                "🎁 **Chương Trình Khuyến Mãi & Coupon Ưu Đãi:**\n\n" +
                "• **Freeship toàn quốc:** Tự động áp dụng phí vận chuyển 0đ cho đơn hàng có giá trị từ **250.000đ** trở lên.\n" +
                "• **Tặng Coupon 15%:** Dành cho khách hàng lần đầu hoàn thành bán sách cũ (trade-in).\n" +
                "• **Cách nhập mã giảm giá:** Truy cập trang **[Giỏ Hàng](/cart)**, điền mã coupon vào ô *Mã Giảm Giá* rồi bấm **Áp Dụng** trước khi xác nhận đơn hàng."
        ));

        list.add(new FaqItemDto(
                "payment",
                "fa-truck-fast",
                "Tôi có thể thanh toán bằng những hình thức nào và bao lâu nhận được sách?",
                "Thanh toán & Giao nhận sách",
                "Thanh toán",
                "🚚 **Phương Thức Thanh Toán & Thời Gian Giao Nhận:**\n\n" +
                "• **Hình thức thanh toán linh hoạt:**\n" +
                "  1. Nhận hàng kiểm tra rồi trả tiền mặt (*COD*).\n" +
                "  2. Thanh toán trực tuyến chuyển khoản ngân hàng / VNPay an toàn.\n" +
                "• **Thời gian giao sách:**\n" +
                "  • Nội thành TP.HCM: Giao nhanh từ **24h – 48h**.\n" +
                "  • Các tỉnh thành khác: Giao từ **2 – 4 ngày** làm việc.\n" +
                "• **Cam kết đóng gói:** Toàn bộ sách cũ đều được bọc màng co và chèn đệm xốp chống va đập góc bìa khi vận chuyển."
        ));

        list.add(new FaqItemDto(
                "literature",
                "fa-book",
                "Nhà sách có những tác phẩm văn học Việt Nam nào đặc sắc nên đọc?",
                "Gợi ý tác phẩm văn học hay",
                "Khám phá",
                "📚 **Tủ Sách Văn Học Tiêu Biểu tại Book4Life:**\n\n" +
                "Book4Life tự hào lưu giữ các ấn bản văn học có giá trị vượt thời gian:\n" +
                "• **Văn học hiện thực:** *Số Đỏ*, *Giông Tố* (Vũ Trọng Phụng); Tuyển tập truyện ngắn Nam Cao (*Chí Phèo*, *Lão Hạc*).\n" +
                "• **Thơ & Tùy bút kinh điển:** *Phấn Thông Vàng* (Xuân Diệu); *Trời Mỗi Ngày Lại Sáng* (Huy Cận); *Hà Nội 36 Phố Phường* (Thạch Lam).\n" +
                "• **Văn học Chiến tranh & Ký ức:** *Tuổi Thơ Dữ Dội*, *Ba Phút Sự Thật* (Phùng Quán); *Nỗi Buồn Chiến Tranh* (Bảo Ninh); *Ăn Mày Dĩ Vãng* (Chu Lai).\n" +
                "• **Văn hóa & Đất rừng phương Nam:** *Đò Dọc* (Bình Nguyên Lộc); *Hương Rừng Cà Mau* (Sơn Nam).\n\n" +
                "👉 Mời bạn ghé xem trọn bộ tại **[Kho Sách Cũ](/books)** và **[Danh Mục Tác Giả](/authors)**."
        ));

        list.add(new FaqItemDto(
                "contact",
                "fa-headset",
                "Làm thế nào để liên hệ trực tiếp nhân viên hỗ trợ hoặc hotline cửa hàng?",
                "Liên hệ Hotline & CSKH",
                "Hỗ trợ",
                "📞 **Kênh Hỗ Trợ Trực Tiếp Khách Hàng Book4Life:**\n\n" +
                "• **Hotline tư vấn nhanh:** **1900 6868** (Nhánh 1: Mua hàng, Nhánh 2: Thu mua sách cũ)\n" +
                "• **Email hỗ trợ:** admin.book4life@gmail.com\n" +
                "• **Thời gian tiếp nhận:** 08:00 – 21:30 hàng ngày.\n\n" +
                "Ngoài ra, bạn cũng có thể gõ trực tiếp câu hỏi vào ô chat này, hệ thống sẽ tự động gửi email chuyển tiếp nội dung đến ban quản trị để hỗ trợ bạn qua email sớm nhất nhé!"
        ));

        return list;
    }
}

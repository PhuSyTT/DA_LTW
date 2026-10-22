/**
 * Book4Life - Chatbox Trợ Lý Ảo CSKH
 * Hệ thống kịch bản có sẵn (Rule-based FAQ) & Chuyển tiếp Email Hotline cho câu hỏi tùy chỉnh
 */
(function () {
    'use strict';

    // 1. Quản lý Session ID trong localStorage
    const SESSION_STORAGE_KEY = 'b4l_ai_chat_session_id';
    function getOrCreateSessionId() {
        let sid = localStorage.getItem(SESSION_STORAGE_KEY);
        if (!sid) {
            sid = 'session-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
            localStorage.setItem(SESSION_STORAGE_KEY, sid);
        }
        return sid;
    }

    let sessionId = getOrCreateSessionId();
    let isWaitingResponse = false;
    let isHistoryLoaded = false;
    let isFaqRendered = false;

    // Danh mục câu hỏi thường gặp (FAQ List chuẩn Book4Life)
    const DEFAULT_FAQ_LIST = [
        {
            id: 'trade-in',
            icon: 'fa-recycle',
            question: 'Quy trình thu mua và đổi sách cũ tại Book4Life như thế nào?',
            shortTitle: 'Quy trình thu mua & đổi sách cũ',
            category: 'Thu mua',
            answer: '🌿 **Quy Trình Thu Mua & Đổi Sách Cũ (Trade-in) tại Book4Life:**\n\n' +
                'Để trao đời mới cho những cuốn sách cũ và nhận lại tiền mặt hoặc điểm tích lũy, bạn thực hiện theo 4 bước:\n\n' +
                '• **Bước 1: Khảo sát định giá online**\n  Truy cập mục **[Đăng Ký Thu Mua Sách](/thu-mua-sach)**, điền tên tác phẩm và hoàn thành 6 câu hỏi kiểm tra nhanh về độ mới (bìa, gáy, trang ố, chữ ký...).\n' +
                '• **Bước 2: Xem giá thu mua tự động**\n  Hệ thống tính toán giá thu mua tức thì dựa trên tỷ lệ độ mới của sách (LIKE NEW đến ACCEPTABLE).\n' +
                '• **Bước 3: Gửi sách về chi nhánh**\n  Mang sách đến chi nhánh gần nhất của Book4Life hoặc gửi bưu điện kèm mã biên lai thu gom.\n' +
                '• **Bước 4: Nhận thanh toán**\n  Nhân viên đối soát thực tế và chuyển tiền mặt/chuyển khoản ngay, hoặc quy đổi sang điểm mua sách được **cộng thêm 10% giá trị**.\n\n' +
                '👉 Bạn có thể theo dõi tiến độ các yêu cầu tại **[Lịch Sử Bán Sách Cũ](/my-trade-in)**.'
        },
        {
            id: 'order-tracking',
            icon: 'fa-box-open',
            question: 'Làm sao để tôi theo dõi tiến độ đơn hàng và hủy đơn?',
            shortTitle: 'Tra cứu đơn hàng & timeline',
            category: 'Đơn hàng',
            answer: '📦 **Tra Cứu Tiến Độ Đơn Hàng & Hướng Dẫn Hủy Đơn:**\n\n' +
                '1. **Xem hành trình đơn hàng trực quan:**\n' +
                '   • Đăng nhập tài khoản và vào mục **[Đơn Hàng Của Tôi](/my-orders)**.\n' +
                '   • Nhấn chọn **\'Xem Tiến Độ (Timeline)\'** tại đơn hàng muốn kiểm tra.\n' +
                '   • Hệ thống sẽ hiển thị timeline 5 bước: *Chờ xác nhận ➔ Đã xác nhận ➔ Đang đóng gói ➔ Đang giao hàng ➔ Giao thành công*.\n\n' +
                '2. **Chính sách Hủy đơn hàng:**\n' +
                '   • Quý khách có thể tự thao tác **\'Hủy Đơn Hàng\'** khi đơn đang ở trạng thái **Chờ xác nhận**.\n' +
                '   • Khi xác nhận hủy, số lượng sách sẽ được hệ thống **tự động hoàn tồn kho** ngay lập tức.'
        },
        {
            id: 'branches',
            icon: 'fa-map-location-dot',
            question: 'Nhà sách Book4Life có những chi nhánh nào và mở cửa đến mấy giờ?',
            shortTitle: 'Hệ thống chi nhánh & giờ mở cửa',
            category: 'Cửa hàng',
            answer: '📍 **Hệ Thống 3 Chi Nhánh Book4Life tại TP.HCM:**\n\n' +
                '• **Chi nhánh Quận 1 (Trụ Sở Chính):**\n  120 Nguyễn Huệ, P. Bến Nghé, Quận 1 • Hotline: 028 3822 1234\n' +
                '• **Chi nhánh TP. Thủ Đức:**\n  45 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức • Hotline: 028 3722 5678\n' +
                '• **Chi nhánh Bình Thạnh:**\n  235 Bạch Đằng, P. 15, Q. Bình Thạnh • Hotline: 028 3511 9012\n\n' +
                '⏰ **Thời gian hoạt động:** Mở cửa từ **08:00 – 21:30** tất cả các ngày trong tuần (kể cả Thứ 7, Chủ Nhật và ngày lễ).\n' +
                'Tại mỗi chi nhánh đều có góc đọc sách xưa và quầy tiếp nhận sách cũ ký gửi trực tiếp!'
        },
        {
            id: 'grading',
            icon: 'fa-book-bookmark',
            question: 'Tiêu chuẩn phân loại chất lượng sách cũ (Like New, Good,...) như thế nào?',
            shortTitle: 'Tiêu chuẩn phân loại sách cũ',
            category: 'Chất lượng',
            answer: '📖 **Tiêu Chuẩn Phân Loại Sách Cũ tại Book4Life:**\n\n' +
                'Mọi cuốn sách khi nhập kho đều được thẩm định nghiêm ngặt theo 5 cấp độ chất lượng:\n' +
                '• **LIKE_NEW (Như mới 95-99%):** Sách đẹp hoàn hảo, bìa phẳng, gáy vuông vức, giấy trắng không tì vết.\n' +
                '• **VERY_GOOD (Rất tốt 85-94%):** Sách nguyên vẹn, gáy thẳng, không ghi chú, có thể có vệt ố vàng tự nhiên rất nhẹ ở mép ngoài.\n' +
                '• **GOOD (Tốt 70-84%):** Bìa hơi sờn nhẹ, giấy ngả màu thời gian nhưng trọn vẹn từng trang, đọc rất tốt.\n' +
                '• **ACCEPTABLE (Chấp nhận được 50-69%):** Sách có nếp gập gáy hoặc ghi chú bằng bút chì, nhưng nội dung đầy đủ.\n' +
                '• **COLLECTIBLE (Sưu tầm / Sách xưa quý hiếm):** Ấn bản xuất bản trước 1975, bản in đầu tiên (First Edition) hoặc có thủ bút, chữ ký của tác giả.'
        },
        {
            id: 'coupon',
            icon: 'fa-ticket',
            question: 'Chính sách miễn phí vận chuyển (Freeship) và mã giảm giá áp dụng ra sao?',
            shortTitle: 'Mã giảm giá & Freeship',
            category: 'Ưu đãi',
            answer: '🎁 **Chương Trình Khuyến Mãi & Coupon Ưu Đãi:**\n\n' +
                '• **Freeship toàn quốc:** Tự động áp dụng phí vận chuyển 0đ cho đơn hàng có giá trị từ **250.000đ** trở lên.\n' +
                '• **Tặng Coupon 15%:** Dành cho khách hàng lần đầu hoàn thành bán sách cũ (trade-in).\n' +
                '• **Cách nhập mã giảm giá:** Truy cập trang **[Giỏ Hàng](/cart)**, điền mã coupon vào ô *Mã Giảm Giá* rồi bấm **Áp Dụng** trước khi xác nhận đơn hàng.'
        },
        {
            id: 'payment',
            icon: 'fa-truck-fast',
            question: 'Tôi có thể thanh toán bằng những hình thức nào và bao lâu nhận được sách?',
            shortTitle: 'Thanh toán & Giao nhận sách',
            category: 'Giao hàng',
            answer: '🚚 **Phương Thức Thanh Toán & Thời Gian Giao Nhận:**\n\n' +
                '• **Hình thức thanh toán linh hoạt:**\n' +
                '  1. Nhận hàng kiểm tra rồi trả tiền mặt (*COD*).\n' +
                '  2. Thanh toán trực tuyến chuyển khoản ngân hàng / VNPay an toàn.\n' +
                '• **Thời gian giao sách:**\n' +
                '  • Nội thành TP.HCM: Giao nhanh từ **24h – 48h**.\n' +
                '  • Các tỉnh thành khác: Giao từ **2 – 4 ngày** làm việc.\n' +
                '• **Cam kết đóng gói:** Toàn bộ sách cũ đều được bọc màng co và chèn đệm xốp chống va đập góc bìa khi vận chuyển.'
        },
        {
            id: 'literature',
            icon: 'fa-book',
            question: 'Nhà sách có những tác phẩm văn học Việt Nam nào đặc sắc nên đọc?',
            shortTitle: 'Gợi ý tác phẩm văn học hay',
            category: 'Sách hay',
            answer: '📚 **Tủ Sách Văn Học Tiêu Biểu tại Book4Life:**\n\n' +
                'Book4Life tự hào lưu giữ các ấn bản văn học có giá trị vượt thời gian:\n' +
                '• **Văn học hiện thực:** *Số Đỏ*, *Giông Tố* (Vũ Trọng Phụng); Tuyển tập truyện ngắn Nam Cao (*Chí Phèo*, *Lão Hạc*).\n' +
                '• **Thơ & Tùy bút kinh điển:** *Phấn Thông Vàng* (Xuân Diệu); *Trời Mỗi Ngày Lại Sáng* (Huy Cận); *Hà Nội 36 Phố Phường* (Thạch Lam).\n' +
                '• **Văn học Chiến tranh & Ký ức:** *Tuổi Thơ Dữ Dội*, *Ba Phút Sự Thật* (Phùng Quán); *Nỗi Buồn Chiến Tranh* (Bảo Ninh); *Ăn Mày Dĩ Vãng* (Chu Lai).\n' +
                '• **Văn hóa & Đất rừng phương Nam:** *Đò Dọc* (Bình Nguyên Lộc); *Hương Rừng Cà Mau* (Sơn Nam).\n\n' +
                '👉 Mời bạn ghé xem trọn bộ tại **[Kho Sách Cũ](/books)** và **[Danh Mục Tác Giả](/authors)**.'
        },
        {
            id: 'contact',
            icon: 'fa-headset',
            question: 'Làm thế nào để liên hệ trực tiếp nhân viên hỗ trợ hoặc hotline cửa hàng?',
            shortTitle: 'Liên hệ Hotline & CSKH',
            category: 'Hotline',
            answer: '📞 **Kênh Hỗ Trợ Trực Tiếp Khách Hàng Book4Life:**\n\n' +
                '• **Hotline tư vấn nhanh:** **1900 6868** (Nhánh 1: Mua hàng, Nhánh 2: Thu mua sách cũ)\n' +
                '• **Email hỗ trợ:** admin.book4life@gmail.com\n' +
                '• **Thời gian tiếp nhận:** 08:00 – 21:30 hàng ngày.\n\n' +
                'Ngoài ra, bạn cũng có thể gõ trực tiếp câu hỏi vào ô chat này, hệ thống sẽ tự động gửi email chuyển tiếp nội dung đến ban quản trị để hỗ trợ bạn qua email sớm nhất nhé!'
        }
    ];

    let faqList = DEFAULT_FAQ_LIST;

    // DOM Elements
    let toggleBtn, chatWindow, closeBtn, clearBtn, messagesContainer, chatForm, chatInput, sendBtn, typingIndicator;
    let faqPopup, faqPopupList, faqPopupClose;

    document.addEventListener('DOMContentLoaded', initChatbox);

    function initChatbox() {
        toggleBtn = document.getElementById('b4l-chat-toggle-btn');
        chatWindow = document.getElementById('b4l-chat-window');
        closeBtn = document.getElementById('b4l-chat-close-btn');
        clearBtn = document.getElementById('b4l-chat-clear-btn');
        messagesContainer = document.getElementById('b4l-chat-messages');
        chatForm = document.getElementById('b4l-chat-form');
        chatInput = document.getElementById('b4l-chat-input');
        sendBtn = document.getElementById('b4l-chat-send-btn');
        typingIndicator = document.getElementById('b4l-chat-typing');

        faqPopup = document.getElementById('b4l-faq-popup');
        faqPopupList = document.getElementById('b4l-faq-popup-list');
        faqPopupClose = document.getElementById('b4l-faq-popup-close');

        if (!toggleBtn || !chatWindow) return;

        // Bắt sự kiện Toggle / Đóng / Mở
        toggleBtn.addEventListener('click', toggleChat);
        if (closeBtn) closeBtn.addEventListener('click', closeChat);

        // Bắt sự kiện Xóa hội thoại
        if (clearBtn) {
            clearBtn.addEventListener('click', handleClearHistory);
        }

        // Bắt sự kiện Gửi tin nhắn tự gõ
        if (chatForm) {
            chatForm.addEventListener('submit', function (e) {
                e.preventDefault();
                sendMessage();
            });
        }

        // Bắt phím Enter
        if (chatInput) {
            chatInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    sendMessage();
                }
            });

            // 🎯 YÊU CẦU: Bắt sự kiện khi click / focus vào ô nhập liệu
            chatInput.addEventListener('focus', function () {
                showFaqPopup();
            });
            chatInput.addEventListener('click', function () {
                showFaqPopup();
            });
        }

        // Đóng FAQ Pop-up khi nhấn nút X
        if (faqPopupClose) {
            faqPopupClose.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                hideFaqPopup();
            });
        }

        // Đóng FAQ Pop-up khi click bên ngoài khung
        document.addEventListener('click', function (e) {
            if (!faqPopup || faqPopup.classList.contains('hidden')) return;
            if (!faqPopup.contains(e.target) && e.target !== chatInput) {
                hideFaqPopup();
            }
        });

        // Đóng khi nhấn phím Escape
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                hideFaqPopup();
            }
        });

        // Gán sự kiện cho các nút Quick Chips có sẵn trong lời chào mặc định
        document.querySelectorAll('.b4l-quick-chip').forEach(chip => {
            chip.addEventListener('click', function () {
                const prompt = this.getAttribute('data-prompt') || this.innerText.trim();
                handleAskFaqQuestion(prompt);
            });
        });

        // Tải danh sách FAQ từ backend nếu có endpoint
        fetchFaqListFromBackend();
    }

    async function fetchFaqListFromBackend() {
        try {
            const res = await fetch('/api/chat/faq');
            if (res.ok) {
                const data = await res.json();
                if (Array.isArray(data) && data.length > 0) {
                    faqList = data;
                    isFaqRendered = false; // Yêu cầu render lại với dữ liệu mới
                }
            }
        } catch (e) {
            // Giữ lại DEFAULT_FAQ_LIST dự phòng
        }
    }

    /**
     * Hiển thị menu Pop-up FAQ
     */
    function showFaqPopup() {
        if (!faqPopup) return;

        if (!isFaqRendered) {
            renderFaqPopupItems();
        }

        faqPopup.classList.remove('hidden');
        faqPopup.classList.add('faq-popup-open');
    }

    /**
     * Ẩn menu Pop-up FAQ
     */
    function hideFaqPopup() {
        if (!faqPopup) return;
        faqPopup.classList.remove('faq-popup-open');
        faqPopup.classList.add('hidden');
    }

    /**
     * Render danh sách các câu hỏi thường gặp vào popup
     */
    function renderFaqPopupItems() {
        if (!faqPopupList) return;
        faqPopupList.innerHTML = '';

        faqList.forEach(faq => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'b4l-faq-btn group';
            btn.innerHTML = `
                <i class="fas ${faq.icon || 'fa-circle-question'}"></i>
                <span class="b4l-faq-btn-text">${escapeHtml(faq.question)}</span>
                <span class="b4l-faq-category-badge">${escapeHtml(faq.category || 'Hỏi đáp')}</span>
            `;

            btn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                hideFaqPopup();
                handleAskFaqQuestion(faq.question, faq.answer);
            });

            faqPopupList.appendChild(btn);
        });

        isFaqRendered = true;
    }

    /**
     * XỬ LÝ CHỌN CÂU HỎI CÓ SẴN (FAQ Rule-based):
     * Tự động in câu hỏi ra khung chat và hiển thị câu trả lời có sẵn tương ứng.
     */
    async function handleAskFaqQuestion(questionText, preScriptedAnswer = null) {
        if (isWaitingResponse) return;

        // Ẩn lời chào mặc định
        const defaultWelcome = document.getElementById('b4l-default-welcome');
        if (defaultWelcome) defaultWelcome.classList.add('hidden');

        // 1. In câu hỏi của người dùng ra khung chat
        appendMessageBubble('USER', questionText, true);
        scrollToBottom();

        // 2. Hiển thị typing indicator
        setWaitingState(true);

        try {
            // Gửi lên backend để lưu vào lịch sử DB và nhận câu trả lời kịch bản chuẩn
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    message: questionText,
                    sessionId: sessionId
                })
            });

            if (response.ok) {
                const data = await response.json();
                const botReply = (data && data.reply) || preScriptedAnswer || 'Dạ em đã ghi nhận thông tin của bạn!';
                appendMessageBubble('ASSISTANT', botReply, true);
            } else if (preScriptedAnswer) {
                // Fallback nếu kết nối mạng chậm
                appendMessageBubble('ASSISTANT', preScriptedAnswer, true);
            } else {
                appendMessageBubble('ASSISTANT', 'Dạ hệ thống đang gián đoạn kết nối một chút. Quý khách vui lòng thử lại sau giây lát nhé! 🌿', true);
            }
        } catch (error) {
            console.error('[Chatbox FAQ] Lỗi:', error);
            if (preScriptedAnswer) {
                appendMessageBubble('ASSISTANT', preScriptedAnswer, true);
            } else {
                appendMessageBubble('ASSISTANT', 'Dạ hệ thống đang gián đoạn kết nối một chút. Quý khách vui lòng thử lại sau giây lát nhé! 🌿', true);
            }
        } finally {
            setWaitingState(false);
            scrollToBottom();
        }
    }

    /**
     * XỬ LÝ CÂU HỎI TỰ GÕ (Gửi Email chuyển tiếp Hotline):
     * Gửi nội dung tin nhắn kèm thông tin người dùng thẳng đến Email Quản trị (Hotline).
     * Chatbot phản hồi: "Dạ, câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Chúng tôi sẽ phản hồi lại bạn qua Email đăng ký trong thời gian sớm nhất nhé!"
     */
    async function sendMessage() {
        if (isWaitingResponse) return;

        const text = chatInput ? chatInput.value.trim() : '';
        if (!text) return;

        // Đóng pop-up FAQ
        hideFaqPopup();

        // Xóa ô nhập liệu
        chatInput.value = '';

        // Ẩn lời chào mặc định
        const defaultWelcome = document.getElementById('b4l-default-welcome');
        if (defaultWelcome) defaultWelcome.classList.add('hidden');

        // 1. Thêm tin nhắn của User lên giao diện
        appendMessageBubble('USER', text, true);
        scrollToBottom();

        // 2. Hiển thị typing indicator & disable nút gửi
        setWaitingState(true);

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 12000);

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    message: text,
                    sessionId: sessionId
                }),
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            if (!response.ok) {
                throw new Error(`HTTP Error ${response.status}`);
            }

            const data = await response.json();
            const botReply = (data && data.reply) ||
                             'Dạ, câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Chúng tôi sẽ phản hồi lại bạn qua Email đăng ký trong thời gian sớm nhất nhé!';

            // 3. Hiển thị phản hồi từ Chatbot
            appendMessageBubble('ASSISTANT', botReply, true);
        } catch (error) {
            console.error('[Chatbox] Lỗi gửi câu hỏi:', error);
            // Phản hồi thông báo đã ghi nhận chuyển tiếp
            appendMessageBubble('ASSISTANT', 'Dạ, câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Chúng tôi sẽ phản hồi lại bạn qua Email đăng ký trong thời gian sớm nhất nhé!', true);
        } finally {
            setWaitingState(false);
            scrollToBottom();
        }
    }

    function toggleChat() {
        if (chatWindow.classList.contains('hidden')) {
            openChat();
        } else {
            closeChat();
        }
    }

    function openChat() {
        chatWindow.classList.remove('hidden', 'chat-closing');
        chatWindow.classList.add('chat-opening');
        if (chatInput) {
            setTimeout(() => chatInput.focus(), 150);
        }

        // Tải lịch sử chat ở lần mở đầu tiên
        if (!isHistoryLoaded) {
            loadChatHistory();
        } else {
            scrollToBottom();
        }
    }

    function closeChat() {
        hideFaqPopup();
        chatWindow.classList.add('chat-closing');
        setTimeout(() => {
            chatWindow.classList.add('hidden');
            chatWindow.classList.remove('chat-closing', 'chat-opening');
        }, 250);
    }

    /**
     * Tải lịch sử trò chuyện từ backend
     */
    async function loadChatHistory() {
        try {
            const res = await fetch(`/api/chat/history?sessionId=${encodeURIComponent(sessionId)}`);
            if (res.ok) {
                const raw = await res.json();
                const history = Array.isArray(raw) ? raw : (raw && Array.isArray(raw.data) ? raw.data : []);
                if (history.length > 0) {
                    const defaultWelcome = document.getElementById('b4l-default-welcome');
                    if (defaultWelcome) {
                        defaultWelcome.classList.add('hidden');
                    }

                    history.forEach(msg => {
                        appendMessageBubble(msg.senderType, msg.content, false);
                    });
                    scrollToBottom();
                }
            }
        } catch (err) {
            console.warn('[Chatbox] Không thể tải lịch sử chat:', err);
        } finally {
            isHistoryLoaded = true;
        }
    }

    /**
     * Hiển thị bong bóng tin nhắn
     */
    function appendMessageBubble(senderType, content, animate = false) {
        if (!messagesContainer) return;

        const isUser = senderType === 'USER';
        const wrapper = document.createElement('div');
        wrapper.className = `flex ${isUser ? 'justify-end' : 'justify-start'} items-end gap-2 ${animate ? 'transition-all duration-300' : ''}`;

        let html = '';
        if (isUser) {
            html = `
                <div class="max-w-[82%] bg-[#835500] text-white rounded-2xl rounded-br-none px-4 py-2.5 shadow-sm text-sm break-words leading-relaxed">
                    ${escapeHtml(content).replace(/\n/g, '<br/>')}
                </div>
                <div class="w-6 h-6 rounded-full bg-[#f6ded1] text-[#835500] flex-shrink-0 flex items-center justify-center text-[10px] font-bold mb-1 shadow-xs">
                    <i class="fas fa-user"></i>
                </div>
            `;
        } else {
            html = `
                <div class="w-7 h-7 rounded-full bg-gradient-to-tr from-[#835500] to-[#f5a623] text-white flex-shrink-0 flex items-center justify-center text-xs shadow-sm mb-1">
                    <i class="fas fa-robot"></i>
                </div>
                <div class="max-w-[85%] bg-white text-[#2D261E] border border-[#e8e0d5] rounded-2xl rounded-bl-none px-4 py-3 shadow-sm text-sm break-words leading-relaxed b4l-msg-ai-content">
                    ${formatMarkdown(content)}
                </div>
            `;
        }

        wrapper.innerHTML = html;

        // Chèn trước typing indicator
        if (typingIndicator && typingIndicator.parentNode === messagesContainer) {
            messagesContainer.insertBefore(wrapper, typingIndicator);
        } else {
            messagesContainer.appendChild(wrapper);
        }
    }

    /**
     * Xóa lịch sử trò chuyện
     */
    async function handleClearHistory() {
        if (!confirm('Bạn có muốn làm mới cuộc trò chuyện này không?')) {
            return;
        }

        try {
            await fetch(`/api/chat/history?sessionId=${encodeURIComponent(sessionId)}`, {
                method: 'DELETE'
            });

            sessionId = 'session-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
            localStorage.setItem(SESSION_STORAGE_KEY, sessionId);

            messagesContainer.querySelectorAll('div.flex').forEach(el => {
                if (el !== typingIndicator) el.remove();
            });

            const defaultWelcome = document.getElementById('b4l-default-welcome');
            if (defaultWelcome) {
                defaultWelcome.classList.remove('hidden');
            }

            scrollToBottom();
        } catch (err) {
            console.error('[Chatbox] Không thể xóa lịch sử:', err);
        }
    }

    function setWaitingState(waiting) {
        isWaitingResponse = waiting;
        if (typingIndicator) {
            typingIndicator.classList.toggle('hidden', !waiting);
        }
        if (sendBtn) {
            sendBtn.disabled = waiting;
            sendBtn.innerHTML = waiting
                ? '<i class="fas fa-spinner fa-spin"></i>'
                : '<i class="fas fa-paper-plane"></i>';
        }
    }

    function scrollToBottom() {
        if (!messagesContainer) return;
        setTimeout(() => {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }, 50);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    /**
     * Chuyển đổi Markdown cơ bản sang HTML an toàn
     */
    function formatMarkdown(text) {
        if (!text) return '';
        let escaped = escapeHtml(text);

        // Chuyển bold **text** -> <strong>text</strong>
        escaped = escaped.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

        // Chuyển italic *text* -> <em>text</em>
        escaped = escaped.replace(/\*(.*?)\*/g, '<em>$1</em>');

        // Chuyển links [text](url) -> <a href="url" target="_blank">text</a>
        escaped = escaped.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

        // Chuyển bullet points • hoặc -
        const lines = escaped.split('\n');
        let formatted = '';
        let inList = false;

        lines.forEach(line => {
            const trimmed = line.trim();
            if (trimmed.startsWith('• ') || trimmed.startsWith('- ')) {
                if (!inList) {
                    formatted += '<ul class="my-1.5 space-y-1 list-disc pl-5">';
                    inList = true;
                }
                formatted += `<li>${trimmed.substring(2)}</li>`;
            } else {
                if (inList) {
                    formatted += '</ul>';
                    inList = false;
                }
                if (trimmed.length > 0) {
                    formatted += `<p class="mb-1.5 last:mb-0">${line}</p>`;
                }
            }
        });

        if (inList) {
            formatted += '</ul>';
        }

        return formatted;
    }
})();

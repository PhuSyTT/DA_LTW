# TÀI LIỆU TỔNG HỢP CÁC NHÓM CHỨC NĂNG HỆ THỐNG
## DỰ ÁN: HỆ THỐNG QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ ĐA CHI NHÁNH (BOOK4LIFE)

---

## MỤC LỤC
1. [TỔNG QUAN HỆ THỐNG & PHÂN QUYỀN VAI TRÒ (RBAC)](#1-tổng-quan-hệ-thống--phân-quyền-vai-trò-rbac)
2. [KIẾN TRÚC TỔNG THỂ 3 PHÂN HỆ NGHIỆP VỤ](#2-kiến-trúc-tổng-thể-3-phân-hệ-nghiệp-vụ)
3. [CHI TIẾT PHÂN HỆ 1: CỔNG TRẢI NGHIỆM ĐỘC GIẢ (CUSTOMER PORTAL)](#3-chi-tiết-phân-hệ-1-cổng-trải-nghiệm-độc-giả-customer-portal)
4. [CHI TIẾT PHÂN HỆ 2: VẬN HÀNH & BÁN LẺ CHI NHÁNH (BRANCH OPERATIONS & FULFILLMENT)](#4-chi-tiết-phân-hệ-2-vận-hành--bán-lẻ-chi-nhánh-branch-operations--fulfillment)
5. [CHI TIẾT PHÂN HỆ 3: QUẢN TRỊ CHUỖI & ĐỘNG CƠ NỀN TẢNG (HQ GOVERNANCE & PLATFORM ENGINES)](#5-chi-tiết-phân-hệ-3-quản-trị-chuỗi--động-cơ-nền-tảng-hq-governance--platform-engines)
6. [HẠ TẦNG EMAIL GIAO DỊCH ĐA KÊNH & KHẢ NĂNG TƯƠNG THÍCH CLOUD](#6-hạ-tầng-email-giao-dịch-đa-kênh--khả-năng-tương-thích-cloud)
7. [BẢNG ĐỐI SOÁT MÃ CHỨC NĂNG THEO SRS CHUẨN HÓA (REQUIREMENTS MATRIX)](#7-bảng-đối-soát-mã-chức-năng-theo-srs-chuẩn-hóa-requirements-matrix)
8. [NGĂN XẾP CÔNG NGHỆ & TỐI ƯU HIỆU NĂNG TRIỂN KHAI](#8-ngăn-xếp-công-nghệ--tối-ưu-hiệu-năng-triển-khai)

---

## 1. TỔNG QUAN HỆ THỐNG & PHÂN QUYỀN VAI TRÒ (RBAC)

**Book4Life** là giải pháp phần mềm chuyên sâu kết hợp hoàn hảo giữa **Thương mại điện tử B2C**, **Thu cũ đổi mới từ độc giả C2B (Trade-in / Buy-back)** và **Hệ thống ERP thu nhỏ**, phục vụ quản trị chuỗi cung ứng, kho bãi vật lý và tài chính cho mô hình chuỗi cửa hàng sách cũ đa chi nhánh.

### Ma trận Phân quyền Vai trò Người dùng (Role-Based Access Control)
| Vai trò (Role) | Ký hiệu hệ thống | Phạm vi & Trách nhiệm chính |
| :--- | :--- | :--- |
| **Quản trị viên chuỗi** | `ROLE_ADMIN` | Quản trị dữ liệu gốc (MDM), cấu hình voucher toàn chuỗi, duyệt tài khoản, phân bổ nhân sự, giám sát báo cáo tài chính tổng hợp, quản trị xóa tài khoản an toàn (Cascade Deletion). |
| **Quản lý chi nhánh** | `ROLE_BRANCH_MANAGER` | Quản lý vận hành chi nhánh phụ trách, giám sát đơn hàng, tạo và duyệt yêu cầu luân chuyển kho nội bộ, quản lý tài xế và nhân viên tại chỗ. |
| **Chuyên viên thẩm định** | `ROLE_STAFF_APPRAISER` | Tiếp nhận yêu cầu bán sách từ khách, thẩm định độ cũ (Grade), định giá mua, đối soát thực tế tại quầy, duyệt nhập kho và tạo phiếu chi. |
| **Nhân viên bán lẻ & kho** | `ROLE_STAFF_WAREHOUSE` / `STAFF_GENERAL` | Nhặt sách đóng gói theo đơn online, cập nhật vị trí kệ sách, bán hàng tại quầy (POS), xác nhận xuất/nhập phiếu luân chuyển. |
| **Tài xế giao vận** | `ROLE_STAFF_DRIVER` | Tiếp nhận đơn giao từ chi nhánh, cập nhật tiến độ giao vận nội thành, xác nhận giao hàng thành công. |
| **Khách hàng / Độc giả** | `ROLE_CUSTOMER` | Tìm sách theo chi nhánh, mua hàng online, gửi yêu cầu thanh lý sách cũ, săn sách hết hàng, đánh giá chất lượng và chat cùng AI. |

---

## 2. KIẾN TRÚC TỔNG THỂ 3 PHÂN HỆ NGHIỆP VỤ

Hệ thống được thiết kế theo mô hình 3 phân hệ tương hỗ khép kín:

```mermaid
flowchart TD
    subgraph P1 [PHÂN HỆ 1: CỔNG TRẢI NGHIỆM ĐỘC GIẢ]
        A1[Tra cứu & Khám phá sách cũ] --> A2[Giỏ hàng & Đặt hàng đa chi nhánh]
        A3[Khảo sát & Bán lại sách cũ Trade-in] --> A4[Săn sách Wishlist & Cảnh báo Restock]
        A5[Đánh giá & Xếp hạng sản phẩm] --> A6[Trợ lý ảo Google Gemini AI]
    end

    subgraph P2 [PHÂN HỆ 2: VẬN HÀNH & BÁN LẺ CHI NHÁNH]
        B1[Quản lý Tồn kho SKU & Kệ sách] --> B2[Xử lý Đơn hàng 4 bước Fulfillment]
        B3[Bán hàng tại quầy POS Barcode] --> B4[Thẩm định sách & Nhập kho Phiếu chi]
        B5[Luân chuyển kho liên chi nhánh]
    end

    subgraph P3 [PHÂN HỆ 3: QUẢN TRỊ CHUỖI & ĐỘNG CƠ NỀN TẢNG]
        C1[Quản trị Dữ liệu gốc MDM] --> C2[Động cơ Tách đơn tự động Order Splitting]
        C3[Động cơ Khuyến mãi đa điều kiện] --> C4[Dashboard Phân tích & Báo cáo Doanh thu]
        C5[Nhật ký kiểm toán Audit Trail] --> C6[Bảo mật 2 lớp OTP & Google OAuth2]
    end

    P1 <-->|Đặt hàng online & Hồ sơ thanh lý| P2
    P2 <-->|Dữ liệu tồn kho & Doanh thu thực tế| P3
    P3 <-->|Dữ liệu chuẩn hóa & Chính sách giá/khuyến mãi| P1
```

---

## 3. CHI TIẾT PHÂN HỆ 1: CỔNG TRẢI NGHIỆM ĐỘC GIẢ (CUSTOMER PORTAL)

> **Mục tiêu:** Mang lại trải nghiệm tìm kiếm minh bạch, quy trình mua sắm tiện lợi và khả năng tương tác hai chiều (Mua B2C & Bán C2B).

### 3.1. Tìm kiếm & Khám phá Sách cũ Trực quan
* **Tra cứu theo chi nhánh:** Xem chính xác cuốn sách đang có mặt tại cửa hàng nào trong chuỗi (Chi nhánh Quận 1, Quận 5, Thủ Đức, v.v.).
* **Thang đo độ cũ minh bạch (Grading Scale):**
  * `LIKE_NEW` (99% - Như mới, bìa phẳng phiu, không tỳ vết).
  * `VERY_GOOD` (90% - Rất tốt, gáy nguyên vẹn, giấy sáng).
  * `GOOD` (80% - Tốt, dấu thời gian nhẹ, không rách trang).
  * `ACCEPTABLE` (60% - Đọc tốt, có thể ngả vàng hoặc có vết chú thích).
* **Hình ảnh thực tế đa góc cạnh từ Cloudinary:** Cho phép độc giả xem chi tiết từng mép sách, gáy sách trước khi quyết định mua.

### 3.2. Giỏ hàng Thông minh & Đặt hàng Linh hoạt
* **Giỏ hàng nguồn gốc chi nhánh:** Tự động gom nhóm sách theo cửa hàng sở hữu.
* **Hình thức nhận sách đa dạng:**
  * **Giao tận nơi (COD):** Tự động tính phí vận chuyển theo Tỉnh/Thành phố dựa trên địa chỉ khách chọn.
  * **Giữ sách tại quầy (Click & Collect):** Độc giả đặt trước và ghé chi nhánh gần nhất nhận sách trong 48 giờ hoàn toàn miễn phí vận chuyển.
* **Tối ưu form điền thông tin:** Sử dụng placeholder mờ hiển thị tên trường rõ ràng (Họ và tên, Số điện thoại, Địa chỉ, Ghi chú), không để dữ liệu mẫu gây nhầm lẫn.

### 3.3. Quy trình Thu Cũ Đổi Mới (C2B Trade-in / Buy-back)
* **Khảo sát trực tuyến:** Khách hàng điền tựa sách, thể loại, năm xuất bản và tải lên 6 ảnh chụp thực tế (bìa trước, bìa sau, gáy sách, mép giấy, trang lót, vết tỳ vết).
* **Thuật toán định giá sơ bộ tự động (*Auto-Appraisal Algorithm*):** Hệ thống tự động tính giá đề xuất ban đầu dựa trên giá bìa gốc, năm xuất bản và tỷ lệ trừ theo mức độ hư hại.
* **Theo dõi tiến trình trực tiếp:** Nhận thông báo kết quả thẩm định chính thức và mã biên lai để mang sách đến quầy nhận tiền mặt hoặc đổi lấy voucher mua sách mới.

### 3.4. Tiện ích Giữ chân & Chăm sóc Khách hàng
* **"Săn sách" & Cảnh báo sách về kho (Wishlist & Restock Alert):** Độc giả đăng ký theo dõi các cuốn sách hiếm đã hết hàng. Khi bất kỳ chi nhánh nào nhập kho, hệ thống tự động gửi Email thông báo kèm link chi tiết sách.
* **Đánh giá & Xếp hạng sản phẩm (Reviews & Ratings):** Khách hàng sau khi hoàn tất đơn được quyền gửi đánh giá thực tế, chấm điểm sao (mặc định 5 sao) và nhận xét chất lượng bảo quản sách.
* **Trợ lý ảo thông minh Google Gemini AI:** Chatbot tích hợp trực tiếp trên web, đóng vai trò "thủ thư ảo" tư vấn sách theo gu đọc, kiểm tra tồn kho và giải đáp chính sách 24/7. Hỗ trợ tính năng **Chuyển tiếp cho nhân viên thật (Human Handoff)** qua email hotline tự động.

---

## 4. CHI TIẾT PHÂN HỆ 2: VẬN HÀNH & BÁN LẺ CHI NHÁNH (BRANCH OPERATIONS & FULFILLMENT)

> **Mục tiêu:** Chuẩn hóa quy trình vận hành tại từng cửa hàng, đảm bảo tính chính xác về tồn kho vật lý và tốc độ hoàn tất đơn hàng.

### 4.1. Quản lý Tồn kho Vật lý & Định vị Kệ sách (Branch Inventory)
* **Kiến trúc sách 2 tầng (2-Tier Architecture):** Tách biệt rõ ràng giữa *Đầu sách gốc (Book Master)* và các *Bản sao vật lý thực tế (Book Item / SKU)*.
* **Mã định danh SKU Barcode:** Mỗi cuốn sách khi vào kho được cấp mã vạch riêng gắn liền với độ cũ và giá bán riêng biệt.
* **Quản lý vị trí kệ (`shelfLocation`):** Lưu chính xác vị trí sách trên kệ (ví dụ: *Kệ A2 - Tầng 3 - Dãy Văn Học*), giúp nhân viên tìm và nhặt sách chỉ trong vài giây.

### 4.2. Xử lý & Hoàn tất Đơn hàng Đa bước (Order Fulfillment Pipeline)
Quy trình khép kín 4 bước rõ ràng:
1. **Tiếp nhận & Phê duyệt:** Nhân viên kiểm tra sách trên kệ và bấm `1. Phê Duyệt & Tiếp Nhận Đơn Hàng` (`PENDING` $\rightarrow$ `CONFIRMED`).
2. **Xuất kho & Đóng gói:** Bấm `2. Bắt Đầu Xuất Kho & Đóng Gói` (`PACKING`).
3. **Phân công vận chuyển:** Chọn tài xế nội bộ của chi nhánh hoặc chọn đơn vị giao vận (GHTK, GHN, AhaMove) kèm mã vận đơn (`SHIPPED`).
4. **Hoàn tất giao hàng:** Xác nhận khách đã nhận sách thành công (`COMPLETED`).
* **Hủy đơn & Hoàn kho tự động:** Trong trường hợp khách hủy đơn hoặc giao thất bại, hệ thống tự động hoàn trả số lượng sách về kho lưu trữ ban đầu bằng Database Transaction an toàn.

### 4.3. Thẩm định & Nhập kho Thu mua Sách cũ
* **Bàn làm việc của Chuyên viên thẩm định (Staff Appraiser):** Soi ảnh chụp phóng to chất lượng cao, đối chiếu bảng tiêu chí hư hại, chốt mức điểm Grade và giá mua chính thức.
* **Đối soát tại quầy:** Khi khách mang sách đến, nhân viên kiểm tra tình trạng thực tế so với hồ sơ online, bấm **Xác nhận nhập kho**, gán vị trí kệ và tự động lập phiếu chi tiền mặt cho khách.

### 4.4. Bán lẻ tại Quầy (Point of Sale - POS)
* Giao diện quẹt Barcode/SKU siêu tốc phục vụ khách mua trực tiếp tại cửa hàng.
* Tìm kiếm sách nhanh theo tên tác giả hoặc tựa đề, áp dụng mã voucher khuyến mãi và tính tiền thối tự động.

### 4.5. Luân chuyển Kho Liên Chi nhánh (Internal Stock Transfer)
* Quản lý chi nhánh lập phiếu đề xuất điều chuyển sách giữa các cửa hàng khi có sự chênh lệch nhu cầu.
* **Ràng buộc kiểm tra tự động:** Hệ thống tự động từ chối điều chuyển nếu cuốn sách đó mới nhập dưới 6 tháng mà tại chi nhánh nguồn chỉ còn duy nhất 1 cuốn (nhằm chống cạn kiệt nguồn sách độc bản).
* Luồng trạng thái chuẩn: *Đề xuất $\rightarrow$ Xuất kho nguồn $\rightarrow$ Đang vận chuyển $\rightarrow$ Nhập kho đích*.

---

## 5. CHI TIẾT PHÂN HỆ 3: QUẢN TRỊ CHUỖI & ĐỘNG CƠ NỀN TẢNG (HQ GOVERNANCE & PLATFORM ENGINES)

> **Mục tiêu:** Kiểm soát tập trung toàn bộ dữ liệu, tối ưu hóa các thuật toán phân bổ và bảo đảm an ninh hệ thống toàn chuỗi.

### 5.1. Quản trị Dữ liệu Gốc Tập trung (Master Data Management - MDM)
* Chuẩn hóa danh mục thống nhất: Tác giả, Thể loại và Đầu sách gốc toàn chuỗi.
* Tự động kiểm tra trùng lặp ISBN khi nhập liệu và công cụ tự sinh mã ISBN-13 chuẩn quốc tế.

### 5.2. Động cơ Định tuyến & Tách đơn Tự động (Order Routing & Splitting Engine)
* Khi giỏ hàng của khách hàng chứa các cuốn sách thuộc nhiều chi nhánh khác nhau, hệ thống tự động phân tách thành 1 Đơn hàng mẹ (`MasterOrder`) và các Đơn hàng con (`SubOrder`) tương ứng với từng chi nhánh sở hữu.
* Mỗi chi nhánh chỉ nhận và xử lý phần sách thuộc kho của mình, đảm bảo tính độc lập và minh bạch trong việc hạch toán doanh thu.

### 5.3. Động cơ Khuyến mãi Đa điều kiện (Coupons & Discounts Engine)
* Thiết lập đa dạng loại voucher:
  * Giảm theo % giá trị sách (kèm trần giảm tối đa).
  * Giảm số tiền cố định (VNĐ).
  * Freeship 100% hoặc giảm phí vận chuyển cố định.
* Cấu hình điều kiện lọc sâu:
  * Giá trị đơn hàng tối thiểu (`minOrderAmount`).
  * Giới hạn theo **Độ cũ của sách** (ví dụ: chỉ áp dụng cho sách cũ tình trạng `GOOD` hoặc `ACCEPTABLE` để kích cầu giải phóng hàng tồn).
  * Giới hạn chi nhánh áp dụng hoặc áp dụng toàn chuỗi.

### 5.4. Báo cáo Thống kê & Nhật ký Kiểm toán (Analytics & Audit Trail)
* **Analytics Dashboard:** Trực quan hóa doanh thu theo ngày/tháng, cơ cấu doanh thu theo chi nhánh, tỷ trọng sách bán chạy, tỷ lệ hoàn tất đơn hàng.
* **Audit Trail (Nhật ký kiểm toán):** Ghi nhận chi tiết lịch sử mọi hành vi quan trọng (ai đổi trạng thái đơn, thời gian thực hiện, ghi chú thao tác) phục vụ việc thanh tra và đối soát nội bộ.

### 5.5. Quản trị Xóa Tài Khoản Triệt Để (Cascade Deletion Governance)
* Khắc phục triệt để lỗi vi phạm khóa ngoại (Foreign Key Constraint Fails) khi xóa người dùng:
* Hệ thống triển khai quy trình xóa tầng phân cấp an toàn trong Transaction CSDL:
  1. `wishlists`
  2. `reviews`
  3. `ai_chat_messages`
  4. `trade_in_images` & `trade_in_requests` (gỡ liên kết nhân viên thẩm định)
  5. `stock_transfers` (gỡ liên kết nhân viên đề xuất)
  6. `order_status_history` (gỡ liên kết người cập nhật và xóa lịch sử đơn hàng của user)
  7. `coupon_usages`
  8. `sub_order_items`
  9. `sub_orders`
  10. `master_orders`
  11. `users` (Xóa chính thức tài khoản người dùng)

### 5.6. Hạ tầng Bảo mật & Xác thực Người dùng (Security & RBAC)
* **Xác thực OTP qua Email:** Mã hóa ngẫu nhiên 6 chữ số qua `SecureRandom`, lưu CSDL với thời hạn hiệu lực 5 phút, khóa sau 5 lần nhập sai để chống tấn công dò mã (Brute-force).
* **Đăng nhập Google 1-Click (Google Identity Services):** Đăng nhập trực tiếp bằng Google OAuth2, tự động giải mã JWT credential và cấp phiên làm việc Spring Security session.
* **Đo lường độ mạnh mật khẩu:** Ràng buộc an toàn bắt buộc mật khẩu phải `> 6 ký tự`, đồng thời chứa đủ chữ cái (a-z, A-Z), chữ số (0-9) và ký tự đặc biệt (!@#$...).
* **Kiến trúc Stateless:** Spring Security kết hợp JSON Web Token (JWT) lưu trữ an toàn trong HttpOnly Cookie, bảo vệ hệ thống trước các cuộc tấn công XSS và CSRF.

---

## 6. HẠ TẦNG EMAIL GIAO DỊCH ĐA KÊNH & KHẢ NĂNG TƯƠNG THÍCH CLOUD

> **Mục tiêu:** Đảm bảo 100% email giao dịch quan trọng (OTP, Chào mừng, Săn sách, Hotline) được phát đi thành công ngay cả trên các môi trường Cloud có tường lửa khắt khe.

### 6.1. Hỗ trợ Song Song Giao thức Kép: HTTPS REST API & SMTP
* **Ưu tiên 1: Brevo REST API v3 (Cổng HTTPS 443):**
  * Gọi trực tiếp tới `https://api.brevo.com/v3/smtp/email` qua giao thức HTTPS bảo mật.
  * **Giải pháp khắc phục triệt để việc Render chặn cổng SMTP:** Cổng 443 là cổng Web tiêu chuẩn nên hoàn toàn không bao giờ bị Render hay các nhà cung cấp Cloud (AWS, Vercel) chặn. Tốc độ phản hồi cực nhanh (~0.2 giây).
* **Ưu tiên 2: Fallback qua SMTP (Cổng 2525 / 587):**
  * Tự động chuyển đổi mượt mà sang `JavaMailSender` nếu chưa cấu hình API Key.
  * Sử dụng cổng chuyên dụng `2525` của Brevo để hạn chế tối đa việc xung đột với cổng `587`.
* **Ưu tiên 3: Hỗ trợ linh hoạt Gmail SMTP (`smtp.gmail.com`):** Dành cho môi trường phát triển cục bộ (Local Development) có App Password.

### 6.2. Bộ Email Mẫu Chuẩn Nhận Diện Thương Hiệu Book4Life
* **Email Mã xác nhận OTP Đăng ký:** Hiển thị mã 6 số to rõ ràng dạng `123 456`, kèm cảnh báo bảo mật và đồng hồ đếm ngược 5 phút.
* **Email Chào Mừng Độc Giả Mới (Welcome Email):** Tự động gửi tới email khách hàng vừa đăng ký thành công (hoặc đăng nhập Google lần đầu), tóm tắt các đặc quyền: mua sách cũ tiết kiệm 30%-70%, săn sách Wishlist, định giá thu cũ đổi mới bằng AI.
* **Email Cảnh Báo Săn Sách (Restock Alert):** Bắn thông báo ngay khi cuốn sách độc bản về kho kèm tên chi nhánh hiện có, tình trạng Grade và link xem chi tiết.
* **Email Chuyển Tiếp Hotline CSKH:** Định tuyến câu hỏi từ khung chat website về hộp thư nóng của ban quản trị để hỗ trợ độc giả kịp thời.

### 6.3. Công cụ Chẩn đoán Trực tiếp trên Cloud (`/api/test-email`)
* Cung cấp endpoint chẩn đoán thời gian thực `GET /api/test-email?to=...` giúp lập trình viên và quản trị viên kiểm tra tức thì trạng thái kết nối Brevo API, mã phản hồi HTTP và phát hiện ngay các lỗi về IP bảo mật hoặc cấu hình biến môi trường trên Render.

---

## 7. BẢNG ĐỐI SOÁT MÃ CHỨC NĂNG THEO SRS CHUẨN HÓA (REQUIREMENTS MATRIX)

| Mã Yêu cầu | Tên Chức Năng | Phân hệ | Đối tượng thao tác |
| :--- | :--- | :--- | :--- |
| **FR-AUTH-01** | Đăng ký tài khoản xác thực 2 bước qua Email OTP | Phân hệ 3 | Customer, Hệ thống |
| **FR-AUTH-02** | Đăng nhập trực tiếp 1-Click bằng tài khoản Google (OAuth2) | Phân hệ 3 | Customer, Hệ thống |
| **FR-AUTH-03** | Đánh giá độ mạnh mật khẩu và kiểm tra trùng lặp email thời gian thực | Phân hệ 3 | Customer |
| **FR-AUTH-04** | Tự động gửi Email Chào Mừng cho tài khoản mới kích hoạt | Phân hệ 3 | Hệ thống (Brevo API) |
| **FR-AUTH-05** | Phân quyền truy cập đa vai trò (RBAC 6 Roles) với JWT HttpOnly Cookie | Phân hệ 3 | Hệ thống, Toàn bộ Users |
| **FR-MDM-01** | Chuẩn hóa danh mục Đầu sách Master | Phân hệ 3 | Admin |
| **FR-MDM-02** | Quản lý Tác giả & Thể loại chuẩn | Phân hệ 3 | Admin |
| **FR-MDM-03** | Tra cứu & Kiểm tra trùng lặp ISBN | Phân hệ 3 | Admin, Staff |
| **FR-INV-01** | Tạo SKU mặt hàng sách cũ tại chi nhánh | Phân hệ 2 | Staff Warehouse |
| **FR-INV-02** | Chấm điểm tình trạng & Đặt giá bán riêng theo độ cũ | Phân hệ 2 | Staff Appraiser |
| **FR-INV-03** | Tải ảnh thực tế đa góc cạnh lên Cloudinary | Phân hệ 2 | Staff Appraiser |
| **FR-INV-04** | Quản lý Vị trí kệ sách (Shelf Location) | Phân hệ 2 | Staff Warehouse |
| **FR-ORD-01** | Giỏ hàng hiển thị nguồn gốc chi nhánh | Phân hệ 1 | Customer |
| **FR-ORD-02** | Thuật toán Tự động tách đơn (Order Splitting) | Phân hệ 3 | Hệ thống |
| **FR-ORD-03** | Trừ kho an toàn bằng Database Transaction | Phân hệ 2 | Hệ thống |
| **FR-ORD-04** | Đặt giữ sách tại quầy (Click & Collect) miễn phí ship | Phân hệ 1 | Customer, Staff |
| **FR-FUL-01** | Tiếp nhận & Phê duyệt đơn hàng tại chi nhánh | Phân hệ 2 | Staff Warehouse |
| **FR-FUL-02** | Quy trình đóng gói & Phân công tài xế giao vận | Phân hệ 2 | Staff, Driver |
| **FR-FUL-03** | Theo dõi tiến độ đơn hàng thời gian thực qua mã vận đơn | Phân hệ 1 | Customer |
| **FR-FUL-04** | Xử lý Hủy đơn & Tự động hoàn kho an toàn | Phân hệ 2 | Staff, Hệ thống |
| **FR-TRF-01** | Tạo phiếu đề xuất luân chuyển sách giữa các chi nhánh | Phân hệ 2 | Branch Manager |
| **FR-TRF-02** | Kiểm tra ràng buộc luân chuyển tự động (Chống cạn sách độc bản) | Phân hệ 3 | Hệ thống |
| **FR-TRF-03** | Quy trình Xuất kho nguồn - Vận chuyển - Nhập kho đích | Phân hệ 2 | Staff các chi nhánh |
| **FR-TRF-04** | Theo dõi lịch sử luân chuyển toàn chuỗi | Phân hệ 3 | Admin |
| **FR-WSH-01** | Đăng ký "Săn sách" khi sản phẩm hết hàng | Phân hệ 1 | Customer |
| **FR-WSH-02** | Tự động quét & Bắn Email thông báo có hàng (Restock Alert) | Phân hệ 1 | Hệ thống (Brevo / SMTP) |
| **FR-WSH-03** | Quản lý danh sách Săn sách cá nhân | Phân hệ 1 | Customer |
| **FR-CPN-01** | Thiết lập Mã giảm giá đa điều kiện (%, tiền mặt, freeship) | Phân hệ 3 | Admin |
| **FR-CPN-02** | Điều kiện giảm theo Độ cũ của sách (Grade) | Phân hệ 3 | Admin, Hệ thống |
| **FR-CPN-03** | Điều kiện giảm theo Chi nhánh áp dụng | Phân hệ 3 | Admin, Hệ thống |
| **FR-CPN-04** | Kiểm tra và Áp dụng mã trong Giỏ hàng | Phân hệ 1 | Customer, Hệ thống |
| **FR-TRD-01** | Điền thông tin khảo sát tình trạng sách cũ cần thanh lý | Phân hệ 1 | Customer |
| **FR-TRD-02** | Tải ảnh chụp thực tế 6 mặt lên Cloudinary | Phân hệ 1 | Customer |
| **FR-TRD-03** | Tự động tính giá định giá sơ bộ ban đầu | Phân hệ 1 | Hệ thống (Auto Algorithm) |
| **FR-TRD-04** | Chuyên viên thẩm định kiểm duyệt ảnh & Chốt giá chính thức | Phân hệ 2 | Staff Appraiser |
| **FR-TRD-05** | Khách hàng phản hồi kết quả định giá (Đồng ý / Từ chối) | Phân hệ 1 | Customer |
| **FR-TRD-06** | Nhận sách thực tế & Nhập kho - Tự động tạo phiếu chi tiền | Phân hệ 2 | Staff Appraiser |
| **FR-REV-01** | Đánh giá & Chấm điểm sao sau khi hoàn tất đơn hàng | Phân hệ 1 | Customer |
| **FR-REV-02** | Quản lý và duyệt đánh giá từ phía Admin | Phân hệ 3 | Admin |
| **FR-GEM-01** | Giao diện Chatbot thông minh trên Web | Phân hệ 1 | Customer, Guest |
| **FR-GEM-02** | Tư vấn tìm sách theo ngữ cảnh (Google Gemini AI) | Phân hệ 1 | Hệ thống, Google Gemini |
| **FR-GEM-03** | Giải đáp thắc mắc về độ cũ sách & chính sách thu cũ đổi mới | Phân hệ 1 | Hệ thống, Google Gemini |
| **FR-GEM-04** | Chuyển tiếp câu hỏi khó sang nhân viên thật qua Email Hotline | Phân hệ 1 | Customer, Staff |
| **FR-ADM-01** | Dashboard phân tích doanh thu và cơ cấu chi nhánh | Phân hệ 3 | Admin |
| **FR-ADM-02** | Nhật ký kiểm toán thao tác hệ thống (Audit Trail) | Phân hệ 3 | Admin |
| **FR-ADM-03** | Xóa tài khoản cascade toàn bộ đơn hàng và lịch sử liên quan | Phân hệ 3 | Admin |

---

## 8. NGĂN XẾP CÔNG NGHỆ & TỐI ƯU HIỆU NĂNG TRIỂN KHAI

### 8.1. Công nghệ Backend & Bảo mật
* **Ngôn ngữ & Nền tảng:** Java 21 LTS, Spring Boot 3.3.x.
* **Tầng dữ liệu:** Spring Data JPA, Hibernate ORM, HikariCP Connection Pool (tối ưu keep-alive 5 phút cho Cloud DB).
* **Bảo mật:** Spring Security 6, JWT (JSON Web Token) trong HttpOnly Cookie, mã hóa mật khẩu BCrypt, phòng chống tấn công CSRF, XSS và Brute-force.
* **Xử lý bất đồng bộ:** `@Async` với TaskExecutor chuyên biệt giúp gửi email không gây nghẽn luồng xử lý web.

### 8.2. Công nghệ Frontend & Giao diện
* **Template Engine:** Thymeleaf 3.x kết hợp Thymeleaf Extras Spring Security 6.
* **Framework UI:** Tailwind CSS, Bootstrap 5.3, Bootstrap Icons, Font Awesome 6.
* **Client-side Logic:** Vanilla JavaScript ES6+ (Async/Await, Fetch API, DOM Manipulation).

### 8.3. Dịch vụ Tích hợp & Đám mây (Cloud & Third-party Services)
* **Email Service:**
  * **Brevo REST API v3:** Giao tiếp qua cổng **HTTPS 443** (sử dụng `java.net.http.HttpClient` gốc của Java, miễn nhiễm 100% trước rào cản chặn cổng SMTP của Render).
  * **Brevo SMTP (Port 2525 / 587):** Giao thức truyền thống với JavaMailSender.
  * **Google SMTP (Gmail):** Dự phòng môi trường cục bộ.
* **Trí tuệ nhân tạo (AI):** Google Gemini 1.5 Flash API (xử lý ngôn ngữ tự nhiên, gợi ý sách thông minh).
* **Lưu trữ đa phương tiện:** Cloudinary CDN REST API (lưu trữ ảnh bìa sách và 6 góc ảnh thực tế của dịch vụ Trade-in).
* **Cơ sở dữ liệu đám mây:** Cloud Aiven MySQL Database / Microsoft SQL Server.
* **Nền tảng Hosting:** Render.com Cloud Platform.

### 8.4. Tối ưu Hiệu năng & Vận hành Tự động
* **Nén dữ liệu Gzip (HTTP Compression):** Kích hoạt `server.compression.enabled=true`, nén 70% - 85% dung lượng HTML, CSS, JS, JSON trả về cho trình duyệt.
* **Bộ nhớ đệm trình duyệt (Browser Cache Control):** Cấu hình `max-age=7d` lưu đệm ảnh, CSS, Javascript trên máy người dùng trong 7 ngày.
* **Cơ chế Tự động chống ngủ đông (Keep-Alive Self-Ping Service):** Tự động gửi tín hiệu ping định kỳ mỗi 12 phút tới `/api/ping`, ngăn ngừa Render Free Server rơi vào trạng thái ngủ đông (cold sleep).
* **Bộ Kiểm Thử Tự Động (Automated Test Suite):** 84 bài kiểm thử tích hợp (Unit & Integration Tests) bao phủ toàn diện tính năng giỏ hàng, POS, Trade-in, Wishlist, Order Splitting, Audit Trail, và Cascade Deletion User.

# TÀI LIỆU TỔNG HỢP CÁC NHÓM CHỨC NĂNG HỆ THỐNG
## DỰ ÁN: HỆ THỐNG QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ ĐA CHI NHÁNH (BOOK4LIFE)

---

## MỤC LỤC
1. [TỔNG QUAN HỆ THỐNG & PHÂN QUYỀN VAI TRÒ (RBAC)](#1-tổng-quan-hệ-thống--phân-quyền-vai-trò-rbac)
2. [KIẾN TRÚC TỔNG THỂ 3 PHÂN HỆ NGHIỆP VỤ](#2-kiến-trúc-tổng-thể-3-phân-hệ-nghiệp-vụ)
3. [CHI TIẾT PHÂN HỆ 1: CỔNG TRẢI NGHIỆM ĐỘC GIẢ (CUSTOMER PORTAL)](#3-chi-tiết-phân-hệ-1-cổng-trải-nghiệm-độc-giả-customer-portal)
4. [CHI TIẾT PHÂN HỆ 2: VẬN HÀNH & BÁN LẺ CHI NHÁNH (BRANCH OPERATIONS & FULFILLMENT)](#4-chi-tiết-phân-hệ-2-vận-hành--bán-lẻ-chi-nhánh-branch-operations--fulfillment)
5. [CHI TIẾT PHÂN HỆ 3: QUẢN TRỊ CHUỖI & ĐỘNG CƠ NỀN TẢNG (HQ GOVERNANCE & PLATFORM ENGINES)](#5-chi-tiết-phân-hệ-3-quản-trị-chuỗi--động-cơ-nền-tảng-hq-governance--platform-engines)
6. [BẢNG ĐỐI SOÁT MÃ CHỨC NĂNG THEO SRS (SRS REQUIREMENTS MATRIX)](#6-bảng-đối-soát-mã-chức-năng-theo-srs-srs-requirements-matrix)
7. [CÔNG NGHỆ ÁP DỤNG TRONG TOÀN BỘ DỰ ÁN](#7-công-nghệ-áp-dụng-trong-toàn-bộ-dự-án)

---

## 1. TỔNG QUAN HỆ THỐNG & PHÂN QUYỀN VAI TRÒ (RBAC)

**Book4Life** là giải pháp phần mềm kết hợp giữa **Thương mại điện tử B2C**, **Thu mua sách cũ từ độc giả C2B (Trade-in)** và **Hệ thống ERP thu nhỏ** nhằm tối ưu hóa chuỗi cung ứng, kho bãi và dòng tiền cho mô hình chuỗi cửa hàng sách cũ đa chi nhánh.

### Ma trận Phân quyền Vai trò Người dùng (Role-Based Access Control)
| Vai trò (Role) | Ký hiệu hệ thống | Phạm vi & Trách nhiệm chính |
| :--- | :--- | :--- |
| **Quản trị viên chuỗi** | `ROLE_ADMIN` | Quản trị dữ liệu gốc (MDM), cấu hình voucher toàn chuỗi, duyệt tài khoản, phân bổ nhân sự, giám sát báo cáo tài chính tổng hợp. |
| **Quản lý chi nhánh** | `ROLE_BRANCH_MANAGER` | Quản lý vận hành chi nhánh phụ trách, giám sát đơn hàng, tạo và duyệt yêu cầu luân chuyển kho nội bộ, quản lý tài xế và nhân viên. |
| **Chuyên viên thẩm định** | `ROLE_STAFF_APPRAISER` | Tiếp nhận yêu cầu bán sách từ khách, thẩm định độ cũ (Grade), định giá mua, đối soát thực tế tại quầy, duyệt nhập kho và tạo phiếu chi. |
| **Nhân viên bán lẻ & kho** | `ROLE_STAFF_WAREHOUSE` / `STAFF_GENERAL` | Nhặt sách đóng gói theo đơn online, cập nhật vị trí kệ sách, bán hàng tại quầy (POS), xác nhận xuất/nhập phiếu luân chuyển. |
| **Tài xế giao vận** | `ROLE_STAFF_DRIVER` | Tiếp nhận đơn giao từ chi nhánh, cập nhật tiến độ giao vận nội thành, xác nhận giao hàng thành công. |
| **Khách hàng / Độc giả** | `ROLE_CUSTOMER` | Tìm sách theo chi nhánh, mua hàng online, gửi yêu cầu thanh lý sách cũ, săn sách hết hàng, đánh giá chất lượng và chat cùng AI. |

---

## 2. KIẾN TRÚC TỔNG THỂ 3 PHÂN HỆ NGHIỆP VỤ

Toàn bộ hệ thống được phân rã thành **3 phân hệ tương hỗ khép kín**, đảm bảo sự kết nối xuyên suốt giữa khách hàng, nhân viên cửa hàng và ban quản trị trụ sở:

```mermaid
flowchart TD
    subgraph P1 [PHẦN 1: CỔNG TRẢI NGHIỆM ĐỘC GIẢ]
        A1[Tra cứu & Khám phá sách cũ] --> A2[Giỏ hàng & Đặt hàng đa chi nhánh]
        A3[Khảo sát & Bán lại sách cũ Trade-in] --> A4[Săn sách Wishlist & Cảnh báo Restock]
        A5[Đánh giá & Xếp hạng sản phẩm] --> A6[Trợ lý ảo Google Gemini AI]
    end

    subgraph P2 [PHẦN 2: VẬN HÀNH & BÁN LẺ CHI NHÁNH]
        B1[Quản lý Tồn kho SKU & Kệ sách] --> B2[Xử lý Đơn hàng 4 bước Fulfillment]
        B3[Bán hàng tại quầy POS Barcode] --> B4[Thẩm định sách & Nhập kho Phiếu chi]
        B5[Luân chuyển kho liên chi nhánh]
    end

    subgraph P3 [PHẦN 3: QUẢN TRỊ CHUỖI & ĐỘNG CƠ NỀN TẢNG]
        C1[Quản trị Dữ liệu gốc MDM] --> C2[Động cơ Tách đơn tự động Order Splitting]
        C3[Động cơ Khuyến mãi đa điều kiện] --> C4[Dashboard Phân tích & Báo cáo Doanh thu]
        C5[Nhật ký kiểm toán Audit Trail] --> C6[Bảo mật 2 lớp OTP & Thước đo Mật khẩu]
    end

    P1 <-->|Đặt hàng online & Hồ sơ thanh lý| P2
    P2 <-->|Dữ liệu tồn kho & Doanh thu thực tế| P3
    P3 <-->|Dữ liệu chuẩn hóa & Chính sách giá/khuyến mãi| P1
```

---

## 3. CHI TIẾT PHÂN HỆ 1: CỔNG TRẢI NGHIỆM ĐỘC GIẢ (CUSTOMER PORTAL)

> **Mục tiêu:** Mang lại trải nghiệm tìm kiếm minh bạch, quy trình mua sắm thuận tiện và khả năng tương tác 2 chiều (Mua sách B2C & Bán sách C2B).

### 3.1. Tìm kiếm & Khám phá Sách cũ Trực quan
* **Tra cứu theo chi nhánh:** Xem chính xác cuốn sách đang có mặt tại cửa hàng nào trong chuỗi (ví dụ: Chi nhánh Quận 1, Thủ Đức, v.v.).
* **Thang đo độ cũ (Grading Scale):** Minh bạch chất lượng từng bản sao vật lý:
  * `LIKE_NEW` (99% - Như mới, không tỳ vết).
  * `VERY_GOOD` (90% - Rất tốt, giấy đẹp, gáy nguyên vẹn).
  * `GOOD` (80% - Tốt, có dấu thời gian nhẹ, không rách trang).
  * `ACCEPTABLE` (60% - Đọc tốt, có thể ngả vàng hoặc có vết chú thích).
* **Hình ảnh thực tế từ Cloudinary:** Cho phép độc giả soi chi tiết từng vết ố, gáy sách trước khi quyết định mua.

### 3.2. Giỏ hàng Thông minh & Đặt hàng Linh hoạt
* **Giỏ hàng nguồn gốc chi nhánh:** Tự động gom nhóm sách theo cửa hàng sở hữu.
* **Hình thức giao nhận đa dạng:**
  * **Giao tận nơi (COD):** Tự động tính phí vận chuyển theo Tỉnh/Thành phố dựa trên địa chỉ khách chọn.
  * **Giữ sách tại quầy (Click & Collect):** Độc giả đặt trước và ghé chi nhánh gần nhất nhận sách trong 48 giờ hoàn toàn miễn phí ship.
* **Giao diện điền thông tin chuẩn hóa:** Toàn bộ ô nhập liệu sạch sẽ với placeholder mờ hiển thị tên trường (Họ và tên, Số điện thoại, Địa chỉ, Ghi chú), không có dữ liệu mẫu gây nhầm lẫn.

### 3.3. Quy trình Bán lại Sách cũ (C2B Trade-in / Buy-back)
* **Khảo sát trực tuyến:** Khách hàng điền tựa sách, chọn thể loại, năm xuất bản và tải lên 6 ảnh chụp thực tế (bìa trước, bìa sau, gáy sách, mép giấy, trang lót, vết tỳ vết).
* **Định giá sơ bộ tự động (*Auto-Appraisal Algorithm*):** Hệ thống tự động tính toán giá đề xuất ban đầu dựa trên giá bìa gốc, năm xuất bản và tỷ lệ trừ theo mức độ hư hại.
* **Theo dõi tiến trình:** Nhận thông báo kết quả thẩm định chính thức và mã biên lai để mang sách đến quầy nhận tiền mặt.

### 3.4. Tiện ích Giữ chân & Chăm sóc Khách hàng
* **"Săn sách" & Cảnh báo sách về kho (Wishlist & Restock Alert):** Độc giả đăng ký theo dõi các cuốn sách hiếm đã hết hàng. Khi bất kỳ chi nhánh nào nhập kho, hệ thống tự động gửi Email thông báo qua Google SMTP.
* **Đánh giá & Xếp hạng sản phẩm (Reviews & Ratings):** Khách hàng sau khi hoàn thành đơn được quyền gửi đánh giá thực tế, chấm sao (mặc định 5 sao) và nhận xét chất lượng bảo quản sách.
* **Trợ lý ảo thông minh Google Gemini AI:** Chatbot tích hợp trực tiếp trên web, đóng vai trò "thủ thư ảo" hỗ trợ gợi ý đầu sách theo gu đọc, kiểm tra tồn kho và giải đáp chính sách 24/7.

---

## 4. CHI TIẾT PHÂN HỆ 2: VẬN HÀNH & BÁN LẺ CHI NHÁNH (BRANCH OPERATIONS & FULFILLMENT)

> **Mục tiêu:** Chuẩn hóa quy trình vận hành tại từng cửa hàng, đảm bảo tính chính xác về tồn kho vật lý và tốc độ hoàn tất đơn hàng.

### 4.1. Quản lý Tồn kho Vật lý & Định vị Kệ sách (Branch Inventory)
* **Kiến trúc sách 2 tầng (2-Tier Architecture):** Tách biệt giữa *Đầu sách gốc (Book Master)* và các *Mặt hàng thực tế (Book Item / SKU)*.
* **Mã định danh SKU Barcode:** Mỗi cuốn sách khi vào kho được cấp mã vạch riêng gắn liền với độ cũ và giá bán riêng biệt.
* **Quản lý vị trí kệ (`shelfLocation`):** Lưu chính xác vị trí sách trên kệ (ví dụ: *Kệ A2 - Tầng 3 - Dãy Văn Học*), giúp nhân viên lấy sách chỉ trong vài giây.

### 4.2. Xử lý & Hoàn tất Đơn hàng Đa bước (Order Fulfillment Pipeline)
Quy trình khép kín 4 bước rõ ràng:
1. **Tiếp nhận & Phê duyệt:** Nhân viên kiểm tra sách trên kệ và bấm `1. Phê Duyệt & Tiếp Nhận Đơn Hàng` (`PENDING` $\rightarrow$ `CONFIRMED`).
2. **Xuất kho & Đóng gói:** Bấm `2. Bắt Đầu Xuất Kho & Đóng Gói` (`PACKING`).
3. **Phân công vận chuyển:** Chọn tài xế nội bộ của chi nhánh hoặc nhập thông tin đối tác giao vận (GHTK, GHN, AhaMove) kèm mã vận đơn (`SHIPPED`).
4. **Hoàn tất giao hàng:** Xác nhận khách đã nhận sách thành công (`COMPLETED`).
* **Hủy đơn & Hoàn kho tự động:** Trong trường hợp khách hủy đơn hoặc đơn giao thất bại, hệ thống tự động hoàn trả số lượng sách về kho lưu trữ ban đầu bằng Database Transaction an toàn.

### 4.3. Thẩm định & Nhập kho Thu mua Sách cũ
* **Bàn làm việc của Chuyên viên thẩm định (Staff Appraiser):** Soi ảnh chụp phóng to chất lượng cao, đối chiếu bảng tiêu chí hư hại, chốt mức điểm Grade và giá mua chính thức.
* **Đối soát tại quầy:** Khi khách mang sách đến, nhân viên kiểm tra tình trạng thực tế so với hồ sơ online, bấm **Xác nhận nhập kho**, gán vị trí kệ và tự động lập phiếu chi tiền mặt cho khách.

### 4.4. Bán lẻ tại Quầy (Point of Sale - POS)
* Giao diện quẹt Barcode/SKU siêu nhanh phục vụ khách mua trực tiếp tại cửa hàng.
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
* Tự động kiểm tra trùng lặp ISBN khi nhập liệu và công cụ tự sinh mã ISBN-13 chuẩn.

### 5.2. Động cơ Định tuyến & Tách đơn Tự động (Order Routing & Splitting Engine)
* Khi giỏ hàng của khách hàng chứa các cuốn sách thuộc nhiều chi nhánh khác nhau, hệ thống tự động phân tách thành 1 Đơn hàng mẹ (`MasterOrder`) và các Đơn hàng con (`SubOrder`) tương ứng với từng chi nhánh sở hữu.
* Mỗi chi nhánh chỉ nhận và xử lý phần sách thuộc kho của mình, đảm bảo tính độc lập và minh bạch trong việc tính doanh thu.

### 5.3. Động cơ Khuyến mãi Đa điều kiện (Coupons & Discounts Engine)
* Thiết lập đa dạng loại voucher:
  * Giảm theo % giá trị sách (kèm trần giảm tối đa).
  * Giảm số tiền cố định (VNĐ).
  * Freeship 100% hoặc giảm phí vận chuyển cố định.
* Cấu hình điều kiện lọc sâu:
  * Giá trị đơn hàng tối thiểu (`minOrderAmount`).
  * Giới hạn theo **Độ cũ của sách** (ví dụ: chỉ áp dụng cho sách cũ tình trạng `GOOD` hoặc `ACCEPTABLE` để đẩy nhanh hàng tồn).
  * Giới hạn chi nhánh áp dụng hoặc áp dụng toàn chuỗi.

### 5.4. Báo cáo Thống kê & Nhật ký Kiểm toán (Analytics & Audit Trail)
* **Analytics Dashboard:** Trực quan hóa doanh thu theo ngày/tháng, cơ cấu doanh thu theo chi nhánh, tỷ trọng sách bán chạy, tỷ lệ hoàn tất đơn hàng.
* **Audit Trail (Nhật ký kiểm toán):** Ghi nhận chi tiết lịch sử mọi hành vi quan trọng (ai đổi trạng thái đơn, đổi từ trạng thái nào sang trạng thái nào, thời gian thực hiện, ghi chú thao tác) phục vụ việc thanh tra và đối soát nội bộ.
* **Công cụ bảo trì dữ liệu:** Cung cấp tính năng dọn dẹp và reset dữ liệu đơn hàng cũ phục vụ công tác kiểm thử hệ thống.

### 5.5. Hạ tầng Bảo mật & Xác thực 2 Lớp (Security & RBAC)
* **Xác thực 2 lớp qua Email OTP (Google SMTP):** Khách hàng đăng ký tài khoản bắt buộc nhập mã OTP 6 số gửi về hòm thư Gmail, đi kèm giao diện dự phòng hiển thị mã và nút *Tự động điền* tiện lợi.
* **Kiểm tra Email trùng lặp thời gian thực:** Cảnh báo ngay khi người dùng nhập liệu, ngăn ngừa lỗi phát sinh ở tầng cơ sở dữ liệu.
* **Đo lường độ mạnh mật khẩu:** Ràng buộc an toàn bắt buộc mật khẩu phải `> 6 ký tự`, đồng thời chứa đủ chữ cái (a-z, A-Z), chữ số (0-9) và ký tự đặc biệt (!@#$...).
* **Kiến trúc Stateless:** Spring Security kết hợp JSON Web Token (JWT) lưu trữ an toàn trong HttpOnly Cookie, bảo vệ hệ thống trước các cuộc tấn công XSS và CSRF.

---

## 6. BẢNG ĐỐI SOÁT MÃ CHỨC NĂNG THEO SRS (SRS REQUIREMENTS MATRIX)

| Mã Yêu cầu | Tên Chức Năng | Phân hệ | Đối tượng thao tác |
| :--- | :--- | :--- | :--- |
| **FR-MDM-01** | Chuẩn hóa danh mục Đầu sách Master | Phân hệ 3 | Admin |
| **FR-MDM-02** | Quản lý Tác giả & Thể loại chuẩn | Phân hệ 3 | Admin |
| **FR-MDM-03** | Tra cứu & Kiểm tra trùng lặp ISBN | Phân hệ 3 | Admin, Staff |
| **FR-INV-01** | Tạo SKU mặt hàng sách cũ tại chi nhánh | Phân hệ 2 | Staff Warehouse |
| **FR-INV-02** | Chấm điểm tình trạng & Đặt giá bán riêng | Phân hệ 2 | Staff Appraiser |
| **FR-INV-03** | Tải ảnh thực tế đa góc cạnh lên Cloudinary | Phân hệ 2 | Staff Appraiser |
| **FR-INV-04** | Quản lý Vị trí kệ sách (Shelf Location) | Phân hệ 2 | Staff Warehouse |
| **FR-ORD-01** | Giỏ hàng hiển thị nguồn gốc chi nhánh | Phân hệ 1 | Customer |
| **FR-ORD-02** | Thuật toán Tự động tách đơn (Order Splitting) | Phân hệ 3 | Hệ thống |
| **FR-ORD-03** | Trừ kho an toàn bằng Database Transaction | Phân hệ 2 | Hệ thống |
| **FR-ORD-04** | Đặt giữ sách tại quầy (Click & Collect) | Phân hệ 1 | Customer, Staff |
| **FR-FUL-01** | Thông báo đơn hàng mới thời gian thực | Phân hệ 2 | Staff Warehouse |
| **FR-FUL-02** | Xử lý quy trình đóng gói & Giao vận | Phân hệ 2 | Staff, Driver |
| **FR-FUL-03** | Cập nhật tiến độ đơn hàng thời gian thực | Phân hệ 1 | Customer |
| **FR-FUL-04** | Xử lý Hủy đơn & Hoàn kho tự động | Phân hệ 2 | Staff, Hệ thống |
| **FR-TRF-01** | Tạo phiếu đề xuất luân chuyển sách | Phân hệ 2 | Branch Manager |
| **FR-TRF-02** | Kiểm tra ràng buộc luân chuyển tự động | Phân hệ 3 | Hệ thống |
| **FR-TRF-03** | Quy trình Xuất kho - Vận chuyển - Nhập kho | Phân hệ 2 | Staff các chi nhánh |
| **FR-TRF-04** | Theo dõi lịch sử luân chuyển toàn chuỗi | Phân hệ 3 | Admin |
| **FR-WSH-01** | Đăng ký "Săn sách" khi hết hàng | Phân hệ 1 | Customer |
| **FR-WSH-02** | Tự động quét & Bắn Email thông báo có hàng | Phân hệ 1 | Hệ thống (Google SMTP) |
| **FR-WSH-03** | Quản lý danh sách Săn sách cá nhân | Phân hệ 1 | Customer |
| **FR-CPN-01** | Thiết lập Mã giảm giá đa điều kiện | Phân hệ 3 | Admin |
| **FR-CPN-02** | Điều kiện giảm theo Độ cũ của sách (Grade) | Phân hệ 3 | Admin, Hệ thống |
| **FR-CPN-03** | Điều kiện giảm theo Chi nhánh áp dụng | Phân hệ 3 | Admin, Hệ thống |
| **FR-CPN-04** | Kiểm tra và Áp dụng mã trong Giỏ hàng | Phân hệ 1 | Customer, Hệ thống |
| **FR-TRD-01** | Điền thông tin khảo sát tình trạng sách cũ | Phân hệ 1 | Customer |
| **FR-TRD-02** | Tải ảnh chụp thực tế 6 mặt lên Cloudinary | Phân hệ 1 | Customer |
| **FR-TRD-03** | Tự động tính giá định giá sơ bộ ban đầu | Phân hệ 1 | Hệ thống (Auto Algorithm) |
| **FR-TRD-04** | Nhân viên kiểm duyệt thẩm định & Chốt giá | Phân hệ 2 | Staff Appraiser |
| **FR-TRD-05** | Khách hàng phản hồi kết quả định giá | Phân hệ 1 | Customer |
| **FR-TRD-06** | Nhận sách thực tế & Nhập kho - Tạo phiếu chi | Phân hệ 2 | Staff Appraiser |
| **FR-GEM-01** | Giao diện Chatbot thông minh trên Web | Phân hệ 1 | Customer, Guest |
| **FR-GEM-02** | Tư vấn tìm sách theo ngữ cảnh (Gemini AI) | Phân hệ 1 | Hệ thống, Google Gemini |
| **FR-GEM-03** | Giải đáp thắc mắc độ cũ & chính sách thu mua | Phân hệ 1 | Hệ thống, Google Gemini |
| **FR-GEM-04** | Chuyển tiếp sang nhân viên thật (Human Handoff) | Phân hệ 1 | Customer, Staff |

---

## 7. CÔNG NGHỆ ÁP DỤNG TRONG TOÀN BỘ DỰ ÁN

* **Backend:** Java 21, Spring Boot 3.x, Spring Data JPA (Hibernate), Spring Security, Spring Mail (Google SMTP).
* **Bảo mật:** JWT (JSON Web Token) lưu trữ qua HttpOnly Cookie, chuẩn mã hóa BCrypt, cơ chế kiểm soát truy cập RBAC 5 vai trò.
* **Frontend:** Thymeleaf Template Engine, Tailwind CSS, Bootstrap 5, Bootstrap Icons, Font Awesome 6, JavaScript ES6.
* **Tích hợp bên thứ ba:**
  * **Google Gemini AI API:** Xử lý ngôn ngữ tự nhiên cho trợ lý ảo thủ thư.
  * **Cloudinary REST API:** Lưu trữ và tối ưu hóa hình ảnh sách thực tế độ phân giải cao.
  * **Google SMTP (Gmail Service):** Gửi mã OTP xác thực và thông báo Restock sách về kho.
* **Cơ sở dữ liệu:** SQL Server / PostgreSQL / MySQL với kiến trúc Transaction `@Transactional` bảo đảm tính toàn vẹn dữ liệu đơn hàng và kho bãi.


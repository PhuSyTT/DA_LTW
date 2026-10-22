-- ============================================================================
-- HỆ THỐNG QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ (OLD BOOKSTORE CHAIN MANAGEMENT)
-- DATABASE SCRIPT CHO MICROSOFT SQL SERVER / SSMS
-- TÀI KHOẢN SA: sa / 1234@abc
-- ============================================================================

USE master;
GO

-- 1. TẠO CƠ SỞ DỮ LIỆU
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'BookstoreChainDB')
BEGIN
    ALTER DATABASE BookstoreChainDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE BookstoreChainDB;
END
GO

CREATE DATABASE BookstoreChainDB COLLATE Vietnamese_CI_AS;
GO

USE BookstoreChainDB;
GO

-- ============================================================================
-- 2. TẠO CẤU TRÚC BẢNG (TABLE SCHEMAS & CONSTRAINTS)
-- ============================================================================

-- Bảng 1: roles (Vai trò & Phân quyền người dùng)
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL
);
GO

-- Bảng 2: branches (Danh sách chi nhánh chuỗi cửa hàng)
CREATE TABLE branches (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    branch_name NVARCHAR(150) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    city NVARCHAR(100) NOT NULL,
    latitude DECIMAL(10,8) NULL,
    longitude DECIMAL(11,8) NULL,
    is_active BIT NOT NULL DEFAULT 1
);
GO

-- Bảng 3: users (Tài khoản người dùng & Nhân sự)
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(500) NULL,
    role_id BIGINT NOT NULL,
    branch_id BIGINT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_users_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);
GO

-- Bảng 4: categories (Thể loại sách)
CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT NULL,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);
GO

-- Bảng 5: authors (Tác giả chuẩn hóa)
CREATE TABLE authors (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    biography NVARCHAR(MAX) NULL,
    avatar_url VARCHAR(500) NULL
);
GO

-- Bảng 6: books (Danh mục đầu sách gốc - Master Catalog)
CREATE TABLE books (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    publisher NVARCHAR(150) NULL,
    publish_year INT NULL,
    original_price DECIMAL(12,2) NOT NULL,
    cover_image_url VARCHAR(500) NULL,
    description NVARCHAR(MAX) NULL,
    CONSTRAINT fk_books_author FOREIGN KEY (author_id) REFERENCES authors(id),
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id)
);
GO

-- Bảng 7: book_items (Mặt hàng sách cũ thực tế tại từng chi nhánh)
CREATE TABLE book_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    book_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    sku_barcode VARCHAR(50) NOT NULL UNIQUE,
    condition_grade VARCHAR(30) NOT NULL, -- LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE, COLLECTIBLE
    selling_price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 1,
    shelf_location NVARCHAR(100) NULL,
    condition_note NVARCHAR(500) NULL,
    import_date DATETIME NOT NULL DEFAULT GETDATE(),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, RESERVED, SOLD
    CONSTRAINT fk_book_items_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_items_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);
GO

-- Bảng 8: book_item_images (Ảnh chụp thực tế góc cạnh vết ố sách cũ trên Cloudinary)
CREATE TABLE book_item_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    book_item_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    is_primary BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_book_item_images_item FOREIGN KEY (book_item_id) REFERENCES book_items(id)
);
GO

-- Bảng 9: coupons (Mã giảm giá đa điều kiện)
CREATE TABLE coupons (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL, -- PERCENT, FIXED_AMOUNT
    discount_value DECIMAL(12,2) NOT NULL,
    min_order_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(12,2) NULL,
    applicable_condition_grade VARCHAR(30) NULL,
    applicable_branch_id BIGINT NULL,
    usage_limit INT NOT NULL DEFAULT 100,
    used_count INT NOT NULL DEFAULT 0,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    CONSTRAINT fk_coupons_branch FOREIGN KEY (applicable_branch_id) REFERENCES branches(id)
);
GO

-- Bảng 10: master_orders (Đơn hàng mẹ - Quản lý tổng thanh toán)
CREATE TABLE master_orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    master_order_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NULL,
    total_items_amount DECIMAL(12,2) NOT NULL,
    total_shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'COD', -- COD, VNPAY, MOMO
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, REFUNDED
    receiver_name NVARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_master_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_master_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);
GO

-- Bảng 11: coupon_usages (Lịch sử áp dụng coupon)
CREATE TABLE coupon_usages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    master_order_id BIGINT NOT NULL,
    used_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_coupon_usages_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    CONSTRAINT fk_coupon_usages_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_coupon_usages_order FOREIGN KEY (master_order_id) REFERENCES master_orders(id)
);
GO

-- Bảng 12: sub_orders (Đơn hàng con tách theo chi nhánh chịu trách nhiệm đóng gói)
CREATE TABLE sub_orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    master_order_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    sub_order_code VARCHAR(50) NOT NULL UNIQUE,
    branch_subtotal DECIMAL(12,2) NOT NULL,
    branch_shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    tracking_number VARCHAR(100) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, PACKING, SHIPPED, COMPLETED, CANCELLED
    cancel_reason NVARCHAR(500) NULL,
    confirmed_at DATETIME NULL,
    shipped_at DATETIME NULL,
    completed_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT fk_sub_orders_master FOREIGN KEY (master_order_id) REFERENCES master_orders(id),
    CONSTRAINT fk_sub_orders_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);
GO

-- Bảng 13: sub_order_items (Chi tiết mặt hàng trong đơn hàng con)
CREATE TABLE sub_order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sub_order_id BIGINT NOT NULL,
    book_item_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_sub_order_items_sub FOREIGN KEY (sub_order_id) REFERENCES sub_orders(id),
    CONSTRAINT fk_sub_order_items_item FOREIGN KEY (book_item_id) REFERENCES book_items(id)
);
GO

-- Bảng 13b: order_status_history (Nhật ký theo dõi tiến độ & điều phối đơn hàng)
CREATE TABLE order_status_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sub_order_id BIGINT NOT NULL,
    master_order_id BIGINT NOT NULL,
    from_status VARCHAR(50) NULL,
    to_status VARCHAR(50) NOT NULL,
    note NVARCHAR(500) NULL,
    updated_by_user_id BIGINT NULL,
    from_branch_id BIGINT NULL,
    to_branch_id BIGINT NULL,
    action_type VARCHAR(50) NOT NULL, -- STATUS_CHANGE, BRANCH_REASSIGN, ORDER_CANCELLED
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_osh_sub_order FOREIGN KEY (sub_order_id) REFERENCES sub_orders(id),
    CONSTRAINT fk_osh_master_order FOREIGN KEY (master_order_id) REFERENCES master_orders(id),
    CONSTRAINT fk_osh_user FOREIGN KEY (updated_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_osh_from_branch FOREIGN KEY (from_branch_id) REFERENCES branches(id),
    CONSTRAINT fk_osh_to_branch FOREIGN KEY (to_branch_id) REFERENCES branches(id)
);
GO

-- Bảng 14: trade_in_requests (Phiếu thu mua sách cũ từ người dùng)
CREATE TABLE trade_in_requests (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    request_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    target_branch_id BIGINT NOT NULL,
    user_condition_grade VARCHAR(30) NOT NULL,
    survey_answers_json NVARCHAR(MAX) NOT NULL,
    auto_estimated_price DECIMAL(12,2) NOT NULL,
    staff_reviewed_price DECIMAL(12,2) NULL,
    staff_condition_grade VARCHAR(30) NULL,
    reviewed_by_staff_id BIGINT NULL,
    status VARCHAR(35) NOT NULL DEFAULT 'PENDING_REVIEW', -- PENDING_REVIEW, OFFERED, ACCEPTED_AWAITING_DELIVERY, RECEIVED_STOCKED, REJECTED, CANCELLED
    delivery_method VARCHAR(30) NOT NULL DEFAULT 'BRING_TO_STORE', -- BRING_TO_STORE, POSTAL_SHIPPING
    staff_notes NVARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_trade_in_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_trade_in_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_trade_in_branch FOREIGN KEY (target_branch_id) REFERENCES branches(id),
    CONSTRAINT fk_trade_in_staff FOREIGN KEY (reviewed_by_staff_id) REFERENCES users(id)
);
GO

-- Bảng 15: trade_in_images (Ảnh chụp thực tế do khách gửi thẩm định)
CREATE TABLE trade_in_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    trade_in_request_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    image_type VARCHAR(30) NOT NULL, -- COVER, SPINE, DEFECT_PAGE
    CONSTRAINT fk_trade_in_images_req FOREIGN KEY (trade_in_request_id) REFERENCES trade_in_requests(id)
);
GO

-- Bảng 16: stock_transfers (Phiếu luân chuyển kho nội bộ)
CREATE TABLE stock_transfers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transfer_code VARCHAR(50) NOT NULL UNIQUE,
    from_branch_id BIGINT NOT NULL,
    to_branch_id BIGINT NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL', -- PENDING_APPROVAL, IN_TRANSIT, COMPLETED, REJECTED
    reason NVARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    completed_at DATETIME NULL,
    CONSTRAINT fk_transfers_from FOREIGN KEY (from_branch_id) REFERENCES branches(id),
    CONSTRAINT fk_transfers_to FOREIGN KEY (to_branch_id) REFERENCES branches(id),
    CONSTRAINT fk_transfers_user FOREIGN KEY (requested_by_user_id) REFERENCES users(id)
);
GO

-- Bảng 17: stock_transfer_items (Chi tiết mặt hàng luân chuyển)
CREATE TABLE stock_transfer_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transfer_id BIGINT NOT NULL,
    book_item_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_transfer_items_trf FOREIGN KEY (transfer_id) REFERENCES stock_transfers(id),
    CONSTRAINT fk_transfer_items_item FOREIGN KEY (book_item_id) REFERENCES book_items(id)
);
GO

-- Bảng 18: wishlists (Danh sách Săn sách khi có hàng)
CREATE TABLE wishlists (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    preferred_branch_id BIGINT NULL,
    min_condition_grade VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    is_notified BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    notified_at DATETIME NULL,
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_wishlists_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_wishlists_branch FOREIGN KEY (preferred_branch_id) REFERENCES branches(id)
);
GO

-- Bảng 19: reviews (Đánh giá chất lượng sách từ khách hàng)
CREATE TABLE reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_item_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_item FOREIGN KEY (book_item_id) REFERENCES book_items(id)
);
GO

-- Bảng 20: chat_messages (Tin nhắn hỗ trợ trực tiếp qua WebSocket)
CREATE TABLE chat_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NULL,
    branch_id BIGINT NULL,
    content NVARCHAR(MAX) NOT NULL,
    attachment_url VARCHAR(500) NULL,
    is_read BIT NOT NULL DEFAULT 0,
    sent_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_chat_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    CONSTRAINT fk_chat_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);
GO

-- Bảng 20b: ai_chat_messages (Lịch sử hội thoại Trợ lý ảo CSKH AI)
CREATE TABLE ai_chat_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NULL,
    session_id VARCHAR(100) NOT NULL,
    sender_type VARCHAR(20) NOT NULL, -- 'USER', 'ASSISTANT'
    content NVARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_ai_chat_user FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- ============================================================================
-- 3. INSERT DỮ LIỆU KHỞI TẠO (SEED DATA)
-- ============================================================================

-- Chèn roles
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (id, role_name, description) VALUES
(1, 'ROLE_ADMIN', N'Quản trị viên toàn quyền hệ thống chuỗi'),
(2, 'ROLE_BRANCH_MANAGER', N'Quản lý chi nhánh, duyệt luân chuyển kho & xem báo cáo'),
(3, 'ROLE_STAFF_APPRAISER', N'Nhân viên kiểm duyệt, chuyên trách thẩm định & nhập kho sách cũ'),
(4, 'ROLE_STAFF_GENERAL', N'Nhân viên bán hàng & kho, xử lý đơn online & quầy POS'),
(5, 'ROLE_CUSTOMER', N'Khách hàng mua sách & gửi yêu cầu bán lại sách cũ');
SET IDENTITY_INSERT roles OFF;
GO

-- Chèn branches
SET IDENTITY_INSERT branches ON;
INSERT INTO branches (id, branch_name, address, phone, city, is_active) VALUES
(1, N'Chi nhánh Quận 1 (Trụ Sở)', N'120 Nguyễn Huệ, Phường Bến Nghé, Quận 1', '028.3822.1101', N'Hồ Chí Minh', 1),
(2, N'Chi nhánh Thủ Đức', N'45 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức', '028.3896.2202', N'Hồ Chí Minh', 1),
(3, N'Chi nhánh Bình Thạnh', N'235 Bạch Đằng, Phường 15, Quận Bình Thạnh', '028.3511.4404', N'Hồ Chí Minh', 1);
SET IDENTITY_INSERT branches OFF;
GO

-- Chèn users (Mật khẩu băm BCrypt chuẩn cho '123456' là: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, full_name, phone, role_id, branch_id, is_active, created_at) VALUES
(1, 'admin@bookstore.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Văn Admin', '0901.111.222', 1, NULL, 1, GETDATE()),
(2, 'manager.q1@bookstore.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Trần Thị Lan (Quản lý Q1)', '0902.333.444', 2, 1, 1, GETDATE()),
(3, 'appraiser.q1@bookstore.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Lê Văn Thẩm Định (Kiểm duyệt Q1)', '0903.555.666', 3, 1, 1, GETDATE()),
(4, 'staff.q1@bookstore.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Phạm Hoàng Kho (Staff Thường Q1)', '0904.777.888', 4, 1, 1, GETDATE()),
(5, 'appraiser.td@bookstore.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Đỗ Minh Kiểm Duyệt (Kiểm duyệt TĐ)', '0905.999.000', 3, 2, 1, GETDATE()),
(6, 'staff.td@bookstore.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Vũ Ngọc Bán Hàng (Staff Thường TĐ)', '0906.123.456', 4, 2, 1, GETDATE()),
(7, 'customer.dung@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Hoàng Tiến Dũng (Độc giả)', '0912.888.999', 5, NULL, 1, GETDATE()),
(8, 'customer.mai@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Tuyết Mai (Độc giả)', '0918.777.666', 5, NULL, 1, GETDATE());
SET IDENTITY_INSERT users OFF;
GO

-- Chèn categories
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (id, name, parent_id) VALUES
(1, N'Văn Học - Tiểu Thuyết', NULL),
(2, N'Khảo Luận - Kỹ Năng Sống', NULL),
(3, N'Lịch Sử - Văn Hóa Việt Nam', NULL),
(4, N'Tư Tưởng - Triết Học - Xã Hội', NULL);
SET IDENTITY_INSERT categories OFF;
GO

-- Chèn 35 tác giả Việt Nam kinh điển
SET IDENTITY_INSERT authors ON;
INSERT INTO authors (id, name, biography) VALUES
(1, N'Nguyễn Du', N'Đại thi hào dân tộc, danh nhân văn hóa thế giới, tác giả kiệt tác Truyện Kiều (Đoạn Trường Tân Thanh), Văn Chiêu Hồn, Bắc Hành Tạp Lục'),
(2, N'Nguyễn Nhật Ánh', N'Nhà văn nổi tiếng của Việt Nam với nhiều tác phẩm gắn liền tuổi thơ như Tôi Thấy Hoa Vàng Trên Cỏ Xanh, Mắt Biếc, Cho Tôi Xin Một Vé Đi Tuổi Thơ'),
(3, N'Nguyễn Tuân', N'Bậc thầy tùy bút và ngôn ngữ nghệ thuật Việt Nam, tác giả Vang Bóng Một Thời, Tùy Bút Sông Đà, Chiếc Lư Đồng Mắt Cua'),
(4, N'Vũ Trọng Phụng', N'Ông vua phóng sự đất Bắc, cây bút trào phúng hiện thực kiệt xuất với Số Đỏ, Giông Tố, Kỹ Nghệ Lấy Tây, Cơm Thầy Cơm Cô'),
(5, N'Nam Cao', N'Đại văn hào của trào lưu hiện thực phê phán Việt Nam thế kỷ 20, tác giả Chí Phèo, Lão Hạc, Sống Mòn, Đôi Mắt, Trăng Sáng'),
(6, N'Thạch Lam', N'Cây bút trữ tình tinh tế và lắng đọng của Tự Lực Văn Đoàn, tác giả Hà Nội Băm Sáu Phố Phường, Gió Đầu Mùa, Nắng Trong Vườn'),
(7, N'Tô Hoài', N'Cây đại thụ văn học thiếu nhi và hiện thực đời sống Việt Nam, tác giả Dế Mèn Phiêu Lưu Ký, Vợ Chồng A Phủ, Cát Bụi Chân Ai'),
(8, N'Nguyễn Hiến Lê', N'Đại học giả, nhà văn, dịch giả uyên bác hàng đầu miền Nam với hơn 100 công trình khảo luận văn hóa, triết học và phương pháp tự học'),
(9, N'Sơn Nam', N'Nhà văn, nhà nghiên cứu Nam Bộ mộc mạc, "ông già đi bộ đất Nam Bộ", tác giả Hương Rừng Cà Mau, Lịch Sử Khẩn Hoang Miền Nam, Văn Minh Miệt Vườn'),
(10, N'Bình Nguyên Lộc', N'Nhà văn, nhà văn hóa miền Nam với vốn sống và ngôn từ dồi dào, tác giả Đò Dọc, Hương Quê, Rừng Mắm'),
(11, N'Vương Hồng Sển', N'Học giả, nhà văn hóa và nhà sưu tầm cổ ngoạn trứ danh đất Sài Gòn xưa, tác giả Sài Gòn Năm Xưa, Thú Chơi Cổ Ngoạn, Hơn Nửa Vòng Đời'),
(12, N'Toan Ánh', N'Học giả khảo cứu phong tục tập quán truyền thống Việt Nam, tác giả bộ sách kinh điển Nếp Cũ: Con Người Việt Nam, Làng Xóm Việt Nam, Tín Ngưỡng Việt Nam'),
(13, N'Đoàn Giỏi', N'Nhà văn gắn liền với thiên nhiên trù phú miền Nam, tác giả kiệt tác thiếu nhi Đất Rừng Phương Nam, Cá Bống Mú, Tê Giác Trong Ngàn Xanh'),
(14, N'Ngô Tất Tố', N'Nhà văn, nhà báo, học giả uyên bác với ngòi bút hiện thực sắc sảo về nông thôn Việt Nam trước Cách mạng: Tắt Đèn, Lều Chõng, Việc Làng'),
(15, N'Nguyên Hồng', N'Nhà văn của những người cùng khổ dưới đáy xã hội, giọng văn nồng nàn xúc cảm với Bỉ Vỏ, Những Ngày Thơ Ấu, Cửa Biển'),
(16, N'Kim Lân', N'Bậc thầy truyện ngắn về làng quê truyền thống và con người thuần hậu nông thôn Việt Nam: Vợ Nhặt, Làng, Con Chó Xấu Xí'),
(17, N'Nhất Linh', N'Nhà văn, chủ soái Tự Lực Văn Đoàn, ngòi bút giải phóng cá nhân và canh tân phụ nữ: Đoạn Tuyệt, Lạnh Lùng, Bướm Trắng, Gánh Hàng Hoa'),
(18, N'Khái Hưng', N'Cây bút trụ cột của Tự Lực Văn Đoàn, phong cách văn chương tài hoa, lãng mạn: Hồn Bướm Mơ Tiên, Nửa Chừng Xuân, Trống Mái, Tiêu Sơn Tráng Sĩ'),
(19, N'Trần Trọng Kim', N'Học giả, nhà sử học và văn hóa dục lỗi lạc, tác giả bộ sách sử đầu tiên bằng chữ Quốc ngữ Việt Nam Sử Lược, Nho Giáo, Phật Giáo'),
(20, N'Đào Duy Anh', N'Học giả, nhà sử học và từ điển học vĩ đại, người đặt nền móng cho nghiên cứu văn hóa sử học Việt Nam hiện đại với Việt Nam Văn Hóa Sử Cương'),
(21, N'Phan Bội Châu', N'Nhà yêu nước vĩ đại, nhà tư tưởng và nhà thơ cách mạng tiên phong thời cận đại: Ngục Trung Thư, Hải Ngoại Huyết Thư, Việt Nam Vong Quốc Sử'),
(22, N'Nguyễn Huy Thiệp', N'Hiện tượng đặc sắc nhất của văn xuôi đổi mới Việt Nam cuối thế kỷ 20 với Tướng Về Hưu, Kiếm Sắc, Vàng Lửa, Thương Nhớ Đồng Quê'),
(23, N'Bảo Ninh', N'Nhà văn thời hậu chiến với kiệt tác dịch ra hơn 20 ngôn ngữ quốc tế Nỗi Buồn Chiến Tranh (Thân Phận Của Tình Yêu), Bội Phản, Gió Dại'),
(24, N'Nguyễn Ngọc Tư', N'Nữ văn sĩ tài hoa của miền đất mũi Cà Mau đương đại, tác giả Cánh Đồng Bất Tận, Khói Trời Lộng Lẫy, Gió Lẻ, Không Ai Qua Sông'),
(25, N'Lưu Quang Vũ', N'Nhà viết kịch kiệt xuất, nhà thơ tài hoa với những vở kịch làm rung chuyển sân khấu Việt Nam: Hồn Trương Ba Da Hàng Thịt, Tôi Và Chúng Ta'),
(26, N'Xuân Diệu', N'Nhà thơ mới nhất trong các nhà thơ mới, ông hoàng thơ tình Việt Nam: Thơ Thơ, Gửi Hương Cho Gió, Phấn Thông Vàng'),
(27, N'Hàn Mặc Tử', N'Thi sĩ tài hoa đoản mệnh, hiện tượng thơ kỳ bí, mãnh liệt và trong trẻo của phong trào Thơ Mới: Gái Quê, Đau Thương (Thơ Điên), Xuân Như Ý'),
(28, N'Huy Cận', N'Nhà thơ lớn của phong trào Thơ Mới và thi ca cách mạng Việt Nam: Lửa Thiêng, Vũ Trụ Ca, Trời Mỗi Ngày Lại Sáng'),
(29, N'Quang Dũng', N'Nhà thơ xứ Đoài mây trắng, chiến binh Tây Tiến hào hoa lãng tử: Mây Đầu Ô, Tây Tiến, Đôi Mắt Người Sơn Tây'),
(30, N'Phùng Quán', N'Nhà văn kiên định, giàu khí phách với trường thiên tiểu thuyết thiếu nhi kinh điển Tuổi Thơ Dữ Dội, Lời Mẹ Dặn, Trăng Hoàng Cung'),
(31, N'Chu Lai', N'Nhà văn quân đội tài hoa về người lính và thời kỳ hậu chiến: Ăn Mày Dĩ Vãng, Mưa Đỏ, Phố Nhà Binh, Khúc Bi Tráng Cuối Cùng'),
(32, N'Lê Lựu', N'Cây bút tiên phong của văn học Đổi mới viết về số phận con người và nông thôn: Thời Xa Vắng, Sóng Ở Đáy Sông, Mở Rừng'),
(33, N'Ma Văn Kháng', N'Cây đại thụ văn xuôi Việt Nam về đồng bào miền núi và biến động thị thành: Mùa Lá Rụng Trong Vườn, Đồng Bạc Trắng Hoa Xòe'),
(34, N'Nguyễn Khải', N'Bậc thầy văn xuôi tư tưởng và phân tích tâm lý con người qua thời cuộc: Mùa Lạc, Gặp Gỡ Cuối Năm, Một Người Hà Nội'),
(35, N'Hoàng Phủ Ngọc Tường', N'Bậc thầy tùy bút văn hóa, lịch sử và tâm hồn xứ Huế: Ai Đã Đặt Tên Cho Dòng Sông, Rất Nhiều Ánh Lửa, Ngọn Cỏ Bồng');
SET IDENTITY_INSERT authors OFF;
GO

UPDATE authors SET avatar_url = '/images/authors/author_' + CAST(id AS VARCHAR(10)) + '.jpg';
GO

-- Chèn 110 đầu sách Việt Nam kinh điển chuẩn hóa
SET IDENTITY_INSERT books ON;
INSERT INTO books (id, title, isbn, author_id, category_id, publisher, publish_year, original_price) VALUES
(1, N'Truyện Kiều (Đoạn Trường Tân Thanh)', '978-604-58-1234-1', 1, 1, N'NXB Văn Học', 2018, 95000),
(2, N'Văn Tế Thập Loại Chúng Sinh (Văn Chiêu Hồn)', '978-604-58-1235-8', 1, 1, N'NXB Văn Học', 2019, 65000),
(3, N'Bắc Hành Tạp Lục & Thơ Chữ Hán Nguyễn Du', '978-604-58-1236-5', 1, 1, N'NXB Hội Nhà Văn', 2020, 110000),
(4, N'Thúy Kiều Đoạn Trường Toàn Tập Khảo Luận', '978-604-58-1237-2', 1, 1, N'NXB Trẻ', 2021, 120000),
(5, N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', '978-604-2-09876-5', 2, 1, N'NXB Trẻ', 2015, 125000),
(6, N'Mắt Biếc', '978-604-1-14567-3', 2, 1, N'NXB Trẻ', 2019, 110000),
(7, N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', '978-604-1-08912-4', 2, 1, N'NXB Trẻ', 2018, 90000),
(8, N'Cô Gái Đến Từ Hôm Qua', '978-604-1-07654-1', 2, 1, N'NXB Trẻ', 2017, 85000),
(9, N'Có Hai Con Mèo Ngồi Bên Cửa Sổ', '978-604-1-06543-2', 2, 1, N'NXB Trẻ', 2016, 78000),
(10, N'Chúc Một Ngày Tốt Lành', '978-604-1-05432-1', 2, 1, N'NXB Trẻ', 2014, 95000),
(11, N'Ngồi Khóc Trên Cây', '978-604-1-04321-9', 2, 1, N'NXB Trẻ', 2013, 105000),
(12, N'Đi Qua Hoa Cúc', '978-604-1-03210-8', 2, 1, N'NXB Trẻ', 2012, 70000),
(13, N'Vang Bóng Một Thời (Tập Tùy Bút Cổ Điển)', '978-013-235088-4', 3, 1, N'NXB Văn Học', 2016, 85000),
(14, N'Tùy Bút Sông Đà & Người Lái Đò Sông Đà', '978-013-449416-6', 3, 1, N'NXB Văn Học', 2018, 90000),
(15, N'Chiếc Lư Đồng Mắt Cua', '978-013-708107-3', 3, 1, N'NXB Hội Nhà Văn', 2017, 82000),
(16, N'Hà Nội Ta Đánh Mỹ Giỏi', '978-013-691571-3', 3, 1, N'NXB Tác Phẩm Mới', 2015, 75000),
(17, N'Tóc Chị Hoài & Cỏ Độc Lập', '978-013-597444-5', 3, 1, N'NXB Văn Học', 2019, 80000),
(18, N'Số Đỏ (Kiệt Tác Tiểu Thuyết Trào Phúng)', '978-604-56-7890-2', 4, 1, N'NXB Văn Học', 2017, 95000),
(19, N'Giông Tố', '978-604-56-7891-9', 4, 1, N'NXB Văn Học', 2018, 98000),
(20, N'Kỹ Nghệ Lấy Tây & Cạm Bẫy Người', '978-604-56-7892-6', 4, 1, N'NXB Lao Động', 2019, 88000),
(21, N'Vỡ Đê', '978-604-56-7893-3', 4, 1, N'NXB Văn Học', 2016, 85000),
(22, N'Chí Phèo (Tuyển Tập Truyện Ngắn Hiện Thực)', '978-604-55-1101-1', 5, 1, N'NXB Văn Học', 2019, 85000),
(23, N'Sống Mòn (Tiểu Thuyết Tấm Lòng Trí Thức)', '978-604-55-1102-8', 5, 1, N'NXB Văn Học', 2018, 92000),
(24, N'Lão Hạc & Đôi Mắt', '978-604-55-1103-5', 5, 1, N'NXB Kim Đồng', 2020, 75000),
(25, N'Trăng Sáng & Dì Hảo', '978-604-55-2201-2', 5, 1, N'NXB Văn Học', 2017, 68000),
(26, N'Hà Nội Băm Sáu Phố Phường', '978-604-55-2202-9', 6, 1, N'NXB Hội Nhà Văn', 2018, 70000),
(27, N'Gió Đầu Mùa (Tập Truyện Ngắn Tuyển Chọn)', '978-604-55-2203-6', 6, 1, N'NXB Văn Học', 2017, 65000),
(28, N'Nắng Trong Vườn & Sợi Tóc', '978-604-2-15678-0', 6, 1, N'NXB Trẻ', 2019, 68000),
(29, N'Dế Mèn Phiêu Lưu Ký (Ấn Bản Cổ Điển)', '978-604-2-15679-7', 7, 1, N'NXB Kim Đồng', 2018, 75000),
(30, N'Truyện Tây Bắc (Vợ Chồng A Phủ)', '978-604-56-3301-4', 7, 1, N'NXB Văn Học', 2017, 80000),
(31, N'Cát Bụi Chân Ai (Hồi Ký Văn Nhân Đất Bắc)', '978-604-55-4401-8', 7, 1, N'NXB Hội Nhà Văn', 2016, 95000),
(32, N'Ba Người Khác (Tiểu Thuyết Đổi Mới)', '978-604-55-4402-5', 7, 1, N'NXB Đà Nẵng', 2015, 88000),
(33, N'Chuyện Cũ Hà Nội (2 Tập Hoàn Chỉnh)', '978-604-55-4403-2', 7, 1, N'NXB Hà Nội', 2019, 135000),
(34, N'Hương Sắc Trong Vườn Văn (2 Tập)', '978-604-68-5501-5', 8, 2, N'NXB Văn Hóa Thông Tin', 2015, 145000),
(35, N'Bảy Ngày Trong Đồng Tháp Mười', '978-604-68-5502-2', 8, 3, N'NXB Trẻ', 2017, 78000),
(36, N'Kim Chỉ Nam Của Học Sinh & Bí Quyết Tự Học', '978-604-68-5503-9', 8, 2, N'NXB Tổng Hợp TP.HCM', 2018, 85000),
(37, N'Đời Viết Văn Của Tôi (Tự Truyện Văn Học)', '978-604-68-5504-6', 8, 2, N'NXB Văn Hóa', 2019, 110000),
(38, N'Rèn Nghị Lực Để Lập Thân', '978-604-68-5505-3', 8, 2, N'NXB Thanh Niên', 2020, 72000),
(39, N'Hương Rừng Cà Mau (Tuyển Tập Truyện Ngắn)', '978-604-56-6601-1', 9, 1, N'NXB Trẻ', 2016, 110000),
(40, N'Lịch Sử Khẩn Hoang Miền Nam', '978-604-56-6602-8', 9, 3, N'NXB Trẻ', 2018, 125000),
(41, N'Văn Minh Miệt Vườn', '978-604-56-6603-5', 9, 3, N'NXB Trẻ', 2017, 85000),
(42, N'Bến Nghé Xưa & Đất Gia Định Xưa', '978-604-56-6604-2', 9, 3, N'NXB Trẻ', 2019, 95000),
(43, N'Người Sài Gòn & Phong Tục Đất Phương Nam', '978-604-56-6605-9', 9, 3, N'NXB Trẻ', 2020, 90000),
(44, N'Đò Dọc (Tiểu Thuyết Đoạt Giải Văn Chương Toàn Quốc)', '978-604-56-7701-3', 10, 1, N'NXB Văn Nghệ', 2015, 98000),
(45, N'Hương Quê (Tập Truyện Ngắn Miền Nam)', '978-604-56-7702-0', 10, 1, N'NXB Hội Nhà Văn', 2017, 88000),
(46, N'Rừng Mắm & Những Câu Chuyện Phù Sa', '978-604-56-7703-7', 10, 1, N'NXB Văn Nghệ', 2018, 82000),
(47, N'Sài Gòn Năm Xưa', '978-604-56-7704-4', 11, 3, N'NXB Tổng Hợp TP.HCM', 2016, 115000),
(48, N'Thú Chơi Cổ Ngoạn', '978-604-56-7705-1', 11, 3, N'NXB Văn Hóa', 2017, 130000),
(49, N'Hơn Nửa Vòng Đời (Hồi Ký Cụ Vương)', '978-604-55-8801-7', 11, 3, N'NXB Trẻ', 2018, 120000),
(50, N'Nếp Cũ: Con Người Việt Nam', '978-604-55-8802-4', 12, 3, N'NXB Trẻ', 2017, 105000),
(51, N'Nếp Cũ: Làng Xóm & Tín Ngưỡng Việt Nam', '978-604-55-8803-1', 12, 3, N'NXB Trẻ', 2018, 115000),
(52, N'Phong Tục Miền Bắc & Hội Hè Đình Đám', '978-604-55-8804-8', 12, 3, N'NXB Văn Hóa Dân Tộc', 2019, 98000),
(53, N'Đất Rừng Phương Nam (Ấn Bản Kinh Điển)', '978-604-1-19901-0', 13, 1, N'NXB Kim Đồng', 2018, 95000),
(54, N'Cá Bống Mú & Ngọn Lửa Khăn Quàng Đỏ', '978-604-1-19902-7', 13, 1, N'NXB Kim Đồng', 2017, 70000),
(55, N'Tê Giác Trong Ngàn Xanh', '978-604-1-19903-4', 13, 1, N'NXB Văn Nghệ', 2016, 78000),
(56, N'Tắt Đèn (Bức Tranh Nông Thôn Thu Cưu Khốc Liệt)', '978-604-1-19904-1', 14, 1, N'NXB Văn Học', 2017, 75000),
(57, N'Lều Chõng (Khoa Cử Nho Học Cuối Mùa)', '978-604-56-1401-2', 14, 1, N'NXB Văn Học', 2018, 85000),
(58, N'Việc Làng (Phóng Sự Hủ Tục Nông Thôn)', '978-604-56-1402-9', 14, 1, N'NXB Hội Nhà Văn', 2019, 72000),
(59, N'Bỉ Vỏ (Tiểu Thuyết Tầng Lớp Dưới Đáy Xã Hội)', '978-604-56-1403-6', 15, 1, N'NXB Văn Học', 2017, 82000),
(60, N'Những Ngày Thơ Ấu (Hồi Ký Cay Đắng Tuổi Thơ)', '978-604-55-1501-6', 15, 1, N'NXB Kim Đồng', 2018, 65000),
(61, N'Cửa Biển (Bộ Tiểu Thuyết Sử Thi Hải Phòng)', '978-604-55-1502-3', 15, 1, N'NXB Văn Học', 2016, 185000),
(62, N'Vợ Nhặt & Con Chó Xấu Xí', '978-604-55-1503-0', 16, 1, N'NXB Kim Đồng', 2017, 70000),
(63, N'Làng & Tuyển Tập Truyện Ngắn Kim Lân', '978-604-55-1601-3', 16, 1, N'NXB Văn Học', 2018, 78000),
(64, N'Hiệp Sĩ Gỗ & Những Truyện Đồng Quê', '978-604-55-1602-0', 16, 1, N'NXB Hội Nhà Văn', 2019, 72000),
(65, N'Đoạn Tuyệt (Tiểu Thuyết Đấu Tranh Tự Do Phụ Nữ)', '978-604-55-1603-7', 17, 1, N'NXB Văn Học', 2016, 85000),
(66, N'Lạnh Lùng', '978-604-56-1701-4', 17, 1, N'NXB Phụ Nữ', 2017, 78000),
(67, N'Bướm Trắng', '978-604-56-1702-1', 17, 1, N'NXB Hội Nhà Văn', 2018, 82000),
(68, N'Gánh Hàng Hoa', '978-604-56-1703-8', 17, 1, N'NXB Văn Học', 2019, 80000),
(69, N'Hồn Bướm Mơ Tiên', '978-604-55-1801-0', 18, 1, N'NXB Văn Học', 2016, 68000),
(70, N'Nửa Chừng Xuân', '978-604-55-1802-7', 18, 1, N'NXB Văn Học', 2017, 85000),
(71, N'Tiêu Sơn Tráng Sĩ (Tiểu Thuyết Lịch Sử Võ Hiệp)', '978-604-55-1803-4', 18, 1, N'NXB Hội Nhà Văn', 2018, 110000),
(72, N'Trống Mái', '978-604-1-19011-8', 18, 1, N'NXB Văn Học', 2019, 72000),
(73, N'Việt Nam Sử Lược (Bộ Sử Ký Quốc Ngữ Đầu Tiên)', '978-604-1-19012-5', 19, 3, N'NXB Văn Học', 2018, 145000),
(74, N'Nho Giáo (Bộ Toàn Thư Khảo Cứu Uyên Bác)', '978-604-1-19013-2', 19, 3, N'NXB Văn Học', 2017, 180000),
(75, N'Phật Giáo & Đường Thi Khảo Luận', '978-604-77-2001-9', 19, 3, N'NXB Khoa Học Xã Hội', 2019, 120000),
(76, N'Việt Nam Văn Hóa Sử Cương (Tác Phẩm Nền Tảng)', '978-604-77-2002-6', 20, 3, N'NXB Nhã Nam - Hội Nhà Văn', 2017, 125000),
(77, N'Đất Nước Việt Nam Qua Các Đời (Địa Dư Lịch Sử)', '978-604-77-2101-6', 20, 3, N'NXB Văn Hóa Thông Tin', 2018, 135000),
(78, N'Hán Việt Từ Điển Giản Yếu', '978-604-77-2201-3', 20, 3, N'NXB Khoa Học Xã Hội', 2016, 165000),
(79, N'Ngục Trung Thư & Hải Ngoại Huyết Thư', '978-604-77-2202-0', 21, 4, N'NXB Văn Học', 2015, 88000),
(80, N'Việt Nam Vong Quốc Sử', '978-604-1-23011-2', 21, 4, N'NXB Khoa Học Xã Hội', 2016, 75000),
(81, N'Phan Bội Châu Niên Biểu (Tự Phán Hồi Ký)', '978-604-1-23012-9', 21, 4, N'NXB Văn Hóa Thông Tin', 2017, 95000),
(82, N'Tướng Về Hưu & Những Ngọn Gió Hua Tát', '978-604-1-23013-6', 22, 1, N'NXB Trẻ', 2017, 95000),
(83, N'Vàng Lửa & Kiếm Sắc', '978-604-1-23014-3', 22, 1, N'NXB Hội Nhà Văn', 2018, 88000),
(84, N'Thương Nhớ Đồng Quê & Phẩm Tiết', '978-604-58-2401-4', 22, 1, N'NXB Phụ Nữ', 2019, 92000),
(85, N'Nỗi Buồn Chiến Tranh (Thân Phận Của Tình Yêu)', '978-604-58-2402-1', 23, 1, N'NXB Trẻ', 2018, 110000),
(86, N'Bội Phản & Những Truyện Ngắn Thời Chiến', '978-604-58-2501-1', 23, 1, N'NXB Hội Nhà Văn', 2019, 85000),
(87, N'Hà Nội Lúc Không Giờ', '978-604-58-2502-8', 23, 1, N'NXB Văn Học', 2020, 78000),
(88, N'Cánh Đồng Bất Tận (Tập Truyện Đoạt Giải ASEAN)', '978-604-58-2503-5', 24, 1, N'NXB Trẻ', 2016, 85000),
(89, N'Khói Trời Lộng Lẫy & Gió Lẻ', '978-604-58-2601-8', 24, 1, N'NXB Trẻ', 2018, 78000),
(90, N'Không Ai Qua Sông & Đảo', '978-604-58-2602-5', 24, 1, N'NXB Trẻ', 2019, 82000),
(91, N'Hồn Trương Ba Da Hàng Thịt (Kịch Bản Văn Học)', '978-604-58-2603-2', 25, 1, N'NXB Sân Khấu', 2017, 78000),
(92, N'Tôi Và Chúng Ta & Lời Thề Thứ 9', '978-604-1-27011-7', 25, 1, N'NXB Sân Khấu', 2018, 85000),
(93, N'Thơ Tình Lưu Quang Vũ - Xuân Quỳnh', '978-604-1-27012-4', 25, 1, N'NXB Hội Nhà Văn', 2019, 90000),
(94, N'Thơ Thơ (Tập Thơ Đầu Tay Đỉnh Cao Thơ Mới)', '978-604-1-27013-1', 26, 1, N'NXB Văn Học', 2016, 72000),
(95, N'Gửi Hương Cho Gió', '978-604-1-27014-8', 26, 1, N'NXB Hội Nhà Văn', 2017, 75000),
(96, N'Phấn Thông Vàng (Tập Văn Xuôi Nghệ Thuật)', '978-013-475759-9', 26, 1, N'NXB Văn Học', 2018, 68000),
(97, N'Gái Quê (Tập Thơ Kinh Điển 1936)', '978-032-112742-6', 27, 1, N'NXB Hội Nhà Văn', 2017, 70000),
(98, N'Đau Thương (Thơ Điên) & Xuân Như Ý', '978-013-468599-1', 27, 1, N'NXB Văn Học', 2018, 75000),
(99, N'Chơi Giữa Mùa Trăng (Văn Xuôi & Thơ Kỳ Ảo)', '978-032-133678-1', 27, 1, N'NXB Trẻ', 2019, 78000),
(100, N'Lửa Thiêng (Tập Thơ Nổi Tiếng Phong Trào Thơ Mới)', '978-020-163361-0', 28, 1, N'NXB Văn Học', 2016, 72000),
(101, N'Vũ Trụ Ca & Kinh Cầu Tự', '978-020-189683-1', 28, 1, N'NXB Hội Nhà Văn', 2018, 76000),
(102, N'Trời Mỗi Ngày Lại Sáng', '978-020-189685-5', 28, 1, N'NXB Văn Học', 2019, 70000),
(103, N'Mây Đầu Ô & Bài Thơ Tây Tiến', '978-013-212695-3', 29, 1, N'NXB Hội Nhà Văn', 2017, 75000),
(104, N'Đôi Mắt Người Sơn Tây & Mùa Hoa Gạo', '978-013-359162-0', 29, 1, N'NXB Văn Học', 2018, 70000),
(105, N'Tuổi Thơ Dữ Dội (Bộ Tiểu Thuyết 2 Tập)', '978-026-204630-5', 30, 1, N'NXB Kim Đồng', 2018, 150000),
(106, N'Lời Mẹ Dặn & Ba Phút Sự Thật', '978-604-1-34011-5', 30, 1, N'NXB Văn Nghệ', 2019, 85000),
(107, N'Ăn Mày Dĩ Vãng (Tiểu Thuyết Đoạt Giải Hội Nhà Văn)', '978-604-1-34012-2', 31, 1, N'NXB Quân Đội Nhân Dân', 2016, 115000),
(108, N'Mưa Đỏ (Khúc Tráng Ca Thành Cổ Quảng Trị)', '978-604-77-3501-3', 31, 1, N'NXB Văn Học', 2018, 125000),
(109, N'Thời Xa Vắng (Tác Phẩm Khởi Đầu Văn Học Đổi Mới)', '978-604-77-3502-0', 32, 1, N'NXB Hội Nhà Văn', 2017, 95000),
(110, N'Ai Đã Đặt Tên Cho Dòng Sông', '978-604-77-3503-7', 35, 1, N'NXB Thuận Hóa', 2018, 85000);
SET IDENTITY_INSERT books OFF;
GO

UPDATE books SET cover_image_url = '/images/books/book_' + CAST(id AS VARCHAR(10)) + '.jpg';
GO

-- Chèn book_items mẫu
SET IDENTITY_INSERT book_items ON;
INSERT INTO book_items (id, book_id, branch_id, sku_barcode, condition_grade, selling_price, stock_quantity, shelf_location, import_date, status) VALUES
(1, 1, 1, 'Q1-DNT-90-001', 'VERY_GOOD', 55000, 2, N'Kệ KNS-01-T2', '2026-01-15 00:00:00', 'AVAILABLE'),
(2, 1, 2, 'TD-DNT-80-002', 'GOOD', 45000, 1, N'Kệ KNS-03-T1', '2026-08-01 00:00:00', 'AVAILABLE'),
(3, 5, 1, 'Q1-HVTCX-99-001', 'LIKE_NEW', 95000, 1, N'Kệ VH-02-T3', '2025-05-10 00:00:00', 'AVAILABLE'),
(4, 5, 3, 'BT-HVTCX-60-001', 'ACCEPTABLE', 40000, 3, N'Kệ VH-05-T1', '2025-11-20 00:00:00', 'AVAILABLE'),
(5, 13, 2, 'TD-CLNC-90-001', 'VERY_GOOD', 290000, 1, N'Kệ IT-01-T2', '2026-07-10 00:00:00', 'AVAILABLE'),
(6, 18, 1, 'Q1-NGK-80-001', 'GOOD', 48000, 1, N'Kệ VH-01-T1', '2026-02-14 00:00:00', 'AVAILABLE'),
(7, 39, 3, 'BT-RNU-90-001', 'VERY_GOOD', 75000, 2, N'Kệ VH-03-T2', '2026-03-01 00:00:00', 'AVAILABLE'),
(8, 72, 1, 'Q1-SAP-99-001', 'LIKE_NEW', 145000, 1, N'Kệ KNS-05-T3', '2026-01-20 00:00:00', 'AVAILABLE'),
(9, 78, 2, 'TD-TLHT-90-001', 'VERY_GOOD', 115000, 2, N'Kệ KT-01-T1', '2026-06-15 00:00:00', 'AVAILABLE'),
(10, 108, 3, 'BT-DPW-99-001', 'LIKE_NEW', 98000, 1, N'Kệ KNS-04-T2', '2026-04-10 00:00:00', 'AVAILABLE'),
(11, 6, 1, 'SKU-Q1-0006-VG', 'VERY_GOOD', 66000, 2, N'Kệ VH-A2-02', '2026-08-10 09:00:52', 'AVAILABLE'),
(12, 6, 2, 'SKU-TD-0006-GD', 'GOOD', 50000, 1, N'Kệ VH-B4-08', '2026-04-23 09:00:52', 'AVAILABLE'),
(13, 6, 3, 'SKU-BT-0006-LN', 'LIKE_NEW', 82000, 1, N'Kệ VH-C2-06', '2026-06-19 09:00:52', 'AVAILABLE'),
(14, 7, 1, 'SKU-Q1-0007-VG', 'VERY_GOOD', 54000, 2, N'Kệ VH-A4-04', '2026-03-09 09:00:52', 'AVAILABLE'),
(15, 7, 2, 'SKU-TD-0007-GD', 'GOOD', 40000, 1, N'Kệ VH-B2-02', '2026-08-07 09:00:52', 'AVAILABLE'),
(16, 8, 1, 'SKU-Q1-0008-VG', 'VERY_GOOD', 51000, 2, N'Kệ VH-A4-02', '2026-08-10 09:00:52', 'AVAILABLE'),
(17, 8, 2, 'SKU-TD-0008-GD', 'GOOD', 38000, 2, N'Kệ VH-B4-04', '2026-07-16 09:00:52', 'AVAILABLE'),
(18, 8, 3, 'SKU-BT-0008-LN', 'LIKE_NEW', 64000, 1, N'Kệ VH-C2-04', '2026-08-12 09:00:52', 'AVAILABLE'),
(19, 9, 1, 'SKU-Q1-0009-VG', 'VERY_GOOD', 47000, 3, N'Kệ VH-A2-09', '2026-01-23 09:00:52', 'AVAILABLE'),
(20, 9, 2, 'SKU-TD-0009-GD', 'GOOD', 35000, 2, N'Kệ VH-B2-03', '2026-06-23 09:00:52', 'AVAILABLE'),
(21, 10, 1, 'SKU-Q1-0010-VG', 'VERY_GOOD', 57000, 3, N'Kệ VH-A1-04', '2026-07-29 09:00:52', 'AVAILABLE'),
(22, 10, 2, 'SKU-TD-0010-GD', 'GOOD', 43000, 1, N'Kệ VH-B2-06', '2026-03-05 09:00:52', 'AVAILABLE'),
(23, 10, 3, 'SKU-BT-0010-LN', 'LIKE_NEW', 71000, 1, N'Kệ VH-C2-07', '2026-08-01 09:00:52', 'AVAILABLE'),
(24, 11, 1, 'SKU-Q1-0011-VG', 'VERY_GOOD', 63000, 2, N'Kệ VH-A1-06', '2026-05-28 09:00:52', 'AVAILABLE'),
(25, 11, 2, 'SKU-TD-0011-GD', 'GOOD', 47000, 1, N'Kệ VH-B2-06', '2026-01-17 09:00:52', 'AVAILABLE'),
(26, 12, 1, 'SKU-Q1-0012-VG', 'VERY_GOOD', 42000, 3, N'Kệ VH-A2-07', '2026-05-11 09:00:52', 'AVAILABLE'),
(27, 12, 2, 'SKU-TD-0012-GD', 'GOOD', 32000, 2, N'Kệ VH-B3-06', '2026-07-19 09:00:52', 'AVAILABLE'),
(28, 12, 3, 'SKU-BT-0012-LN', 'LIKE_NEW', 52000, 1, N'Kệ VH-C3-08', '2026-04-19 09:00:52', 'AVAILABLE'),
(29, 13, 1, 'SKU-Q1-0013-VG', 'VERY_GOOD', 270000, 3, N'Kệ KN-A1-05', '2026-03-08 09:00:52', 'AVAILABLE'),
(30, 13, 2, 'SKU-TD-0013-GD', 'GOOD', 202000, 2, N'Kệ KN-B3-01', '2026-02-05 09:00:52', 'AVAILABLE'),
(31, 14, 1, 'SKU-Q1-0014-VG', 'VERY_GOOD', 288000, 3, N'Kệ KN-A4-07', '2026-05-27 09:00:52', 'AVAILABLE'),
(32, 14, 2, 'SKU-TD-0014-GD', 'GOOD', 216000, 1, N'Kệ KN-B3-04', '2026-06-23 09:00:52', 'AVAILABLE'),
(33, 14, 3, 'SKU-BT-0014-LN', 'LIKE_NEW', 360000, 1, N'Kệ KN-C3-03', '2026-07-20 09:00:52', 'AVAILABLE'),
(34, 15, 1, 'SKU-Q1-0015-VG', 'VERY_GOOD', 252000, 3, N'Kệ KN-A1-04', '2026-05-15 09:00:52', 'AVAILABLE'),
(35, 15, 2, 'SKU-TD-0015-GD', 'GOOD', 189000, 2, N'Kệ KN-B1-01', '2026-07-02 09:00:52', 'AVAILABLE'),
(36, 16, 1, 'SKU-Q1-0016-VG', 'VERY_GOOD', 312000, 3, N'Kệ KN-A4-05', '2026-05-16 09:00:52', 'AVAILABLE'),
(37, 16, 2, 'SKU-TD-0016-GD', 'GOOD', 234000, 2, N'Kệ KN-B3-09', '2026-03-06 09:00:52', 'AVAILABLE'),
(38, 16, 3, 'SKU-BT-0016-LN', 'LIKE_NEW', 390000, 1, N'Kệ KN-C3-01', '2026-08-04 09:00:52', 'AVAILABLE'),
(39, 17, 1, 'SKU-Q1-0017-VG', 'VERY_GOOD', 330000, 3, N'Kệ KN-A2-08', '2026-02-02 09:00:52', 'AVAILABLE'),
(40, 17, 2, 'SKU-TD-0017-GD', 'GOOD', 248000, 2, N'Kệ KN-B4-02', '2026-03-22 09:00:52', 'AVAILABLE'),
(41, 18, 1, 'SKU-Q1-0018-VG', 'VERY_GOOD', 47000, 2, N'Kệ VH-A3-04', '2026-04-14 09:00:52', 'AVAILABLE'),
(42, 18, 2, 'SKU-TD-0018-GD', 'GOOD', 36000, 2, N'Kệ VH-B3-02', '2026-08-17 09:00:52', 'AVAILABLE'),
(43, 18, 3, 'SKU-BT-0018-LN', 'LIKE_NEW', 59000, 1, N'Kệ VH-C3-06', '2026-07-14 09:00:52', 'AVAILABLE'),
(44, 19, 1, 'SKU-Q1-0019-VG', 'VERY_GOOD', 55000, 2, N'Kệ VH-A1-08', '2026-02-07 09:00:52', 'AVAILABLE'),
(45, 19, 2, 'SKU-TD-0019-GD', 'GOOD', 41000, 1, N'Kệ VH-B2-04', '2026-03-16 09:00:52', 'AVAILABLE'),
(46, 20, 1, 'SKU-Q1-0020-VG', 'VERY_GOOD', 45000, 2, N'Kệ VH-A2-06', '2026-04-11 09:00:52', 'AVAILABLE'),
(47, 20, 2, 'SKU-TD-0020-GD', 'GOOD', 34000, 2, N'Kệ VH-B1-09', '2026-06-12 09:00:52', 'AVAILABLE'),
(48, 20, 3, 'SKU-BT-0020-LN', 'LIKE_NEW', 56000, 1, N'Kệ VH-C1-08', '2026-06-15 09:00:52', 'AVAILABLE'),
(49, 21, 1, 'SKU-Q1-0021-VG', 'VERY_GOOD', 48000, 3, N'Kệ VH-A4-07', '2026-07-18 09:00:52', 'AVAILABLE'),
(50, 21, 2, 'SKU-TD-0021-GD', 'GOOD', 36000, 2, N'Kệ VH-B1-06', '2026-01-07 09:00:52', 'AVAILABLE'),
(51, 22, 1, 'SKU-Q1-0022-VG', 'VERY_GOOD', 39000, 3, N'Kệ VH-A2-07', '2026-02-18 09:00:52', 'AVAILABLE'),
(52, 22, 2, 'SKU-TD-0022-GD', 'GOOD', 29000, 2, N'Kệ VH-B4-03', '2026-02-27 09:00:52', 'AVAILABLE'),
(53, 22, 3, 'SKU-BT-0022-LN', 'LIKE_NEW', 49000, 1, N'Kệ VH-C1-03', '2026-05-21 09:00:52', 'AVAILABLE'),
(54, 23, 1, 'SKU-Q1-0023-VG', 'VERY_GOOD', 42000, 2, N'Kệ VH-A1-09', '2026-05-29 09:00:52', 'AVAILABLE'),
(55, 23, 2, 'SKU-TD-0023-GD', 'GOOD', 32000, 2, N'Kệ VH-B1-02', '2026-07-13 09:00:52', 'AVAILABLE'),
(56, 24, 1, 'SKU-Q1-0024-VG', 'VERY_GOOD', 35000, 3, N'Kệ VH-A4-04', '2026-08-13 09:00:52', 'AVAILABLE'),
(57, 24, 2, 'SKU-TD-0024-GD', 'GOOD', 26000, 2, N'Kệ VH-B1-01', '2026-01-30 09:00:52', 'AVAILABLE'),
(58, 24, 3, 'SKU-BT-0024-LN', 'LIKE_NEW', 44000, 1, N'Kệ VH-C3-07', '2026-05-07 09:00:52', 'AVAILABLE'),
(59, 25, 1, 'SKU-Q1-0025-VG', 'VERY_GOOD', 41000, 2, N'Kệ VH-A4-09', '2026-07-09 09:00:52', 'AVAILABLE'),
(60, 25, 2, 'SKU-TD-0025-GD', 'GOOD', 31000, 2, N'Kệ VH-B4-07', '2026-02-25 09:00:52', 'AVAILABLE'),
(61, 26, 1, 'SKU-Q1-0026-VG', 'VERY_GOOD', 45000, 3, N'Kệ VH-A2-09', '2026-04-25 09:00:52', 'AVAILABLE'),
(62, 26, 2, 'SKU-TD-0026-GD', 'GOOD', 34000, 1, N'Kệ VH-B1-02', '2026-02-05 09:00:52', 'AVAILABLE'),
(63, 26, 3, 'SKU-BT-0026-LN', 'LIKE_NEW', 56000, 1, N'Kệ VH-C1-06', '2026-08-14 09:00:52', 'AVAILABLE'),
(64, 27, 1, 'SKU-Q1-0027-VG', 'VERY_GOOD', 48000, 3, N'Kệ VH-A2-01', '2026-06-12 09:00:52', 'AVAILABLE'),
(65, 27, 2, 'SKU-TD-0027-GD', 'GOOD', 36000, 2, N'Kệ VH-B4-01', '2026-03-03 09:00:52', 'AVAILABLE'),
(66, 28, 1, 'SKU-Q1-0028-VG', 'VERY_GOOD', 36000, 2, N'Kệ VH-A2-05', '2026-08-11 09:00:52', 'AVAILABLE'),
(67, 28, 2, 'SKU-TD-0028-GD', 'GOOD', 27000, 1, N'Kệ VH-B1-02', '2026-04-22 09:00:52', 'AVAILABLE'),
(68, 28, 3, 'SKU-BT-0028-LN', 'LIKE_NEW', 45000, 1, N'Kệ VH-C1-02', '2026-03-24 09:00:52', 'AVAILABLE'),
(69, 29, 1, 'SKU-Q1-0029-VG', 'VERY_GOOD', 33000, 2, N'Kệ VH-A4-09', '2026-04-18 09:00:52', 'AVAILABLE'),
(70, 29, 2, 'SKU-TD-0029-GD', 'GOOD', 25000, 1, N'Kệ VH-B2-05', '2026-07-07 09:00:52', 'AVAILABLE'),
(71, 30, 1, 'SKU-Q1-0030-VG', 'VERY_GOOD', 51000, 3, N'Kệ VH-A4-03', '2026-05-26 09:00:52', 'AVAILABLE'),
(72, 30, 2, 'SKU-TD-0030-GD', 'GOOD', 38000, 2, N'Kệ VH-B3-02', '2026-07-27 09:00:52', 'AVAILABLE'),
(73, 30, 3, 'SKU-BT-0030-LN', 'LIKE_NEW', 64000, 1, N'Kệ VH-C2-01', '2026-06-15 09:00:52', 'AVAILABLE'),
(74, 31, 1, 'SKU-Q1-0031-VG', 'VERY_GOOD', 30000, 2, N'Kệ VH-A3-03', '2026-01-25 09:00:52', 'AVAILABLE'),
(75, 31, 2, 'SKU-TD-0031-GD', 'GOOD', 22000, 1, N'Kệ VH-B2-06', '2026-07-13 09:00:52', 'AVAILABLE'),
(76, 32, 1, 'SKU-Q1-0032-VG', 'VERY_GOOD', 33000, 3, N'Kệ VH-A3-05', '2026-07-06 09:00:52', 'AVAILABLE'),
(77, 32, 2, 'SKU-TD-0032-GD', 'GOOD', 25000, 2, N'Kệ VH-B4-09', '2026-06-17 09:00:52', 'AVAILABLE'),
(78, 32, 3, 'SKU-BT-0032-LN', 'LIKE_NEW', 41000, 1, N'Kệ VH-C2-09', '2026-08-05 09:00:52', 'AVAILABLE'),
(79, 33, 1, 'SKU-Q1-0033-VG', 'VERY_GOOD', 31000, 2, N'Kệ VH-A1-02', '2026-07-29 09:00:52', 'AVAILABLE'),
(80, 33, 2, 'SKU-TD-0033-GD', 'GOOD', 23000, 1, N'Kệ VH-B4-02', '2026-06-06 09:00:52', 'AVAILABLE'),
(81, 34, 1, 'SKU-Q1-0034-VG', 'VERY_GOOD', 192000, 2, N'Kệ VH-A4-07', '2026-07-03 09:00:52', 'AVAILABLE'),
(82, 34, 2, 'SKU-TD-0034-GD', 'GOOD', 144000, 1, N'Kệ VH-B3-01', '2026-07-13 09:00:52', 'AVAILABLE'),
(83, 34, 3, 'SKU-BT-0034-LN', 'LIKE_NEW', 240000, 1, N'Kệ VH-C2-07', '2026-04-01 09:00:52', 'AVAILABLE'),
(84, 35, 1, 'SKU-Q1-0035-VG', 'VERY_GOOD', 204000, 3, N'Kệ VH-A4-04', '2026-07-20 09:00:52', 'AVAILABLE'),
(85, 35, 2, 'SKU-TD-0035-GD', 'GOOD', 153000, 1, N'Kệ VH-B3-02', '2026-08-12 09:00:52', 'AVAILABLE'),
(86, 36, 1, 'SKU-Q1-0036-VG', 'VERY_GOOD', 198000, 3, N'Kệ VH-A1-08', '2026-07-12 09:00:52', 'AVAILABLE'),
(87, 36, 2, 'SKU-TD-0036-GD', 'GOOD', 148000, 2, N'Kệ VH-B1-05', '2026-05-02 09:00:52', 'AVAILABLE'),
(88, 36, 3, 'SKU-BT-0036-LN', 'LIKE_NEW', 248000, 1, N'Kệ VH-C3-09', '2026-07-10 09:00:52', 'AVAILABLE'),
(89, 37, 1, 'SKU-Q1-0037-VG', 'VERY_GOOD', 216000, 2, N'Kệ VH-A3-03', '2026-06-26 09:00:52', 'AVAILABLE'),
(90, 37, 2, 'SKU-TD-0037-GD', 'GOOD', 162000, 1, N'Kệ VH-B1-04', '2026-07-21 09:00:52', 'AVAILABLE'),
(91, 38, 1, 'SKU-Q1-0038-VG', 'VERY_GOOD', 252000, 2, N'Kệ VH-A2-04', '2026-07-20 09:00:52', 'AVAILABLE'),
(92, 38, 2, 'SKU-TD-0038-GD', 'GOOD', 189000, 2, N'Kệ VH-B1-02', '2026-04-11 09:00:52', 'AVAILABLE'),
(93, 38, 3, 'SKU-BT-0038-LN', 'LIKE_NEW', 315000, 1, N'Kệ VH-C2-03', '2026-04-06 09:00:52', 'AVAILABLE'),
(94, 39, 1, 'SKU-Q1-0039-VG', 'VERY_GOOD', 69000, 3, N'Kệ VH-A4-06', '2026-06-10 09:00:52', 'AVAILABLE'),
(95, 39, 2, 'SKU-TD-0039-GD', 'GOOD', 52000, 2, N'Kệ VH-B2-02', '2026-05-09 09:00:52', 'AVAILABLE'),
(96, 40, 1, 'SKU-Q1-0040-VG', 'VERY_GOOD', 87000, 3, N'Kệ VH-A3-07', '2026-05-09 09:00:52', 'AVAILABLE'),
(97, 40, 2, 'SKU-TD-0040-GD', 'GOOD', 65000, 2, N'Kệ VH-B1-05', '2026-07-03 09:00:52', 'AVAILABLE'),
(98, 40, 3, 'SKU-BT-0040-LN', 'LIKE_NEW', 109000, 1, N'Kệ VH-C3-05', '2026-05-02 09:00:52', 'AVAILABLE'),
(99, 41, 1, 'SKU-Q1-0041-VG', 'VERY_GOOD', 168000, 3, N'Kệ VH-A3-09', '2026-08-11 09:00:52', 'AVAILABLE'),
(100, 41, 2, 'SKU-TD-0041-GD', 'GOOD', 126000, 1, N'Kệ VH-B3-06', '2026-01-22 09:00:52', 'AVAILABLE'),
(101, 42, 1, 'SKU-Q1-0042-VG', 'VERY_GOOD', 96000, 2, N'Kệ VH-A2-03', '2026-03-08 09:00:52', 'AVAILABLE'),
(102, 42, 2, 'SKU-TD-0042-GD', 'GOOD', 72000, 1, N'Kệ VH-B2-09', '2026-04-09 09:00:52', 'AVAILABLE'),
(103, 42, 3, 'SKU-BT-0042-LN', 'LIKE_NEW', 120000, 1, N'Kệ VH-C1-07', '2026-05-26 09:00:52', 'AVAILABLE'),
(104, 43, 1, 'SKU-Q1-0043-VG', 'VERY_GOOD', 51000, 3, N'Kệ VH-A1-04', '2026-07-28 09:00:52', 'AVAILABLE'),
(105, 43, 2, 'SKU-TD-0043-GD', 'GOOD', 38000, 2, N'Kệ VH-B1-06', '2026-06-24 09:00:52', 'AVAILABLE'),
(106, 44, 1, 'SKU-Q1-0044-VG', 'VERY_GOOD', 65000, 2, N'Kệ VH-A1-05', '2026-07-13 09:00:52', 'AVAILABLE'),
(107, 44, 2, 'SKU-TD-0044-GD', 'GOOD', 49000, 2, N'Kệ VH-B2-03', '2026-03-27 09:00:52', 'AVAILABLE'),
(108, 44, 3, 'SKU-BT-0044-LN', 'LIKE_NEW', 81000, 1, N'Kệ VH-C2-01', '2026-04-08 09:00:52', 'AVAILABLE'),
(109, 45, 1, 'SKU-Q1-0045-VG', 'VERY_GOOD', 63000, 3, N'Kệ VH-A3-07', '2026-04-08 09:00:52', 'AVAILABLE'),
(110, 45, 2, 'SKU-TD-0045-GD', 'GOOD', 47000, 2, N'Kệ VH-B4-07', '2026-03-27 09:00:52', 'AVAILABLE'),
(111, 46, 1, 'SKU-Q1-0046-VG', 'VERY_GOOD', 90000, 2, N'Kệ VH-A2-01', '2026-03-13 09:00:52', 'AVAILABLE'),
(112, 46, 2, 'SKU-TD-0046-GD', 'GOOD', 68000, 2, N'Kệ VH-B3-08', '2026-06-06 09:00:52', 'AVAILABLE'),
(113, 46, 3, 'SKU-BT-0046-LN', 'LIKE_NEW', 112000, 1, N'Kệ VH-C2-03', '2026-08-20 09:00:52', 'AVAILABLE'),
(114, 47, 1, 'SKU-Q1-0047-VG', 'VERY_GOOD', 57000, 2, N'Kệ VH-A2-04', '2026-08-19 09:00:52', 'AVAILABLE'),
(115, 47, 2, 'SKU-TD-0047-GD', 'GOOD', 43000, 2, N'Kệ VH-B4-09', '2026-07-26 09:00:52', 'AVAILABLE'),
(116, 48, 1, 'SKU-Q1-0048-VG', 'VERY_GOOD', 66000, 3, N'Kệ VH-A3-09', '2026-06-10 09:00:52', 'AVAILABLE'),
(117, 48, 2, 'SKU-TD-0048-GD', 'GOOD', 50000, 2, N'Kệ VH-B1-02', '2026-06-27 09:00:52', 'AVAILABLE'),
(118, 48, 3, 'SKU-BT-0048-LN', 'LIKE_NEW', 82000, 1, N'Kệ VH-C1-08', '2026-05-15 09:00:52', 'AVAILABLE'),
(119, 49, 1, 'SKU-Q1-0049-VG', 'VERY_GOOD', 54000, 2, N'Kệ VH-A2-06', '2026-07-23 09:00:52', 'AVAILABLE'),
(120, 49, 2, 'SKU-TD-0049-GD', 'GOOD', 40000, 1, N'Kệ VH-B3-04', '2026-01-18 09:00:52', 'AVAILABLE'),
(121, 50, 1, 'SKU-Q1-0050-VG', 'VERY_GOOD', 57000, 3, N'Kệ VH-A4-01', '2026-07-18 09:00:52', 'AVAILABLE'),
(122, 50, 2, 'SKU-TD-0050-GD', 'GOOD', 43000, 1, N'Kệ VH-B4-05', '2026-03-01 09:00:52', 'AVAILABLE'),
(123, 50, 3, 'SKU-BT-0050-LN', 'LIKE_NEW', 71000, 1, N'Kệ VH-C3-04', '2026-04-07 09:00:52', 'AVAILABLE'),
(124, 51, 1, 'SKU-Q1-0051-VG', 'VERY_GOOD', 51000, 2, N'Kệ VH-A4-03', '2026-07-08 09:00:52', 'AVAILABLE'),
(125, 51, 2, 'SKU-TD-0051-GD', 'GOOD', 38000, 1, N'Kệ VH-B4-01', '2026-07-30 09:00:52', 'AVAILABLE'),
(126, 52, 1, 'SKU-Q1-0052-VG', 'VERY_GOOD', 48000, 2, N'Kệ VH-A2-04', '2026-06-26 09:00:52', 'AVAILABLE'),
(127, 52, 2, 'SKU-TD-0052-GD', 'GOOD', 36000, 1, N'Kệ VH-B2-06', '2026-07-10 09:00:52', 'AVAILABLE'),
(128, 52, 3, 'SKU-BT-0052-LN', 'LIKE_NEW', 60000, 1, N'Kệ VH-C2-01', '2026-04-19 09:00:52', 'AVAILABLE'),
(129, 53, 1, 'SKU-Q1-0053-VG', 'VERY_GOOD', 59000, 3, N'Kệ VH-A3-07', '2026-07-15 09:00:52', 'AVAILABLE'),
(130, 53, 2, 'SKU-TD-0053-GD', 'GOOD', 44000, 2, N'Kệ VH-B1-04', '2026-04-21 09:00:52', 'AVAILABLE'),
(131, 54, 1, 'SKU-Q1-0054-VG', 'VERY_GOOD', 63000, 2, N'Kệ VH-A3-06', '2026-08-16 09:00:52', 'AVAILABLE'),
(132, 54, 2, 'SKU-TD-0054-GD', 'GOOD', 47000, 2, N'Kệ VH-B4-08', '2026-07-22 09:00:52', 'AVAILABLE'),
(133, 54, 3, 'SKU-BT-0054-LN', 'LIKE_NEW', 79000, 1, N'Kệ VH-C1-08', '2026-07-21 09:00:52', 'AVAILABLE'),
(134, 55, 1, 'SKU-Q1-0055-VG', 'VERY_GOOD', 67000, 2, N'Kệ VH-A3-04', '2026-01-22 09:00:52', 'AVAILABLE'),
(135, 55, 2, 'SKU-TD-0055-GD', 'GOOD', 50000, 1, N'Kệ VH-B4-07', '2026-04-03 09:00:52', 'AVAILABLE'),
(136, 56, 1, 'SKU-Q1-0056-VG', 'VERY_GOOD', 53000, 3, N'Kệ VH-A2-06', '2026-07-11 09:00:52', 'AVAILABLE'),
(137, 56, 2, 'SKU-TD-0056-GD', 'GOOD', 40000, 1, N'Kệ VH-B1-07', '2026-08-13 09:00:52', 'AVAILABLE'),
(138, 56, 3, 'SKU-BT-0056-LN', 'LIKE_NEW', 66000, 1, N'Kệ VH-C1-06', '2026-05-01 09:00:52', 'AVAILABLE'),
(139, 57, 1, 'SKU-Q1-0057-VG', 'VERY_GOOD', 57000, 2, N'Kệ VH-A2-08', '2026-05-28 09:00:52', 'AVAILABLE'),
(140, 57, 2, 'SKU-TD-0057-GD', 'GOOD', 43000, 1, N'Kệ VH-B2-05', '2026-04-25 09:00:52', 'AVAILABLE'),
(141, 58, 1, 'SKU-Q1-0058-VG', 'VERY_GOOD', 39000, 3, N'Kệ VH-A1-08', '2026-08-27 09:00:52', 'AVAILABLE'),
(142, 58, 2, 'SKU-TD-0058-GD', 'GOOD', 29000, 2, N'Kệ VH-B4-07', '2026-06-02 09:00:52', 'AVAILABLE'),
(143, 58, 3, 'SKU-BT-0058-LN', 'LIKE_NEW', 49000, 1, N'Kệ VH-C2-02', '2026-06-13 09:00:52', 'AVAILABLE'),
(144, 59, 1, 'SKU-Q1-0059-VG', 'VERY_GOOD', 51000, 2, N'Kệ VH-A2-03', '2026-02-01 09:00:52', 'AVAILABLE'),
(145, 59, 2, 'SKU-TD-0059-GD', 'GOOD', 38000, 2, N'Kệ VH-B3-02', '2026-07-06 09:00:52', 'AVAILABLE'),
(146, 60, 1, 'SKU-Q1-0060-VG', 'VERY_GOOD', 162000, 2, N'Kệ VH-A3-06', '2026-07-29 09:00:52', 'AVAILABLE'),
(147, 60, 2, 'SKU-TD-0060-GD', 'GOOD', 122000, 2, N'Kệ VH-B1-06', '2026-04-30 09:00:52', 'AVAILABLE'),
(148, 60, 3, 'SKU-BT-0060-LN', 'LIKE_NEW', 202000, 1, N'Kệ VH-C1-07', '2026-04-23 09:00:52', 'AVAILABLE'),
(149, 61, 1, 'SKU-Q1-0061-VG', 'VERY_GOOD', 81000, 3, N'Kệ VH-A2-06', '2026-01-31 09:00:52', 'AVAILABLE'),
(150, 61, 2, 'SKU-TD-0061-GD', 'GOOD', 61000, 2, N'Kệ VH-B3-08', '2026-05-07 09:00:52', 'AVAILABLE'),
(151, 62, 1, 'SKU-Q1-0062-VG', 'VERY_GOOD', 66000, 3, N'Kệ VH-A2-02', '2026-04-17 09:00:52', 'AVAILABLE'),
(152, 62, 2, 'SKU-TD-0062-GD', 'GOOD', 50000, 2, N'Kệ VH-B2-01', '2026-01-09 09:00:52', 'AVAILABLE'),
(153, 62, 3, 'SKU-BT-0062-LN', 'LIKE_NEW', 82000, 1, N'Kệ VH-C1-08', '2026-04-11 09:00:52', 'AVAILABLE'),
(154, 63, 1, 'SKU-Q1-0063-VG', 'VERY_GOOD', 36000, 2, N'Kệ VH-A2-09', '2026-04-13 09:00:52', 'AVAILABLE'),
(155, 63, 2, 'SKU-TD-0063-GD', 'GOOD', 27000, 1, N'Kệ VH-B1-06', '2026-06-07 09:00:52', 'AVAILABLE'),
(156, 64, 1, 'SKU-Q1-0064-VG', 'VERY_GOOD', 84000, 3, N'Kệ VH-A3-04', '2026-02-21 09:00:52', 'AVAILABLE'),
(157, 64, 2, 'SKU-TD-0064-GD', 'GOOD', 63000, 1, N'Kệ VH-B3-05', '2026-08-13 09:00:52', 'AVAILABLE'),
(158, 64, 3, 'SKU-BT-0064-LN', 'LIKE_NEW', 105000, 1, N'Kệ VH-C2-09', '2026-07-22 09:00:52', 'AVAILABLE'),
(159, 65, 1, 'SKU-Q1-0065-VG', 'VERY_GOOD', 69000, 3, N'Kệ VH-A1-03', '2026-06-03 09:00:52', 'AVAILABLE'),
(160, 65, 2, 'SKU-TD-0065-GD', 'GOOD', 52000, 2, N'Kệ VH-B3-07', '2026-06-24 09:00:52', 'AVAILABLE'),
(161, 66, 1, 'SKU-Q1-0066-VG', 'VERY_GOOD', 33000, 2, N'Kệ VH-A3-07', '2026-01-22 09:00:52', 'AVAILABLE'),
(162, 66, 2, 'SKU-TD-0066-GD', 'GOOD', 25000, 2, N'Kệ VH-B1-06', '2026-05-15 09:00:52', 'AVAILABLE'),
(163, 66, 3, 'SKU-BT-0066-LN', 'LIKE_NEW', 41000, 1, N'Kệ VH-C2-09', '2026-03-23 09:00:52', 'AVAILABLE'),
(164, 67, 1, 'SKU-Q1-0067-VG', 'VERY_GOOD', 54000, 3, N'Kệ VH-A3-09', '2026-02-25 09:00:52', 'AVAILABLE'),
(165, 67, 2, 'SKU-TD-0067-GD', 'GOOD', 40000, 1, N'Kệ VH-B2-01', '2026-05-04 09:00:52', 'AVAILABLE'),
(166, 68, 1, 'SKU-Q1-0068-VG', 'VERY_GOOD', 66000, 3, N'Kệ VH-A4-02', '2026-07-25 09:00:52', 'AVAILABLE'),
(167, 68, 2, 'SKU-TD-0068-GD', 'GOOD', 50000, 1, N'Kệ VH-B2-04', '2026-06-29 09:00:52', 'AVAILABLE'),
(168, 68, 3, 'SKU-BT-0068-LN', 'LIKE_NEW', 82000, 1, N'Kệ VH-C2-03', '2026-07-08 09:00:52', 'AVAILABLE'),
(169, 69, 1, 'SKU-Q1-0069-VG', 'VERY_GOOD', 84000, 3, N'Kệ VH-A1-07', '2026-07-19 09:00:52', 'AVAILABLE'),
(170, 69, 2, 'SKU-TD-0069-GD', 'GOOD', 63000, 1, N'Kệ VH-B4-08', '2026-01-09 09:00:52', 'AVAILABLE'),
(171, 70, 1, 'SKU-Q1-0070-VG', 'VERY_GOOD', 81000, 3, N'Kệ VH-A2-04', '2026-08-11 09:00:52', 'AVAILABLE'),
(172, 70, 2, 'SKU-TD-0070-GD', 'GOOD', 61000, 2, N'Kệ VH-B2-07', '2026-08-13 09:00:52', 'AVAILABLE'),
(173, 70, 3, 'SKU-BT-0070-LN', 'LIKE_NEW', 101000, 1, N'Kệ VH-C2-06', '2026-04-18 09:00:52', 'AVAILABLE'),
(174, 71, 1, 'SKU-Q1-0071-VG', 'VERY_GOOD', 45000, 3, N'Kệ VH-A4-09', '2026-02-01 09:00:52', 'AVAILABLE'),
(175, 71, 2, 'SKU-TD-0071-GD', 'GOOD', 34000, 1, N'Kệ VH-B3-08', '2026-04-01 09:00:52', 'AVAILABLE'),
(176, 72, 1, 'SKU-Q1-0072-VG', 'VERY_GOOD', 117000, 2, N'Kệ KT-A4-08', '2026-06-10 09:00:52', 'AVAILABLE'),
(177, 72, 2, 'SKU-TD-0072-GD', 'GOOD', 88000, 2, N'Kệ KT-B2-07', '2026-06-18 09:00:52', 'AVAILABLE'),
(178, 72, 3, 'SKU-BT-0072-LN', 'LIKE_NEW', 146000, 1, N'Kệ KT-C1-02', '2026-07-10 09:00:52', 'AVAILABLE'),
(179, 73, 1, 'SKU-Q1-0073-VG', 'VERY_GOOD', 123000, 2, N'Kệ KT-A4-02', '2026-06-08 09:00:52', 'AVAILABLE'),
(180, 73, 2, 'SKU-TD-0073-GD', 'GOOD', 92000, 2, N'Kệ KT-B3-05', '2026-07-25 09:00:52', 'AVAILABLE'),
(181, 74, 1, 'SKU-Q1-0074-VG', 'VERY_GOOD', 111000, 3, N'Kệ KT-A1-09', '2026-08-26 09:00:52', 'AVAILABLE'),
(182, 74, 2, 'SKU-TD-0074-GD', 'GOOD', 83000, 2, N'Kệ KT-B2-06', '2026-04-16 09:00:52', 'AVAILABLE'),
(183, 74, 3, 'SKU-BT-0074-LN', 'LIKE_NEW', 139000, 1, N'Kệ KT-C3-05', '2026-05-29 09:00:52', 'AVAILABLE'),
(184, 75, 1, 'SKU-Q1-0075-VG', 'VERY_GOOD', 126000, 3, N'Kệ KT-A2-08', '2026-02-12 09:00:52', 'AVAILABLE'),
(185, 75, 2, 'SKU-TD-0075-GD', 'GOOD', 94000, 1, N'Kệ KT-B3-03', '2026-04-29 09:00:52', 'AVAILABLE'),
(186, 76, 1, 'SKU-Q1-0076-VG', 'VERY_GOOD', 135000, 3, N'Kệ KT-A3-03', '2026-08-17 09:00:52', 'AVAILABLE'),
(187, 76, 2, 'SKU-TD-0076-GD', 'GOOD', 101000, 1, N'Kệ KT-B3-03', '2026-02-15 09:00:52', 'AVAILABLE'),
(188, 76, 3, 'SKU-BT-0076-LN', 'LIKE_NEW', 169000, 1, N'Kệ KT-C1-04', '2026-04-04 09:00:52', 'AVAILABLE'),
(189, 77, 1, 'SKU-Q1-0077-VG', 'VERY_GOOD', 113000, 2, N'Kệ KT-A3-02', '2026-04-25 09:00:52', 'AVAILABLE'),
(190, 77, 2, 'SKU-TD-0077-GD', 'GOOD', 85000, 1, N'Kệ KT-B2-01', '2026-02-09 09:00:52', 'AVAILABLE'),
(191, 78, 1, 'SKU-Q1-0078-VG', 'VERY_GOOD', 96000, 3, N'Kệ IT-A1-06', '2026-05-09 09:00:52', 'AVAILABLE'),
(192, 78, 2, 'SKU-TD-0078-GD', 'GOOD', 72000, 1, N'Kệ IT-B1-04', '2026-04-20 09:00:52', 'AVAILABLE'),
(193, 78, 3, 'SKU-BT-0078-LN', 'LIKE_NEW', 120000, 1, N'Kệ IT-C3-09', '2026-03-21 09:00:52', 'AVAILABLE'),
(194, 79, 1, 'SKU-Q1-0079-UNQ', 'ACCEPTABLE', 52000, 1, N'Kệ IT-S1-04', '2026-07-12 09:00:52', 'AVAILABLE'),
(195, 80, 2, 'SKU-TD-0080-UNQ', 'VERY_GOOD', 57000, 1, N'Kệ IT-S2-09', '2025-12-01 09:00:52', 'AVAILABLE'),
(196, 81, 3, 'SKU-BT-0081-UNQ', 'VERY_GOOD', 63000, 1, N'Kệ IT-S3-09', '2026-08-05 09:00:52', 'AVAILABLE'),
(197, 82, 1, 'SKU-Q1-0082-UNQ', 'ACCEPTABLE', 34000, 1, N'Kệ IT-S1-05', '2026-01-03 09:00:52', 'AVAILABLE'),
(198, 83, 2, 'SKU-TD-0083-UNQ', 'VERY_GOOD', 51000, 1, N'Kệ IT-S2-02', '2026-08-10 09:00:52', 'AVAILABLE'),
(199, 84, 3, 'SKU-BT-0084-UNQ', 'LIKE_NEW', 74000, 1, N'Kệ KT-S3-07', '2026-01-04 09:00:52', 'AVAILABLE'),
(200, 85, 1, 'SKU-Q1-0085-UNQ', 'GOOD', 50000, 1, N'Kệ KT-S1-04', '2026-08-18 09:00:52', 'AVAILABLE'),
(201, 86, 2, 'SKU-TD-0086-UNQ', 'LIKE_NEW', 124000, 1, N'Kệ KT-S2-05', '2025-12-19 09:00:52', 'AVAILABLE'),
(202, 87, 3, 'SKU-BT-0087-UNQ', 'GOOD', 79000, 1, N'Kệ KT-S3-03', '2026-06-15 09:00:52', 'AVAILABLE'),
(203, 88, 1, 'SKU-Q1-0088-UNQ', 'LIKE_NEW', 109000, 1, N'Kệ KT-S1-07', '2026-02-25 09:00:52', 'AVAILABLE'),
(204, 89, 2, 'SKU-TD-0089-UNQ', 'GOOD', 34000, 1, N'Kệ KT-S2-08', '2026-05-26 09:00:52', 'AVAILABLE'),
(205, 90, 3, 'SKU-BT-0090-UNQ', 'ACCEPTABLE', 20000, 1, N'Kệ KT-S3-01', '2025-12-19 09:00:52', 'AVAILABLE'),
(206, 91, 1, 'SKU-Q1-0091-UNQ', 'GOOD', 44000, 1, N'Kệ KT-S1-07', '2026-08-11 09:00:52', 'AVAILABLE'),
(207, 92, 2, 'SKU-TD-0092-UNQ', 'ACCEPTABLE', 72000, 1, N'Kệ VH-S2-07', '2026-02-17 09:00:52', 'AVAILABLE'),
(208, 93, 3, 'SKU-BT-0093-UNQ', 'ACCEPTABLE', 87000, 1, N'Kệ VH-S3-08', '2026-07-21 09:00:52', 'AVAILABLE'),
(209, 94, 1, 'SKU-Q1-0094-UNQ', 'GOOD', 117000, 1, N'Kệ VH-S1-07', '2026-02-20 09:00:52', 'AVAILABLE'),
(210, 95, 2, 'SKU-TD-0095-UNQ', 'LIKE_NEW', 165000, 1, N'Kệ VH-S2-06', '2026-07-15 09:00:52', 'AVAILABLE'),
(211, 96, 3, 'SKU-BT-0096-UNQ', 'GOOD', 220000, 1, N'Kệ KN-S3-03', '2026-02-07 09:00:52', 'AVAILABLE'),
(212, 97, 1, 'SKU-Q1-0097-UNQ', 'GOOD', 234000, 1, N'Kệ KN-S1-02', '2026-05-30 09:00:52', 'AVAILABLE'),
(213, 98, 2, 'SKU-TD-0098-UNQ', 'ACCEPTABLE', 138000, 1, N'Kệ KN-S2-04', '2025-12-11 09:00:52', 'AVAILABLE'),
(214, 99, 3, 'SKU-BT-0099-UNQ', 'VERY_GOOD', 234000, 1, N'Kệ KN-S3-04', '2026-06-24 09:00:52', 'AVAILABLE'),
(215, 100, 1, 'SKU-Q1-0100-UNQ', 'ACCEPTABLE', 153000, 1, N'Kệ KN-S1-09', '2026-01-15 09:00:52', 'AVAILABLE');
SET IDENTITY_INSERT book_items OFF;
GO

-- Chèn book_item_images mẫu
SET IDENTITY_INSERT book_item_images ON;
INSERT INTO book_item_images (id, book_item_id, image_url, cloudinary_public_id, is_primary) VALUES
(1, 1, 'https://res.cloudinary.com/antigravity/image/upload/v1/branch_items/q1_dnt_front.webp', 'branch_items/q1_dnt_front', 1),
(2, 1, 'https://res.cloudinary.com/antigravity/image/upload/v1/branch_items/q1_dnt_spine.webp', 'branch_items/q1_dnt_spine', 0),
(3, 3, 'https://res.cloudinary.com/antigravity/image/upload/v1/branch_items/q1_hvtcx_front.webp', 'branch_items/q1_hvtcx_front', 1),
(4, 5, 'https://res.cloudinary.com/antigravity/image/upload/v1/branch_items/td_clnc_detail.webp', 'branch_items/td_clnc_detail', 1);
SET IDENTITY_INSERT book_item_images OFF;
GO

-- Chèn coupons mẫu
SET IDENTITY_INSERT coupons ON;
INSERT INTO coupons (id, code, discount_type, discount_value, min_order_amount, max_discount_amount, applicable_condition_grade, applicable_branch_id, usage_limit, used_count, start_date, end_date, is_active) VALUES
(1, 'XACHO50', 'PERCENT', 50, 100000, 50000, 'ACCEPTABLE', NULL, 100, 0, '2026-01-01', '2026-12-31', 1),
(2, 'KHAITRUONG_TD', 'FIXED_AMOUNT', 20000, 80000, NULL, NULL, 2, 200, 1, '2026-01-01', '2026-12-31', 1),
(3, 'TRIANVIP', 'PERCENT', 10, 0, 100000, NULL, NULL, 500, 0, '2026-01-01', '2026-12-31', 1);
SET IDENTITY_INSERT coupons OFF;
GO

-- Chèn master_orders mẫu
SET IDENTITY_INSERT master_orders ON;
INSERT INTO master_orders (id, master_order_code, user_id, coupon_id, total_items_amount, total_shipping_fee, discount_amount, final_amount, payment_method, payment_status, receiver_name, receiver_phone, shipping_address, created_at) VALUES
(1, 'ORD-2026-9801', 7, NULL, 345000, 45000, 0, 390000, 'VNPAY', 'PAID', N'Hoàng Tiến Dũng', '0912.888.999', N'Số 12 Nguyễn Du, Phường Bến Nghé, Quận 1, TP.HCM', GETDATE()),
(2, 'ORD-2026-9802', 8, 2, 45000, 15000, 20000, 40000, 'COD', 'PENDING', N'Nguyễn Tuyết Mai', '0918.777.666', N'Ký túc xá Khu B ĐHQG, TP. Thủ Đức, TP.HCM', GETDATE());
SET IDENTITY_INSERT master_orders OFF;
GO

-- Chèn sub_orders mẫu (Tách đơn hàng theo chi nhánh)
SET IDENTITY_INSERT sub_orders ON;
INSERT INTO sub_orders (id, master_order_id, branch_id, sub_order_code, branch_subtotal, branch_shipping_fee, tracking_number, status, created_at) VALUES
(1, 1, 1, 'SUB-Q1-9801-1', 55000, 20000, 'VNPOST-9912', 'PACKING', GETDATE()),
(2, 1, 2, 'SUB-TD-9801-2', 290000, 25000, 'GHN-7788', 'SHIPPED', GETDATE()),
(3, 2, 2, 'SUB-TD-9802-1', 45000, 15000, NULL, 'PENDING', GETDATE());
SET IDENTITY_INSERT sub_orders OFF;
GO

-- Chèn sub_order_items mẫu
SET IDENTITY_INSERT sub_order_items ON;
INSERT INTO sub_order_items (id, sub_order_id, book_item_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 1, 55000, 55000),
(2, 2, 5, 1, 290000, 290000),
(3, 3, 2, 1, 45000, 45000);
SET IDENTITY_INSERT sub_order_items OFF;
GO

-- Chèn trade_in_requests mẫu
SET IDENTITY_INSERT trade_in_requests ON;
INSERT INTO trade_in_requests (id, request_code, user_id, book_id, target_branch_id, user_condition_grade, survey_answers_json, auto_estimated_price, staff_reviewed_price, staff_condition_grade, reviewed_by_staff_id, status, delivery_method, staff_notes, created_at) VALUES
(1, 'TI-2026-0012', 7, 5, 1, 'VERY_GOOD', N'{"cover":"good","spine":"intact","pages":"slightly_yellowed","notes":false}', 38000, 35000, 'VERY_GOOD', 3, 'ACCEPTED_AWAITING_DELIVERY', 'BRING_TO_STORE', N'Sách nguyên vẹn, ố nhẹ mép giấy, giá 35k phù hợp', GETDATE()),
(2, 'TI-2026-0013', 8, 13, 2, 'LIKE_NEW', N'{"cover":"perfect","spine":"tight","pages":"clean","notes":false}', 185000, NULL, NULL, NULL, 'PENDING_REVIEW', 'POSTAL_SHIPPING', NULL, GETDATE());
SET IDENTITY_INSERT trade_in_requests OFF;
GO

-- Chèn trade_in_images mẫu
SET IDENTITY_INSERT trade_in_images ON;
INSERT INTO trade_in_images (id, trade_in_request_id, image_url, cloudinary_public_id, image_type) VALUES
(1, 1, 'https://res.cloudinary.com/antigravity/image/upload/v1/trade_in/u7_cover.webp', 'trade_in/u7_cover', 'COVER'),
(2, 1, 'https://res.cloudinary.com/antigravity/image/upload/v1/trade_in/u7_spine.webp', 'trade_in/u7_spine', 'SPINE'),
(3, 1, 'https://res.cloudinary.com/antigravity/image/upload/v1/trade_in/u7_page_defect.webp', 'trade_in/u7_page_defect', 'DEFECT_PAGE'),
(4, 2, 'https://res.cloudinary.com/antigravity/image/upload/v1/trade_in/u8_clnc_cover.webp', 'trade_in/u8_clnc_cover', 'COVER');
SET IDENTITY_INSERT trade_in_images OFF;
GO

-- Chèn stock_transfers mẫu
SET IDENTITY_INSERT stock_transfers ON;
INSERT INTO stock_transfers (id, transfer_code, from_branch_id, to_branch_id, requested_by_user_id, status, reason, created_at, completed_at) VALUES
(1, 'TRF-2026-0889', 1, 2, 2, 'IN_TRANSIT', N'Điều chuyển cân đối sách Tôi Thấy Hoa Vàng...', GETDATE(), NULL),
(2, 'TRF-2026-0890', 2, 1, 2, 'REJECTED', N'Xin sách Clean Code nhưng vi phạm quy tắc luân chuyển', GETDATE(), NULL);
SET IDENTITY_INSERT stock_transfers OFF;
GO

-- Chèn stock_transfer_items mẫu
SET IDENTITY_INSERT stock_transfer_items ON;
INSERT INTO stock_transfer_items (id, transfer_id, book_item_id, quantity) VALUES
(1, 1, 3, 1),
(2, 2, 5, 1);
SET IDENTITY_INSERT stock_transfer_items OFF;
GO

-- Chèn wishlists mẫu
SET IDENTITY_INSERT wishlists ON;
INSERT INTO wishlists (id, user_id, book_id, preferred_branch_id, min_condition_grade, is_notified, created_at, notified_at) VALUES
(1, 7, 18, 2, 'GOOD', 0, GETDATE(), NULL),
(2, 8, 13, NULL, 'VERY_GOOD', 1, DATEADD(day, -5, GETDATE()), GETDATE());
SET IDENTITY_INSERT wishlists OFF;
GO

PRINT N'========================================================================';
PRINT N'ĐÃ TẠO DATABASE VÀ CHÈN DỮ LIỆU THÀNH CÔNG CHO BookstoreChainDB!';
PRINT N'========================================================================';
GO

-- ============================================================================
-- B?NG X�C TH?C M� OTP �ANG K� T�I KHO?N QUA EMAIL (5 PH�T H?T H?N)
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'otp_verifications')
BEGIN
    CREATE TABLE otp_verifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email NVARCHAR(255) NOT NULL,
        otp_code VARCHAR(10) NOT NULL,
        password_hash NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(255) NULL,
        phone VARCHAR(20) NULL,
        attempts INT NOT NULL DEFAULT 0,
        is_used BIT NOT NULL DEFAULT 0,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        expires_at DATETIME NOT NULL,
        cooldown_until DATETIME NOT NULL
    );
    CREATE INDEX idx_otp_email ON otp_verifications(email);
    CREATE INDEX idx_otp_expires ON otp_verifications(expires_at);
END;


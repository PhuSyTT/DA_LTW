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
    biography NVARCHAR(MAX) NULL
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
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PACKING, SHIPPED, COMPLETED, CANCELLED
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
(2, N'Kỹ Năng Sống - Tâm Lý', NULL),
(3, N'Công Nghệ Thông Tin - Lập Trình', NULL),
(4, N'Kinh Tế - Đầu Tư', NULL);
SET IDENTITY_INSERT categories OFF;
GO

-- Chèn 35 authors chuẩn
SET IDENTITY_INSERT authors ON;
INSERT INTO authors (id, name, biography) VALUES
(1, N'Dale Carnegie', N'Tác giả người Mỹ, nổi tiếng với các sách rèn luyện kỹ năng giao tiếp kinh điển như Đắc Nhân Tâm, Quẳng Gánh Lo Đi'),
(2, N'Nguyễn Nhật Ánh', N'Nhà văn nổi tiếng của Việt Nam với nhiều tác phẩm gắn liền tuổi thơ như Tôi Thấy Hoa Vàng Trên Cỏ Xanh, Mắt Biếc'),
(3, N'Robert C. Martin (Uncle Bob)', N'Chuyên gia công nghệ phần mềm hàng đầu thế giới, tác giả các tác phẩm Clean Code, Clean Architecture'),
(4, N'Paulo Coelho', N'Tiểu thuyết gia nổi tiếng người Brasil, tác giả kiệt tác văn học toàn cầu Nhà Giả Kim'),
(5, N'Nam Cao', N'Đại văn hào hiện thực phê phán Việt Nam thế kỷ 20, tác giả Chí Phèo, Lão Hạc, Đôi Mắt, Sống Mòn'),
(6, N'Vũ Trọng Phụng', N'Nhà văn trào phúng hiện thực xuất sắc của văn học Việt Nam, tác giả Số Đỏ, Giông Tố, Kỹ Nghệ Lấy Tây'),
(7, N'Tô Hoài', N'Cây đại thụ văn học Việt Nam với kiệt tác thiếu nhi Dế Mèn Phiêu Lưu Ký và Vợ Chồng A Phủ'),
(8, N'Thạch Lam', N'Nhà văn tiêu biểu nhóm Tự Lực Văn Đoàn với ngòi bút tinh tế, tác giả Gió Đầu Mùa, Hà Nội 36 Phố Phường'),
(9, N'Kim Dung', N'Đại thụ tiểu thuyết kiếm hiệp Á Đông, tác giả Anh Hùng Xạ Điêu, Thần Điêu Đại Hiệp, Tiếu Ngạo Giang Hồ'),
(10, N'Haruki Murakami', N'Nhà văn đương đại kiệt xuất người Nhật Bản, tác giả Rừng Na Uy, Kafka Bên Bờ Biển, 1Q84'),
(11, N'Higashino Keigo', N'Bậc thầy tiểu thuyết trinh thám tâm lý Nhật Bản, tác giả Phía Sau Nghi Can X, Tiệm Tạp Hóa Namiya'),
(12, N'Arthur Conan Doyle', N'Nhà văn vĩ đại người Scotland, cha đẻ của tượng đài thám tử lừng danh Sherlock Holmes'),
(13, N'Agatha Christie', N'Nữ hoàng tiểu thuyết trinh thám thế giới với hàng trăm vụ án bí ẩn của thám tử Hercule Poirot'),
(14, N'George Orwell', N'Nhà văn tư tưởng kiệt xuất người Anh, tác giả 1984 và Chuyện Ở Nông Trại (Animal Farm)'),
(15, N'Victor Hugo', N'Đại văn hào Pháp của phong trào lãng mạn, tác giả Những Người Khốn Khổ, Nhà Thờ Đức Bà Paris'),
(16, N'Ernest Hemingway', N'Nhà văn Mỹ đoạt giải Nobel Văn học, cha đẻ nguyên lý tảng băng trôi và tác phẩm Ông Già Và Biển Cả'),
(17, N'Franz Kafka', N'Nhà văn hiện sinh vĩ đại người Áo-Hung, tác giả các kiệt tác Biến Dạng (Hóa Thân), Vụ Án'),
(18, N'Gabriel García Márquez', N'Đại văn hào Colombia đoạt giải Nobel Văn học, bậc thầy chủ nghĩa hiện thực huyền ảo với Trăm Năm Cô Đơn'),
(19, N'Yuval Noah Harari', N'Giáo sư sử học người Israel, tác giả bộ ba kinh điển Sapiens: Lược Sử Loài Người, Homo Deus, 21 Bài Học'),
(20, N'Daniel Kahneman', N'Nhà tâm lý học đoạt giải Nobel Kinh tế, tác giả cuốn sách đột phá Tư Duy Nhanh Và Chậm'),
(21, N'James Clear', N'Chuyên gia nghiên cứu hành vi và thói quen, tác giả cuốn sách bán chạy toàn cầu Atomic Habits'),
(22, N'Morgan Housel', N'Chuyên gia tài chính và đối tác quỹ Collaborative Fund, tác giả Tâm Lý Học Về Tiền'),
(23, N'Robert Kiyosaki', N'Doanh nhân và nhà đầu tư, tác giả bộ sách giáo dục tài chính Dạy Con Làm Giàu'),
(24, N'Napoleon Hill', N'Bậc thầy triết lý thành công cá nhân, tác giả tác phẩm Nghĩ Giàu Và Làm Giàu'),
(25, N'Stephen R. Covey', N'Chuyên gia phát triển năng lực lãnh đạo, tác giả 7 Thói Quen Của Người Thành Đạt'),
(26, N'Brian Tracy', N'Diễn giả truyền cảm hứng và quản trị thời gian, tác giả Hãy Ăn Con Ếch Đó'),
(27, N'Walter Isaacson', N'Nhà viết tiểu sử hàng đầu nước Mỹ, tác giả tiểu sử Steve Jobs, Elon Musk, Einstein'),
(28, N'Martin Fowler', N'Chuyên gia kiến trúc phần mềm tại ThoughtWorks, người đặt nền móng cho Refactoring và Microservices'),
(29, N'Joshua Bloch', N'Cựu kiến trúc sư trưởng Java tại Sun Microsystems/Google, tác giả cuốn sách Effective Java'),
(30, N'Erich Gamma', N'Đồng tác giả bộ tứ Gang of Four (GoF) với cuốn Design Patterns'),
(31, N'Donald Knuth', N'Huyền thoại khoa học máy tính thế giới, tác giả bộ The Art of Computer Programming'),
(32, N'Andrew S. Tanenbaum', N'Giáo sư khoa học máy tính, tác giả các giáo trình kinh điển Mạng Máy Tính và Hệ Điều Hành'),
(33, N'Thomas H. Cormen', N'Giáo sư khoa học máy tính tại Dartmouth College, đồng tác giả Introduction to Algorithms (CLRS)'),
(34, N'Eric Ries', N'Doanh nhân Thung lũng Silicon, cha đẻ phương pháp Khởi Nghiệp Tinh Gọn (The Lean Startup)'),
(35, N'Cal Newport', N'Giáo sư khoa học máy tính tại ĐH Georgetown, tác giả cuốn sách Deep Work');
SET IDENTITY_INSERT authors OFF;
GO

-- Chèn 110 books chuẩn hóa
SET IDENTITY_INSERT books ON;
INSERT INTO books (id, title, isbn, author_id, category_id, publisher, publish_year, original_price) VALUES
(1, N'Đắc Nhân Tâm (How to Win Friends and Influence People)', '978-604-58-1234-1', 1, 2, N'NXB Tổng Hợp TP.HCM', 2018, 86000),
(2, N'Quẳng Gánh Lo Đi Và Vui Sống (How to Stop Worrying)', '978-604-58-1235-8', 1, 2, N'NXB Tổng Hợp TP.HCM', 2019, 88000),
(3, N'Nghệ Thuật Nói Trước Công Chúng (The Art of Public Speaking)', '978-604-58-1236-5', 1, 2, N'NXB Lao Động', 2020, 95000),
(4, N'Thu Phục Lòng Người Trong Kỷ Nguyên Số', '978-604-58-1237-2', 1, 2, N'NXB Trẻ', 2021, 110000),
(5, N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', '978-604-2-09876-5', 2, 1, N'NXB Trẻ', 2015, 125000),
(6, N'Mắt Biếc', '978-604-1-14567-3', 2, 1, N'NXB Trẻ', 2019, 110000),
(7, N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', '978-604-1-08912-4', 2, 1, N'NXB Trẻ', 2018, 90000),
(8, N'Cô Gái Đến Từ Hôm Qua', '978-604-1-07654-1', 2, 1, N'NXB Trẻ', 2017, 85000),
(9, N'Có Hai Con Mèo Ngồi Bên Cửa Sổ', '978-604-1-06543-2', 2, 1, N'NXB Trẻ', 2016, 78000),
(10, N'Chúc Một Ngày Tốt Lành', '978-604-1-05432-1', 2, 1, N'NXB Trẻ', 2014, 95000),
(11, N'Ngồi Khóc Trên Cây', '978-604-1-04321-9', 2, 1, N'NXB Trẻ', 2013, 105000),
(12, N'Đi Qua Hoa Cúc', '978-604-1-03210-8', 2, 1, N'NXB Trẻ', 2012, 70000),
(13, N'Clean Code: A Handbook of Agile Software Craftsmanship', '978-013-235088-4', 3, 3, N'Prentice Hall', 2008, 450000),
(14, N'Clean Architecture: A Craftsman Guide to Software Structure', '978-013-449416-6', 3, 3, N'Prentice Hall', 2017, 480000),
(15, N'The Clean Coder: A Code of Conduct for Programmers', '978-013-708107-3', 3, 3, N'Prentice Hall', 2011, 420000),
(16, N'Clean Craftsmanship: Disciplines, Standards, and Ethics', '978-013-691571-3', 3, 3, N'Addison-Wesley', 2021, 520000),
(17, N'Agile Software Development: Principles, Patterns, Practices', '978-013-597444-5', 3, 3, N'Prentice Hall', 2002, 550000),
(18, N'Nhà Giả Kim (The Alchemist)', '978-604-56-7890-2', 4, 1, N'NXB Hội Nhà Văn', 2017, 79000),
(19, N'Phù Thủy Phố Portobello', '978-604-56-7891-9', 4, 1, N'NXB Hội Nhà Văn', 2018, 92000),
(20, N'Bên Sông Piedra Tôi Ngồi Xuống Và Tôi Khóc', '978-604-56-7892-6', 4, 1, N'NXB Hội Nhà Văn', 2016, 75000),
(21, N'Quỷ Dữ Và Nàng Prym', '978-604-56-7893-3', 4, 1, N'NXB Hội Nhà Văn', 2015, 80000),
(22, N'Chí Phèo & Lão Hạc (Tuyển Tập Hiện Thực)', '978-604-55-1101-1', 5, 1, N'NXB Văn Học', 2019, 65000),
(23, N'Sống Mòn', '978-604-55-1102-8', 5, 1, N'NXB Văn Học', 2018, 70000),
(24, N'Đôi Mắt & Các Truyện Ngắn Chọn Lọc', '978-604-55-1103-5', 5, 1, N'NXB Kim Đồng', 2020, 58000),
(25, N'Số Đỏ (Kiệt Tác Văn Học Trào Phúng)', '978-604-55-2201-2', 6, 1, N'NXB Văn Học', 2017, 68000),
(26, N'Giông Tố', '978-604-55-2202-9', 6, 1, N'NXB Văn Học', 2018, 75000),
(27, N'Kỹ Nghệ Lấy Tây & Cơm Thầy Cơm Cô', '978-604-55-2203-6', 6, 1, N'NXB Văn Học', 2019, 80000),
(28, N'Dế Mèn Phiêu Lưu Ký (Ấn Bản Kỷ Niệm)', '978-604-2-15678-0', 7, 1, N'NXB Kim Đồng', 2020, 60000),
(29, N'Vợ Chồng A Phủ & Truyện Tây Bắc', '978-604-2-15679-7', 7, 1, N'NXB Kim Đồng', 2019, 55000),
(30, N'Cát Bụi Chân Ai', '978-604-56-3301-4', 7, 1, N'NXB Hội Nhà Văn', 2016, 85000),
(31, N'Gió Đầu Mùa', '978-604-55-4401-8', 8, 1, N'NXB Văn Học', 2018, 50000),
(32, N'Hà Nội Ba Mươi Sáu Phố Phường', '978-604-55-4402-5', 8, 1, N'NXB Văn Học', 2019, 55000),
(33, N'Nắng Trong Vườn & Sợi Tóc', '978-604-55-4403-2', 8, 1, N'NXB Văn Học', 2020, 52000),
(34, N'Anh Hùng Xạ Điêu (Trọn Bộ 4 Tập)', '978-604-68-5501-5', 9, 1, N'NXB Văn Học', 2018, 320000),
(35, N'Thần Điêu Đại Hiệp (Trọn Bộ 4 Tập)', '978-604-68-5502-2', 9, 1, N'NXB Văn Học', 2018, 340000),
(36, N'Ỷ Thiên Đồ Long Ký (Trọn Bộ 4 Tập)', '978-604-68-5503-9', 9, 1, N'NXB Văn Học', 2019, 330000),
(37, N'Tiếu Ngạo Giang Hồ (Trọn Bộ 4 Tập)', '978-604-68-5504-6', 9, 1, N'NXB Văn Học', 2019, 360000),
(38, N'Thiên Long Bát Bộ (Trọn Bộ 5 Tập)', '978-604-68-5505-3', 9, 1, N'NXB Văn Học', 2020, 420000),
(39, N'Rừng Na Uy (Norwegian Wood)', '978-604-56-6601-1', 10, 1, N'NXB Hội Nhà Văn', 2018, 115000),
(40, N'Kafka Bên Bờ Biển (Kafka on the Shore)', '978-604-56-6602-8', 10, 1, N'NXB Hội Nhà Văn', 2019, 145000),
(41, N'1Q84 (Trọn Bộ 3 Tập)', '978-604-56-6603-5', 10, 1, N'NXB Hội Nhà Văn', 2017, 280000),
(42, N'Biên Niên Sử Chim Vặn Dây Cót', '978-604-56-6604-2', 10, 1, N'NXB Hội Nhà Văn', 2016, 160000),
(43, N'Tôi Nói Gì Khi Nói Về Chạy Bộ', '978-604-56-6605-9', 10, 1, N'NXB Hội Nhà Văn', 2020, 85000),
(44, N'Phía Sau Nghi Can X (The Devotion of Suspect X)', '978-604-56-7701-3', 11, 1, N'NXB Hội Nhà Văn', 2018, 108000),
(45, N'Điều Kỳ Diệu Của Tiệm Tạp Hóa Namiya', '978-604-56-7702-0', 11, 1, N'NXB Hội Nhà Văn', 2019, 105000),
(46, N'Bạch Dạ Hành (Journey Under the Midnight Sun)', '978-604-56-7703-7', 11, 1, N'NXB Hội Nhà Văn', 2017, 150000),
(47, N'Bí Mật Của Naoko', '978-604-56-7704-4', 11, 1, N'NXB Hội Nhà Văn', 2016, 95000),
(48, N'Ác Ý (Malice)', '978-604-56-7705-1', 11, 1, N'NXB Hội Nhà Văn', 2020, 110000),
(49, N'Sherlock Holmes: Cuộc Phiêu Lưu Cuối Cùng', '978-604-55-8801-7', 12, 1, N'NXB Văn Học', 2018, 90000),
(50, N'Sherlock Holmes: Chiếc Nhẫn Tình Cờ & Dấu Bộ Tứ', '978-604-55-8802-4', 12, 1, N'NXB Văn Học', 2019, 95000),
(51, N'Sherlock Holmes: Con Chó Của Dòng Họ Baskerville', '978-604-55-8803-1', 12, 1, N'NXB Văn Học', 2020, 85000),
(52, N'Sherlock Holmes: Thung Lũng Khủng Khiếp', '978-604-55-8804-8', 12, 1, N'NXB Văn Học', 2017, 80000),
(53, N'Mười Người Da Đen Nhỏ (And Then There Were None)', '978-604-1-19901-0', 13, 1, N'NXB Trẻ', 2019, 98000),
(54, N'Án Mạng Trên Chuyến Tàu Tốc Hành Phương Đông', '978-604-1-19902-7', 13, 1, N'NXB Trẻ', 2020, 105000),
(55, N'Án Mạng Trên Sông Nile (Death on the Nile)', '978-604-1-19903-4', 13, 1, N'NXB Trẻ', 2021, 112000),
(56, N'Vụ Án Mạng Bí Ẩn Ở Styles', '978-604-1-19904-1', 13, 1, N'NXB Trẻ', 2018, 88000),
(57, N'1984 (Một Chín Tám Tư)', '978-604-56-1401-2', 14, 1, N'NXB Hội Nhà Văn', 2019, 95000),
(58, N'Chuyện Ở Nông Trại (Animal Farm)', '978-604-56-1402-9', 14, 1, N'NXB Hội Nhà Văn', 2020, 65000),
(59, N'Đường Về Wigan Pier', '978-604-56-1403-6', 14, 1, N'NXB Tri Thức', 2018, 85000),
(60, N'Những Người Khốn Khổ (Bộ 3 Tập)', '978-604-55-1501-6', 15, 1, N'NXB Văn Học', 2018, 270000),
(61, N'Nhà Thờ Đức Bà Paris', '978-604-55-1502-3', 15, 1, N'NXB Văn Học', 2019, 135000),
(62, N'Chín Mươi Ba (Ninety-Three)', '978-604-55-1503-0', 15, 1, N'NXB Văn Học', 2017, 110000),
(63, N'Ông Già Và Biển Cả (The Old Man and the Sea)', '978-604-55-1601-3', 16, 1, N'NXB Văn Học', 2019, 60000),
(64, N'Chuông Nguyện Hồn Ai (For Whom the Bell Tolls)', '978-604-55-1602-0', 16, 1, N'NXB Văn Học', 2018, 140000),
(65, N'Giã Từ Vũ Khí (A Farewell to Arms)', '978-604-55-1603-7', 16, 1, N'NXB Văn Học', 2017, 115000),
(66, N'Hóa Thân (The Metamorphosis)', '978-604-56-1701-4', 17, 1, N'NXB Hội Nhà Văn', 2020, 55000),
(67, N'Vụ Án (The Trial)', '978-604-56-1702-1', 17, 1, N'NXB Hội Nhà Văn', 2018, 90000),
(68, N'Lâu Đài (The Castle)', '978-604-56-1703-8', 17, 1, N'NXB Hội Nhà Văn', 2017, 110000),
(69, N'Trăm Năm Cô Đơn (One Hundred Years of Solitude)', '978-604-55-1801-0', 18, 1, N'NXB Văn Học', 2019, 140000),
(70, N'Tình Yêu Thời Thổ Tả (Love in the Time of Cholera)', '978-604-55-1802-7', 18, 1, N'NXB Văn Học', 2018, 135000),
(71, N'Ký Sự Về Một Cái Chết Được Báo Trước', '978-604-55-1803-4', 18, 1, N'NXB Văn Học', 2017, 75000),
(72, N'Sapiens: Lược Sử Loài Người', '978-604-1-19011-8', 19, 2, N'NXB Thế Giới', 2018, 195000),
(73, N'Homo Deus: Lược Sử Tương Lai', '978-604-1-19012-5', 19, 2, N'NXB Thế Giới', 2019, 205000),
(74, N'21 Bài Học Cho Thế Kỷ 21', '978-604-1-19013-2', 19, 2, N'NXB Thế Giới', 2020, 185000),
(75, N'Tư Duy Nhanh Và Chậm (Thinking, Fast and Slow)', '978-604-77-2001-9', 20, 2, N'NXB Thế Giới', 2019, 210000),
(76, N'Nhiễu: Điểm Mù Trong Phán Đoán Của Con Người', '978-604-77-2002-6', 20, 2, N'NXB Thế Giới', 2021, 225000),
(77, N'Thói Quen Nguyên Tử (Atomic Habits)', '978-604-77-2101-6', 21, 2, N'NXB Thế Giới', 2019, 189000),
(78, N'Tâm Lý Học Về Tiền (The Psychology of Money)', '978-604-77-2201-3', 22, 4, N'NXB Trẻ', 2021, 160000),
(79, N'Như Chưa Từng Có Cuộc Đổi Thay (Same as Ever)', '978-604-77-2202-0', 22, 4, N'NXB Trẻ', 2023, 175000),
(80, N'Dạy Con Làm Giàu (Tập 1: Để Không Có Tiền Vẫn Tạo Ra Tiền)', '978-604-1-23011-2', 23, 4, N'NXB Trẻ', 2019, 95000),
(81, N'Dạy Con Làm Giàu (Tập 2: Sử Dụng Đồng Vốn)', '978-604-1-23012-9', 23, 4, N'NXB Trẻ', 2019, 105000),
(82, N'Dạy Con Làm Giàu (Tập 3: Hướng Dẫn Đầu Tư)', '978-604-1-23013-6', 23, 4, N'NXB Trẻ', 2019, 115000),
(83, N'Doanh Nghiệp Của Thế Kỷ 21', '978-604-1-23014-3', 23, 4, N'NXB Trẻ', 2020, 85000),
(84, N'Nghĩ Giàu Và Làm Giàu (Think and Grow Rich)', '978-604-58-2401-4', 24, 2, N'NXB Tổng Hợp TP.HCM', 2018, 98000),
(85, N'Chiến Thắng Con Quỷ Trong Bạn (Outwitting the Devil)', '978-604-58-2402-1', 24, 2, N'NXB Lao Động', 2019, 110000),
(86, N'7 Thói Quen Của Người Thành Đạt (The 7 Habits)', '978-604-58-2501-1', 25, 2, N'NXB Tổng Hợp TP.HCM', 2018, 165000),
(87, N'Thói Quen Thứ 8 (The 8th Habit)', '978-604-58-2502-8', 25, 2, N'NXB Tổng Hợp TP.HCM', 2019, 175000),
(88, N'Tốc Độ Của Niềm Tin (The Speed of Trust)', '978-604-58-2503-5', 25, 2, N'NXB Tổng Hợp TP.HCM', 2020, 145000),
(89, N'Hãy Ăn Con Ếch Đó! (Eat That Frog!)', '978-604-58-2601-8', 26, 2, N'NXB Lao Động', 2019, 75000),
(90, N'Thuật Quản Trị Thời Gian', '978-604-58-2602-5', 26, 2, N'NXB Lao Động', 2020, 68000),
(91, N'Tâm Lý Học Bán Hàng (The Psychology of Selling)', '978-604-58-2603-2', 26, 2, N'NXB Lao Động', 2018, 98000),
(92, N'Tiểu Sử Steve Jobs', '978-604-1-27011-7', 27, 1, N'NXB Trẻ', 2019, 240000),
(93, N'Tiểu Sử Elon Musk', '978-604-1-27012-4', 27, 1, N'NXB Trẻ', 2023, 290000),
(94, N'Leonardo da Vinci (Thiên Tài Toàn Năng)', '978-604-1-27013-1', 27, 1, N'NXB Thế Giới', 2020, 260000),
(95, N'Tiểu Sử Einstein: Cuộc Đời Và Vũ Trụ', '978-604-1-27014-8', 27, 1, N'NXB Thế Giới', 2018, 220000),
(96, N'Refactoring: Improving the Design of Existing Code', '978-013-475759-9', 28, 3, N'Addison-Wesley', 2018, 490000),
(97, N'Patterns of Enterprise Application Architecture', '978-032-112742-6', 28, 3, N'Addison-Wesley', 2002, 520000),
(98, N'Effective Java (3rd Edition)', '978-013-468599-1', 29, 3, N'Addison-Wesley', 2017, 460000),
(99, N'Java Puzzlers: Traps, Pitfalls, and Corner Cases', '978-032-133678-1', 29, 3, N'Addison-Wesley', 2005, 390000),
(100, N'Design Patterns: Elements of Reusable Object-Oriented Software', '978-020-163361-0', 30, 3, N'Addison-Wesley', 1994, 510000),
(101, N'The Art of Computer Programming, Vol 1: Fundamental Algorithms', '978-020-189683-1', 31, 3, N'Addison-Wesley', 1997, 650000),
(102, N'The Art of Computer Programming, Vol 3: Sorting and Searching', '978-020-189685-5', 31, 3, N'Addison-Wesley', 1998, 680000),
(103, N'Computer Networks (Mạng Máy Tính)', '978-013-212695-3', 32, 3, N'Pearson', 2010, 480000),
(104, N'Modern Operating Systems (Các Hệ Điều Hành Hiện Đại)', '978-013-359162-0', 32, 3, N'Pearson', 2014, 520000),
(105, N'Introduction to Algorithms (CLRS 4th Edition)', '978-026-204630-5', 33, 3, N'MIT Press', 2022, 750000),
(106, N'Khởi Nghiệp Tinh Gọn (The Lean Startup)', '978-604-1-34011-5', 34, 4, N'NXB Trẻ', 2018, 145000),
(107, N'Con Đường Tinh Gọn (The Startup Way)', '978-604-1-34012-2', 34, 4, N'NXB Trẻ', 2019, 165000),
(108, N'Deep Work: Làm Ra Làm, Chơi Ra Chơi', '978-604-77-3501-3', 35, 2, N'NXB Thế Giới', 2018, 135000),
(109, N'Digital Minimalism: Tối Giản Thời Đại Số', '978-604-77-3502-0', 35, 2, N'NXB Thế Giới', 2019, 140000),
(110, N'So Good They Can Not Ignore You', '978-604-77-3503-7', 35, 2, N'NXB Thế Giới', 2017, 125000);
SET IDENTITY_INSERT books OFF;
GO

-- Chèn book_items mẫu
SET IDENTITY_INSERT book_items ON;
INSERT INTO book_items (id, book_id, branch_id, sku_barcode, condition_grade, selling_price, stock_quantity, shelf_location, import_date, status) VALUES
(1, 1, 1, 'Q1-DNT-90-001', 'VERY_GOOD', 55000, 2, N'Kệ KNS-01-T2', '2026-01-15', 'AVAILABLE'),
(2, 1, 2, 'TD-DNT-80-002', 'GOOD', 45000, 1, N'Kệ KNS-03-T1', '2026-08-01', 'AVAILABLE'),
(3, 5, 1, 'Q1-HVTCX-99-001', 'LIKE_NEW', 95000, 1, N'Kệ VH-02-T3', '2025-05-10', 'AVAILABLE'),
(4, 5, 3, 'BT-HVTCX-60-001', 'ACCEPTABLE', 40000, 3, N'Kệ VH-05-T1', '2025-11-20', 'AVAILABLE'),
(5, 13, 2, 'TD-CLNC-90-001', 'VERY_GOOD', 290000, 1, N'Kệ IT-01-T2', '2026-07-10', 'AVAILABLE'),
(6, 18, 1, 'Q1-NGK-80-001', 'GOOD', 48000, 1, N'Kệ VH-01-T1', '2026-02-14', 'AVAILABLE'),
(7, 39, 3, 'BT-RNU-90-001', 'VERY_GOOD', 75000, 2, N'Kệ VH-03-T2', '2026-03-01', 'AVAILABLE'),
(8, 72, 1, 'Q1-SAP-99-001', 'LIKE_NEW', 145000, 1, N'Kệ KNS-05-T3', '2026-01-20', 'AVAILABLE'),
(9, 78, 2, 'TD-TLHT-90-001', 'VERY_GOOD', 115000, 2, N'Kệ KT-01-T1', '2026-06-15', 'AVAILABLE'),
(10, 108, 3, 'BT-DPW-99-001', 'LIKE_NEW', 98000, 1, N'Kệ KNS-04-T2', '2026-04-10', 'AVAILABLE');
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

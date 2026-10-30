-- ============================================================
-- BOOK4LIFE AIVEN CLOUD MYSQL BACKUP DATA
-- Exported at: Fri Oct 30 06:18:24 GMT+07:00 2026
-- Host: mysql-bookstore-dinhsy06112006-0dc4.b.aivencloud.com:26077
-- Database: defaultdb
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- Structure and Data for table `ai_chat_messages`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `ai_chat_messages`;
CREATE TABLE "ai_chat_messages" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "content" text NOT NULL,
  "created_at" datetime(6) NOT NULL,
  "sender_type" varchar(20) NOT NULL,
  "session_id" varchar(100) NOT NULL,
  "user_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "FKomymgkwjebej7rs8ne14ct5wq" ("user_id"),
  CONSTRAINT "FKomymgkwjebej7rs8ne14ct5wq" FOREIGN KEY ("user_id") REFERENCES "users" ("id")
);

INSERT INTO `ai_chat_messages` VALUES 
(11, 'Làm thế nào để liên hệ trực tiếp nhân viên hỗ trợ hoặc hotline cửa hàng?', '2026-10-29 19:48:47.049221', 'USER', 'session-1792647741070-qfvyeyq', 14),
(12, '📞 **Kênh Hỗ Trợ Trực Tiếp Khách Hàng Book4Life:**\n\n• **Hotline tư vấn nhanh:** **1900 6868** (Nhánh 1: Mua hàng, Nhánh 2: Thu mua sách cũ)\n• **Email hỗ trợ:** admin.book4life@gmail.com\n• **Thời gian tiếp nhận:** 08:00 – 21:30 hàng ngày.\n\nNgoài ra, bạn cũng có thể gõ trực tiếp câu hỏi vào ô chat này, hệ thống sẽ tự động gửi email chuyển tiếp nội dung đến ban quản trị để hỗ trợ bạn qua email sớm nhất nhé!', '2026-10-29 19:48:47.095130', 'ASSISTANT', 'session-1792647741070-qfvyeyq', 14),
(13, 'Chính sách miễn phí vận chuyển (Freeship) và mã giảm giá áp dụng ra sao?', '2026-10-29 19:49:00.095206', 'USER', 'session-1792647741070-qfvyeyq', 14),
(14, '🎁 **Chương Trình Khuyến Mãi & Coupon Ưu Đãi:**\n\n• **Freeship toàn quốc:** Tự động áp dụng phí vận chuyển 0đ cho đơn hàng có giá trị từ **250.000đ** trở lên.\n• **Tặng Coupon 15%:** Dành cho khách hàng lần đầu hoàn thành bán sách cũ (trade-in).\n• **Cách nhập mã giảm giá:** Truy cập trang **[Giỏ Hàng](/cart)**, điền mã coupon vào ô *Mã Giảm Giá* rồi bấm **Áp Dụng** trước khi xác nhận đơn hàng.', '2026-10-29 19:49:00.138694', 'ASSISTANT', 'session-1792647741070-qfvyeyq', 14);

-- ------------------------------------------------------------
-- Structure and Data for table `authors`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `authors`;
CREATE TABLE "authors" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "name" varchar(150) NOT NULL,
  "biography" text,
  "avatar_url" varchar(500) DEFAULT NULL,
  PRIMARY KEY ("id")
);

INSERT INTO `authors` VALUES 
(1, 'Nam Cao', 'Nhà văn hiện thực xuất sắc của văn học Việt Nam.', NULL),
(2, 'Vũ Trọng Phụng', 'Ông vua phóng sự đất Bắc thời kỳ 1930 - 1945.', NULL),
(3, 'Nguyễn Du', 'Đại thi hào dân tộc Việt Nam, danh nhân văn hóa thế giới.', NULL),
(4, 'Tô Hoài', 'Cây đại thụ của nền văn học thiếu nhi hiện đại Việt Nam.', NULL),
(5, 'Antoine de Saint-Exupéry', 'Nhà văn, phi công tiên phong người Pháp, tác giả Hoàng Tử Bé.', NULL),
(6, 'Paulo Coelho', 'Tiểu thuyết gia người Brazil, tác giả kiệt tác Nhà Giả Kim.', NULL),
(7, 'Dale Carnegie', 'Bậc thầy phát triển bản thân, tác giả Đắc Nhân Tâm.', NULL);

-- ------------------------------------------------------------
-- Structure and Data for table `book_item_images`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `book_item_images`;
CREATE TABLE "book_item_images" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "cloudinary_public_id" varchar(255) NOT NULL,
  "image_url" varchar(500) NOT NULL,
  "is_primary" bit(1) NOT NULL,
  "book_item_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  KEY "FKdfqihor39reb2mungj6jgha79" ("book_item_id"),
  CONSTRAINT "FKdfqihor39reb2mungj6jgha79" FOREIGN KEY ("book_item_id") REFERENCES "book_items" ("id")
);


-- ------------------------------------------------------------
-- Structure and Data for table `book_items`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `book_items`;
CREATE TABLE "book_items" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "condition_grade" varchar(30) NOT NULL,
  "condition_note" varchar(500) DEFAULT NULL,
  "import_date" datetime(6) NOT NULL,
  "selling_price" decimal(12,2) NOT NULL,
  "shelf_location" varchar(100) DEFAULT NULL,
  "sku_barcode" varchar(50) NOT NULL,
  "status" varchar(20) NOT NULL,
  "stock_quantity" int NOT NULL,
  "book_id" bigint NOT NULL,
  "branch_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UKkeiy3yja6sjrj4n4pbqui4xk7" ("sku_barcode"),
  KEY "FKcucaiijcpiw2eb64bleox0ivq" ("branch_id"),
  KEY "FKpe3ct8txpojojbixpksxisrhn" ("book_id"),
  CONSTRAINT "FKcucaiijcpiw2eb64bleox0ivq" FOREIGN KEY ("branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKpe3ct8txpojojbixpksxisrhn" FOREIGN KEY ("book_id") REFERENCES "books" ("id")
);

INSERT INTO `book_items` VALUES 
(1, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-01', 'SKU-B1-1001', 'SOLD', 0, 1, 1),
(2, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-01', 'SKU-B2-2001', 'SOLD', 0, 1, 2),
(3, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-02', 'SKU-B1-1002', 'AVAILABLE', 4, 2, 1),
(4, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-02', 'SKU-B2-2002', 'AVAILABLE', 2, 2, 2),
(5, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-03', 'SKU-B1-1003', 'AVAILABLE', 5, 3, 1),
(6, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-03', 'SKU-B2-2003', 'SOLD', 0, 3, 2),
(7, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-04', 'SKU-B1-1004', 'AVAILABLE', 4, 4, 1),
(8, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-04', 'SKU-B2-2004', 'SOLD', 0, 4, 2),
(9, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-05', 'SKU-B1-1005', 'AVAILABLE', 5, 5, 1),
(10, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-05', 'SKU-B2-2005', 'AVAILABLE', 3, 5, 2),
(11, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-06', 'SKU-B1-1006', 'SOLD', 0, 6, 1),
(12, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-06', 'SKU-B2-2006', 'AVAILABLE', 3, 6, 2),
(13, 'LIKE_NEW', 'Sách tuyển chọn 95%', '2026-10-22 06:23:21', 65000.00, 'Kệ A1-07', 'SKU-B1-1007', 'AVAILABLE', 5, 7, 1),
(14, 'VERY_GOOD', 'Sách còn tốt 85%', '2026-10-22 06:23:21', 55000.00, 'Kệ B2-07', 'SKU-B2-2007', 'AVAILABLE', 2, 7, 2);

-- ------------------------------------------------------------
-- Structure and Data for table `books`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `books`;
CREATE TABLE "books" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "title" varchar(255) NOT NULL,
  "isbn" varchar(20) NOT NULL,
  "author_id" bigint NOT NULL,
  "category_id" bigint NOT NULL,
  "publisher" varchar(150) DEFAULT NULL,
  "publish_year" int DEFAULT NULL,
  "original_price" decimal(12,2) NOT NULL,
  "cover_image_url" varchar(500) DEFAULT NULL,
  "description" text,
  PRIMARY KEY ("id"),
  UNIQUE KEY "isbn" ("isbn"),
  KEY "fk_book_author" ("author_id"),
  KEY "fk_book_category" ("category_id"),
  CONSTRAINT "fk_book_author" FOREIGN KEY ("author_id") REFERENCES "authors" ("id"),
  CONSTRAINT "fk_book_category" FOREIGN KEY ("category_id") REFERENCES "categories" ("id")
);

INSERT INTO `books` VALUES 
(1, 'Chí Phèo', '978-604-1-12345-1', 1, 1, 'NXB Văn Học', 1941, 85000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792764187/bookstore/covers/c65st5fxqgqiavvzzltc.jpg', 'Tuyệt phẩm văn học hiện thực phê phán kinh điển của Nam Cao.'),
(2, 'Số Đỏ', '978-604-1-12345-2', 2, 1, 'NXB Văn Học', 1936, 95000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792765103/bookstore/covers/psernsowwmlz3racoech.jpg', 'Kiệt tác châm biếm kinh điển của văn học Việt Nam thời kỳ tiền chiến.'),
(3, 'Truyện Kiều', '978-604-1-12345-3', 3, 1, 'NXB Văn Học', 1820, 120000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792764146/bookstore/covers/u2qeeu5glsvq7jqox4vy.jpg', 'Đỉnh cao thi ca văn học cổ điển Việt Nam.'),
(4, 'Dế Mèn Phiêu Lưu Ký', '978-604-1-12345-4', 4, 1, 'NXB Kim Đồng', 1941, 65000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792764746/bookstore/covers/xjmhj5sbqjjuwvwb0t6r.jpg', 'Tác phẩm thiếu nhi kinh điển gắn liền với tuổi thơ nhiều thế hệ.'),
(5, 'Hoàng Tử Bé (The Little Prince)', '978-604-1-12345-5', 5, 1, 'NXB Hội Nhà Văn', 1943, 75000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792765003/bookstore/covers/ki6hd5nzsc8fk8ltr26t.jpg', 'Cuốn sách kinh điển thế giới về tình bạn và triết lý sống.'),
(6, 'Nhà Giả Kim (The Alchemist)', '978-604-1-12345-6', 6, 1, 'NXB Nhã Nam', 1988, 89000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792764885/bookstore/covers/qkc7rlxbx6yefs13rgzg.jpg', 'Tác phẩm truyền cảm hứng kinh điển dịch ra hơn 80 thứ tiếng trên toàn thế giới.'),
(7, 'Đắc Nhân Tâm', '978-604-1-12345-7', 7, 4, 'NXB First News', 1936, 110000.00, 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1792763956/bookstore/covers/mtd4ac0xzqzzf2tlqxyv.jpg', 'Cuốn sách kỹ năng kinh điển bán chạy nhất mọi thời đại.');

-- ------------------------------------------------------------
-- Structure and Data for table `branches`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `branches`;
CREATE TABLE "branches" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "address" varchar(255) NOT NULL,
  "branch_name" varchar(150) NOT NULL,
  "city" varchar(100) NOT NULL,
  "is_active" bit(1) NOT NULL,
  "latitude" decimal(10,8) DEFAULT NULL,
  "longitude" decimal(11,8) DEFAULT NULL,
  "phone" varchar(20) NOT NULL,
  PRIMARY KEY ("id")
);

INSERT INTO `branches` VALUES 
(1, '123 Nguyễn Thị Minh Khai, P. Bến Thành, Q.1', 'Book4Life Quận 1', 'TP. Hồ Chí Minh', true, 10.77190000, 106.69830000, '028.3912.8888'),
(2, '456 Võ Văn Ngân, TP. Thủ Đức', 'Book4Life Thủ Đức', 'TP. Hồ Chí Minh', true, 10.85040000, 106.77180000, '028.3896.7777'),
(3, '78 Tràng Tiền, Q. Hoàn Kiếm', 'Book4Life Hoàn Kiếm', 'Hà Nội', true, 21.02850000, 105.85420000, '024.3934.6666');

-- ------------------------------------------------------------
-- Structure and Data for table `categories`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE "categories" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "name" varchar(100) NOT NULL,
  "parent_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UKt8o6pivur7nn124jehx7cygw5" ("name"),
  KEY "FKsaok720gsu4u2wrgbk10b5n8d" ("parent_id"),
  CONSTRAINT "FKsaok720gsu4u2wrgbk10b5n8d" FOREIGN KEY ("parent_id") REFERENCES "categories" ("id")
);

INSERT INTO `categories` VALUES 
(1, 'Kinh Điển', NULL),
(2, 'Văn Học', NULL),
(3, 'Kinh Tế & Quản Trị', NULL),
(4, 'Kỹ Năng Sống', NULL),
(5, 'Lịch Sử & Văn Hóa', NULL),
(6, 'Khoa Học & Triết Học', NULL);

-- ------------------------------------------------------------
-- Structure and Data for table `coupon_usages`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `coupon_usages`;
CREATE TABLE "coupon_usages" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "used_at" datetime(6) NOT NULL,
  "coupon_id" bigint NOT NULL,
  "master_order_id" bigint NOT NULL,
  "user_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  KEY "FK3mvslb8gc0ac6501mfmvifgva" ("coupon_id"),
  KEY "FKg1vne7r7k0y2cqnb60g2x2fas" ("master_order_id"),
  KEY "FK6mev6grxbqmt8l0jxvobfg70n" ("user_id"),
  CONSTRAINT "FK3mvslb8gc0ac6501mfmvifgva" FOREIGN KEY ("coupon_id") REFERENCES "coupons" ("id"),
  CONSTRAINT "FK6mev6grxbqmt8l0jxvobfg70n" FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
  CONSTRAINT "FKg1vne7r7k0y2cqnb60g2x2fas" FOREIGN KEY ("master_order_id") REFERENCES "master_orders" ("id")
);


-- ------------------------------------------------------------
-- Structure and Data for table `coupons`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE "coupons" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "applicable_condition_grade" varchar(30) DEFAULT NULL,
  "code" varchar(50) NOT NULL,
  "discount_type" varchar(20) NOT NULL,
  "discount_value" decimal(12,2) DEFAULT NULL,
  "end_date" datetime(6) DEFAULT NULL,
  "is_active" bit(1) DEFAULT NULL,
  "max_discount_amount" decimal(12,2) DEFAULT NULL,
  "min_order_amount" decimal(12,2) DEFAULT NULL,
  "start_date" datetime(6) DEFAULT NULL,
  "usage_limit" int DEFAULT NULL,
  "used_count" int DEFAULT NULL,
  "applicable_branch_id" bigint DEFAULT NULL,
  "coupon_category" varchar(30) DEFAULT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UKeplt0kkm9yf2of2lnx6c1oy9b" ("code"),
  KEY "FK30840mefg5lehbpoqwnuk1r5j" ("applicable_branch_id"),
  CONSTRAINT "FK30840mefg5lehbpoqwnuk1r5j" FOREIGN KEY ("applicable_branch_id") REFERENCES "branches" ("id")
);

INSERT INTO `coupons` VALUES 
(1, NULL, 'FREESHIP', 'FREESHIP', 25000.00, '2027-04-24 18:50:43.093262', true, 25000.00, 0.00, '2026-10-23 18:50:43.093262', 500, 0, NULL, 'SHIPPING'),
(2, NULL, 'FREESHIPMAX', 'FREESHIP', 25000.00, '2027-04-24 18:50:43.093262', true, 25000.00, 50000.00, '2026-10-23 18:50:43.093262', 1000, 0, NULL, 'SHIPPING'),
(3, NULL, 'SHIP10K', 'SHIP_DISCOUNT', 10000.00, '2027-04-24 18:50:43.093262', true, 10000.00, 50000.00, '2026-10-23 18:50:43.093262', 1000, 0, NULL, 'SHIPPING'),
(4, NULL, 'BOOK10', 'PERCENT', 10.00, '2027-04-24 18:50:43.093262', true, 50000.00, 100000.00, '2026-10-23 18:50:43.093262', 500, 2, NULL, 'ORDER_DISCOUNT'),
(5, NULL, 'TIETKIEM', 'FIXED_AMOUNT', 20000.00, '2027-04-24 18:50:43.093262', true, 20000.00, 150000.00, '2026-10-23 18:50:43.093262', 500, 0, NULL, 'ORDER_DISCOUNT'),
(7, NULL, 'WEBNAYDEP', 'PERCENT', 90.00, '2026-11-01 00:00:00', true, NULL, 0.00, '2026-10-29 00:00:00', 100, 0, NULL, 'ORDER_DISCOUNT');

-- ------------------------------------------------------------
-- Structure and Data for table `drivers`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `drivers`;
CREATE TABLE "drivers" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "active_orders_count" int DEFAULT NULL,
  "carrier_name" varchar(100) DEFAULT NULL,
  "created_at" datetime(6) DEFAULT NULL,
  "full_name" varchar(100) NOT NULL,
  "license_plate" varchar(30) DEFAULT NULL,
  "phone" varchar(20) NOT NULL,
  "rating" double DEFAULT NULL,
  "status" varchar(20) NOT NULL,
  "vehicle_type" varchar(30) DEFAULT NULL,
  "branch_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "FKc4072clkxn890ovjr4am19eql" ("branch_id"),
  CONSTRAINT "FKc4072clkxn890ovjr4am19eql" FOREIGN KEY ("branch_id") REFERENCES "branches" ("id")
);

INSERT INTO `drivers` VALUES 
(1, 0, 'Đội xe Book4Life HN', '2026-10-24 18:50:41.174860', 'Nguyễn Văn Tuấn', '29B1-686.88', '0987.111.222', 4.9, 'AVAILABLE', 'MOTORBIKE', 3),
(2, 1, 'Đội xe Book4Life Q1', '2026-10-24 18:50:41.252528', 'Trần Quốc Bảo', '59P2-777.99', '0903.222.333', 5.0, 'AVAILABLE', 'MOTORBIKE', 1),
(3, 0, 'Đội xe Book4Life Thủ Đức', '2026-10-24 18:50:41.252550', 'Lê Hữu Thắng', '59X3-555.22', '0912.333.444', 4.8, 'AVAILABLE', 'MOTORBIKE', 2),
(4, 0, 'Book4Life Fast Truck', '2026-10-24 18:50:41.252568', 'Bùi Minh Quân (Xe Tải)', '51D-888.33', '0938.444.555', 5.0, 'AVAILABLE', 'TRUCK', 1),
(5, 0, 'Giao Hàng Tiết Kiệm', '2026-10-24 18:50:41.252582', 'Shipper Đối Tác GHTK', 'GHTK-EXPRESS', '1900.6092', 4.7, 'AVAILABLE', 'MOTORBIKE', NULL),
(6, 0, 'Giao Hàng Nhanh', '2026-10-24 18:50:41.252600', 'Shipper Đối Tác GHN', 'GHN-FAST', '1900.636677', 4.8, 'AVAILABLE', 'MOTORBIKE', NULL);

-- ------------------------------------------------------------
-- Structure and Data for table `master_orders`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `master_orders`;
CREATE TABLE "master_orders" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "created_at" datetime(6) DEFAULT NULL,
  "discount_amount" decimal(38,2) DEFAULT NULL,
  "final_amount" decimal(38,2) DEFAULT NULL,
  "master_order_code" varchar(255) DEFAULT NULL,
  "payment_method" varchar(255) DEFAULT NULL,
  "payment_status" varchar(255) DEFAULT NULL,
  "receiver_name" varchar(255) DEFAULT NULL,
  "receiver_phone" varchar(255) DEFAULT NULL,
  "shipping_address" varchar(255) DEFAULT NULL,
  "total_items_amount" decimal(38,2) DEFAULT NULL,
  "total_shipping_fee" decimal(38,2) DEFAULT NULL,
  "coupon_id" bigint DEFAULT NULL,
  "user_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UK8tavk97h0u3ltnxjlqokrwn3r" ("master_order_code"),
  KEY "FKeyw2c4rqslluhkdyqhc3tp5oy" ("coupon_id"),
  KEY "FK6n3ht2bxcdvvfp6953y8rjs2l" ("user_id"),
  CONSTRAINT "FK6n3ht2bxcdvvfp6953y8rjs2l" FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
  CONSTRAINT "FKeyw2c4rqslluhkdyqhc3tp5oy" FOREIGN KEY ("coupon_id") REFERENCES "coupons" ("id")
);

INSERT INTO `master_orders` VALUES 
(8, '2026-10-29 19:34:59.545083', 31500.00, 148500.00, 'MO-1793302499493', 'COD', 'PAID', 'Phú Sỹ', '0346033591', 'Trường ĐH Công nghệ Kỹ Thuật TP. HCM, Phường Linh Chiểu, Thành phố Thủ Đức, Thành phố Hồ Chí Minh', 165000.00, 0.00, 4, 14),
(9, '2026-10-29 19:47:14.424245', 0.00, 129000.00, 'MO-1793303234423', 'COD', 'PENDING', 'Phú Sỹ', '0346033591', 'Trường ĐH Công nghệ Kỹ Thuật TP. HCM, Phường Linh Chiểu, Thành phố Thủ Đức, Thành phố Hồ Chí Minh', 120000.00, 9000.00, NULL, 14),
(10, '2026-10-29 20:07:01.937468', 27000.00, 108000.00, 'MO-1793304421893', 'COD', 'PENDING', 'Phú Sỹ', '0346033591', 'Trường ĐH Công nghệ Kỹ Thuật TP. HCM, Phường Linh Chiểu, Thành phố Thủ Đức, Thành phố Hồ Chí Minh', 120000.00, 0.00, 4, 16),
(11, '2026-10-29 20:11:10.564534', 0.00, 110000.00, 'POS-1793304670563', 'CASH', 'PAID', 'Khách lẻ tại quầy', 'Tại quầy', 'Bán trực tiếp tại: Book4Life Thủ Đức', 110000.00, 0.00, NULL, 4);

-- ------------------------------------------------------------
-- Structure and Data for table `order_status_history`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_status_history`;
CREATE TABLE "order_status_history" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "action_type" varchar(50) NOT NULL,
  "created_at" datetime(6) NOT NULL,
  "from_status" varchar(50) DEFAULT NULL,
  "note" varchar(500) DEFAULT NULL,
  "to_status" varchar(50) NOT NULL,
  "from_branch_id" bigint DEFAULT NULL,
  "master_order_id" bigint NOT NULL,
  "sub_order_id" bigint NOT NULL,
  "to_branch_id" bigint DEFAULT NULL,
  "updated_by_user_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "FKeag7h6s90ldolvwtjpiuhs9ic" ("from_branch_id"),
  KEY "FKf5pcxaj7sv6wqrr47y4k6ypmq" ("master_order_id"),
  KEY "FKlvph9yup76hdm2orsypkvkyyc" ("sub_order_id"),
  KEY "FKl2xs76ej0ow3p658wlb2s0dmb" ("to_branch_id"),
  KEY "FKb6lfo1jq1cwf20brgat7usga7" ("updated_by_user_id"),
  CONSTRAINT "FKb6lfo1jq1cwf20brgat7usga7" FOREIGN KEY ("updated_by_user_id") REFERENCES "users" ("id"),
  CONSTRAINT "FKeag7h6s90ldolvwtjpiuhs9ic" FOREIGN KEY ("from_branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKf5pcxaj7sv6wqrr47y4k6ypmq" FOREIGN KEY ("master_order_id") REFERENCES "master_orders" ("id"),
  CONSTRAINT "FKl2xs76ej0ow3p658wlb2s0dmb" FOREIGN KEY ("to_branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKlvph9yup76hdm2orsypkvkyyc" FOREIGN KEY ("sub_order_id") REFERENCES "sub_orders" ("id")
);

INSERT INTO `order_status_history` VALUES 
(17, 'STATUS_CHANGE', '2026-10-29 19:34:59.796929', NULL, 'Khách hàng đã đặt đơn thành công qua hệ thống Book4Life (PTTT: COD)', 'PENDING', NULL, 8, 8, 2, 14),
(18, 'STATUS_CHANGE', '2026-10-29 19:36:30.326912', 'PENDING', 'Nhân viên/Quản lý phê duyệt đơn hàng', 'CONFIRMED', 2, 8, 8, 2, 4),
(19, 'STATUS_CHANGE', '2026-10-29 19:36:55.647417', 'CONFIRMED', 'Thủ kho bắt đầu nhặt sách trên kệ và đóng gói', 'PACKING', 2, 8, 8, 2, 4),
(22, 'STATUS_CHANGE', '2026-10-29 19:37:22.370454', 'PACKING', 'Đã phân công tài xế: Shipper Đối Tác GHN (1900.636677) - Đơn vị: Giao Hàng Nhanh', 'SHIPPED', 2, 8, 8, 2, 4),
(23, 'CUSTOMER_CONFIRMED_DELIVERY', '2026-10-29 19:38:12.177507', 'SHIPPED', 'Khách hàng xác nhận đã nhận hàng thành công. Đánh giá: 5/5 sao ⭐. Phản hồi: "Đóng gói cẩn thận 2 lớp, shop giao hàng siêu iuuu"', 'COMPLETED', 2, 8, 8, 2, 14),
(24, 'STATUS_CHANGE', '2026-10-29 19:47:14.641772', NULL, 'Khách hàng đã đặt đơn thành công qua hệ thống Book4Life (PTTT: COD)', 'PENDING', NULL, 9, 9, 1, 14),
(25, 'STATUS_CHANGE', '2026-10-29 19:47:14.685344', NULL, 'Khách hàng đã đặt đơn thành công qua hệ thống Book4Life (PTTT: COD)', 'PENDING', NULL, 9, 10, 2, 14),
(26, 'STATUS_CHANGE', '2026-10-29 19:47:43.072325', 'PENDING', 'Nhân viên/Quản lý phê duyệt đơn hàng', 'CONFIRMED', 2, 9, 10, 2, 4),
(27, 'STATUS_CHANGE', '2026-10-29 20:07:02.157038', NULL, 'Khách hàng đã đặt đơn thành công qua hệ thống Book4Life (PTTT: COD)', 'PENDING', NULL, 10, 11, 1, 16),
(28, 'STATUS_CHANGE', '2026-10-29 20:07:02.212604', NULL, 'Khách hàng đã đặt đơn thành công qua hệ thống Book4Life (PTTT: COD)', 'PENDING', NULL, 10, 12, 2, 16),
(29, 'POS_SALE', '2026-10-29 20:11:10.735136', NULL, 'Đơn hàng bán lẻ trực tiếp tại quầy POS đã thanh toán thành công', 'COMPLETED', NULL, 11, 13, 2, 4),
(30, 'STATUS_CHANGE', '2026-10-29 20:12:50.167434', 'PENDING', 'Nhân viên/Quản lý phê duyệt đơn hàng', 'CONFIRMED', 1, 10, 11, 1, 3),
(31, 'STATUS_CHANGE', '2026-10-29 20:12:56.140274', 'PENDING', 'Nhân viên/Quản lý phê duyệt đơn hàng', 'CONFIRMED', 1, 9, 9, 1, 3),
(32, 'STATUS_CHANGE', '2026-10-29 20:13:15.527979', 'CONFIRMED', 'Thủ kho bắt đầu nhặt sách đóng gói', 'PACKING', 1, 10, 11, 1, 3),
(34, 'STATUS_CHANGE', '2026-10-29 20:13:32.678101', 'PACKING', 'Đã phân công tài xế: Trần Quốc Bảo (0903.222.333) - Đơn vị: Đội xe Book4Life Q1', 'SHIPPED', 1, 10, 11, 1, 3);

-- ------------------------------------------------------------
-- Structure and Data for table `otp_verifications`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `otp_verifications`;
CREATE TABLE "otp_verifications" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "attempts" int NOT NULL,
  "created_at" datetime(6) NOT NULL,
  "email" varchar(150) NOT NULL,
  "expires_at" datetime(6) NOT NULL,
  "full_name" varchar(100) DEFAULT NULL,
  "is_used" bit(1) NOT NULL,
  "otp_code" varchar(10) NOT NULL,
  "password_hash" varchar(255) NOT NULL,
  "phone" varchar(20) DEFAULT NULL,
  "otp_type" varchar(50) DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "idx_otp_email_created" ("email","created_at"),
  KEY "idx_otp_expires" ("expires_at")
);

INSERT INTO `otp_verifications` VALUES 
(1, 0, '2026-10-27 20:48:27.742862', 'dinhsy06112006@gmail.com', '2026-10-27 20:53:27.742796', 'dinh', true, '929116', '$2a$10$d5ClbV.aMeeicv9M/jgEguxxKJRwBGSd06lDkL4wAFsT4ArIFxTme', '0346033591', NULL),
(2, 0, '2026-10-27 20:50:09.886580', 'dinhsy06112006@gmail.com', '2026-10-27 20:55:09.886532', 'dinh', true, '717119', '$2a$10$d5ClbV.aMeeicv9M/jgEguxxKJRwBGSd06lDkL4wAFsT4ArIFxTme', '0346033591', NULL),
(3, 0, '2026-10-28 07:52:22.821558', 'haphuongtrinh20@gmail.com', '2026-10-28 07:57:22.821505', 'Trinh', true, '201935', '$2a$10$HEU0RfnvNWxaStBVx7PPteUiJT0HlVC2ZshulxD/bFeNDPvbUkMP2', '0829097956', NULL),
(4, 0, '2026-10-28 10:29:24.622578', 'thuypt1011@gmail.com', '2026-10-28 10:34:24.622556', 'Thúy Phạm', true, '344363', '$2a$10$URpI8MvQ5oaViyWLuiaC2eCq4ppjN2W6l/tVqxyNzWjn0VEWEV2GW', '0357827958', NULL),
(5, 0, '2026-10-28 16:29:20.907949', 'dinhsy06112006@gmail.com', '2026-10-28 16:34:20.907860', 'Đinh Phú Sỹ', true, '304437', '$2a$10$qPjOva12DwSctrsiGCY5YO2ko1aQIgDfCpRxR6Qg3RS8mrj7pKbjO', '0346033591', 'REGISTER'),
(6, 0, '2026-10-28 16:32:25.117898', 'dinhsy06112006@gmail.com', '2026-10-28 16:37:25.117815', 'Đinh Phú Sỹ', true, '245487', '$2a$10$4cpQcWAJ/.hoBR47fz.J8..Wk1yjYmSxp5ZjUaRVg3lshYM2jNkLe', '0346033591', 'REGISTER'),
(7, 0, '2026-10-28 17:57:19.779864', 'dinhsy06112006@gmail.com', '2026-10-28 18:02:19.779811', 'Đinh Phú Sỹ', true, '783296', '$2a$10$5bFRhT5hE8GYeaBscjq...pmLc3ywJ4Fyuh.LDPH1UJl1QL9WZyia', '0346033591', 'REGISTER'),
(8, 0, '2026-10-29 01:36:29.218490', 'dinhsy06112006@gmail.com', '2026-10-29 01:41:29.218428', 'Đinh Phú Sỹ', true, '151836', '$2a$10$vdxlJDTBOrgv1jchqd9q2OIGzziXzf7MsrDVFeNzG.gNQKBRufSj6', '0346033591', 'REGISTER'),
(9, 0, '2026-10-29 02:32:58.224427', 'dinhsy06112006@gmail.com', '2026-10-29 02:37:58.224375', 'Đinh Phú Sỹ', true, '922061', '$2a$10$JYI5NwU7pkwvyan7676a7e.tUyZ4OMHJxEEo/gq.6pMj9uuVtjar6', '0346033591', 'REGISTER'),
(10, 0, '2026-10-29 03:28:21.237466', 'dinhphusy001@gmail.com', '2026-10-29 03:33:21.237379', 'Đinh Phú Sỹ', true, '752918', '$2a$10$0Q0lHzZNfiYQqBpukyroa.QhvTwQTlBBEMyO8Qzl3b4uQl6qf65Ie', '0346033591', 'REGISTER'),
(11, 0, '2026-10-29 06:01:19.067074', 'dinhsy06112006@gmail.com', '2026-10-29 06:06:19.067051', 'Đinh Phú Sỹ', true, '909843', '$2a$10$zXMkH/Z5SItF4CZExxrlx.ANtXoTOGAJcUHsvVDfCe1XYhSnImRaq', '0346033591', 'REGISTER'),
(12, 0, '2026-10-29 06:28:44.618633', 'dinhsy06112006@gmail.com', '2026-10-29 06:33:44.618530', 'Đinh Phú Sỹ', true, '112745', '$2a$10$KzJZZI5xmp12Iv1ZdMRVHe7SvYENQ1ZrimUFu6ig4zinJik7HaROC', '0346033591', 'REGISTER'),
(13, 0, '2026-10-29 07:04:57.815786', 'dinhsy06112006@gmail.com', '2026-10-29 07:09:57.815752', 'Đinh Phú Sỹ', true, '387026', '$2a$10$KzJZZI5xmp12Iv1ZdMRVHe7SvYENQ1ZrimUFu6ig4zinJik7HaROC', '0346033591', 'REGISTER'),
(14, 0, '2026-10-29 07:25:06.317505', 'dinhsy06112006@gmail.com', '2026-10-29 07:30:06.317461', 'Đinh Phú Sỹ', true, '800161', '$2a$10$KzJZZI5xmp12Iv1ZdMRVHe7SvYENQ1ZrimUFu6ig4zinJik7HaROC', '0346033591', 'REGISTER'),
(15, 0, '2026-10-29 07:40:53.964379', 'dinhsy06112006@gmail.com', '2026-10-29 07:45:53.964330', 'Đinh Phú Sỹ', true, '344395', '$2a$10$STU40CjnGOTZ7STKwZdjvObb99ELV1P6E6yLntjf2UhmK7AwZboCq', '0346033591', 'REGISTER'),
(16, 0, '2026-10-29 07:59:43.899703', 'dinhsy06112006@gmail.com', '2026-10-29 08:04:43.899625', 'Đinh Phú Sỹ', true, '619216', '$2a$10$KRpTuqXowZSlLLqu2Tisfu9We/W8WICqIIwTg.JPI1HgBbRrzpKrC', '0346033591', 'REGISTER'),
(17, 0, '2026-10-29 08:16:49.410386', 'dinhphusy001@gmail.com', '2026-10-29 08:21:49.410321', 'Đinh Phú Sỹ', true, '277296', '$2a$10$bbuVEauWbMuj8MREa7ykRubcAYkifNPtToqGPhAWRcD9utM1n.9MG', '0346033591', 'REGISTER'),
(18, 0, '2026-10-29 08:18:31.076520', 'dinhphusy001@gmail.com', '2026-10-29 08:23:31.076482', 'Đinh Phú Sỹ', true, '404284', '$2a$10$bbuVEauWbMuj8MREa7ykRubcAYkifNPtToqGPhAWRcD9utM1n.9MG', '0346033591', 'REGISTER'),
(19, 0, '2026-10-29 08:19:18.705744', 'test.diagnostic999@gmail.com', '2026-10-29 08:24:18.705715', 'Test Diagnostic', false, '270584', '$2a$10$uuN6Mui/ZSOFus6VkbyV5e.zMcstHpPFM8ASJai1AyCaBB47uklP6', '0901234567', 'REGISTER'),
(20, 0, '2026-10-29 08:32:53.724533', 'dinhphusy001@gmail.com', '2026-10-29 08:37:53.724479', 'Đinh Phú Sỹ', false, '834273', '$2a$10$6l0cKbHf7PYwho6eo0i1ZOklJ7kO7WYrwe5I7l4pnpfnyI.FyCMn6', '0901234567', 'REGISTER'),
(21, 3, '2026-10-29 08:34:24.923485', 'dinhsy06112006@gmail.com', '2026-10-29 08:39:24.923459', 'Đinh Phú Sỹ', true, '630665', '$2a$10$UkrPYCUAKAnCl4dDVKsqA.30sfh4yY7yPFEQIXL0d66Z4NZw8JGmu', '0346033591', 'REGISTER'),
(22, 0, '2026-10-29 08:54:17.028113', 'thuypt1011@gmail.com', '2026-10-29 08:59:17.028094', 'Thúy Phạm', true, '874152', '$2a$10$HW0ggwtK1IvlZmQuaxEJaunxAP9PREV.sOITxXQVSAbxpiPvM0Aw2', '0357827958', 'REGISTER'),
(23, 0, '2026-10-29 09:39:21.319850', 'thuypt1011@gmail.com', '2026-10-29 09:44:21.319828', 'Thúy Phạm', true, '666250', '$2a$10$vAcX7ai.e1n2MVE3eyEu7O85.QCOQGNkOasFqclDoShNEM.hyi2QS', '0357827958', 'REGISTER'),
(24, 0, '2026-10-29 19:22:54.125386', 'dinhsy06112006@gmail.com', '2026-10-29 19:27:54.125329', 'Đinh Phú Sỹ', true, '943216', '$2a$10$GZMSSJu/UWoBI.UvCgEZae5Nh7dsLv9OH3VeD9ca4hdvMBnLikdSC', '0346033591', 'REGISTER'),
(25, 0, '2026-10-29 19:30:22.226873', 'dinhsy06112006@gmail.com', '2026-10-29 19:35:22.226848', 'Đinh Phú Sỹ', true, '500947', '$2a$10$98DrMLk9JjpUtg6ee2I2PeOxejqNcyZ4e8rT6Kw31wsqyVoMupcqi', '0346033591', 'REGISTER'),
(26, 0, '2026-10-29 20:04:18.629116', 'dinhsy06112006@gmail.com', '2026-10-29 20:09:18.629098', 'Đinh Phú Sỹ', true, '922453', '$2a$10$SmuoE28iwCTX6MN.JHqzUezct.dLV20ow03gPfgZ43PuSYReLiHPS', '0346033591', 'REGISTER');

-- ------------------------------------------------------------
-- Structure and Data for table `reviews`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE "reviews" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "comment" text,
  "created_at" datetime(6) NOT NULL,
  "is_verified_purchase" bit(1) DEFAULT NULL,
  "order_id" bigint DEFAULT NULL,
  "rating" int NOT NULL,
  "sub_order_code" varchar(64) DEFAULT NULL,
  "title" varchar(255) DEFAULT NULL,
  "book_id" bigint NOT NULL,
  "user_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  KEY "idx_reviews_book_id" ("book_id"),
  KEY "idx_reviews_user_id" ("user_id"),
  CONSTRAINT "FK6a9k6xvev80se5rreqvuqr7f9" FOREIGN KEY ("book_id") REFERENCES "books" ("id"),
  CONSTRAINT "FKcgy7qjc1r99dp117y9en6lxye" FOREIGN KEY ("user_id") REFERENCES "users" ("id")
);


-- ------------------------------------------------------------
-- Structure and Data for table `roles`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE "roles" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "description" varchar(255) DEFAULT NULL,
  "role_name" varchar(50) NOT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UK716hgxp60ym1lifrdgp67xt5k" ("role_name")
);

INSERT INTO `roles` VALUES 
(1, 'Quản trị viên toàn hệ thống - Toàn quyền cấu hình & quản lý', 'ADMIN'),
(2, 'Quản lý chi nhánh - Điều phối nhân viên & tồn kho chi nhánh', 'BRANCH_MANAGER'),
(3, 'Nhân viên thu mua & kho - Đánh giá, định giá và nhập kho sách cũ', 'WAREHOUSE_STAFF'),
(4, 'Nhân viên bán hàng - Thu ngân tại quầy POS & xử lý đơn online', 'CASHIER'),
(5, 'Khách hàng độc giả - Tìm kiếm, đặt mua sách và quản lý tài khoản', 'CUSTOMER'),
(6, 'Khách hàng', 'ROLE_CUSTOMER');

-- ------------------------------------------------------------
-- Structure and Data for table `stock_transfer_items`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `stock_transfer_items`;
CREATE TABLE "stock_transfer_items" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "quantity" int DEFAULT NULL,
  "book_item_id" bigint DEFAULT NULL,
  "transfer_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "FKr0k2999bg4iqxt0ev413xdsb1" ("book_item_id"),
  KEY "FKertcckucd49nmw8ley2j5pxmh" ("transfer_id"),
  CONSTRAINT "FKertcckucd49nmw8ley2j5pxmh" FOREIGN KEY ("transfer_id") REFERENCES "stock_transfers" ("id"),
  CONSTRAINT "FKr0k2999bg4iqxt0ev413xdsb1" FOREIGN KEY ("book_item_id") REFERENCES "book_items" ("id")
);


-- ------------------------------------------------------------
-- Structure and Data for table `stock_transfers`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `stock_transfers`;
CREATE TABLE "stock_transfers" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "completed_at" datetime(6) DEFAULT NULL,
  "created_at" datetime(6) DEFAULT NULL,
  "reason" text,
  "status" varchar(255) DEFAULT NULL,
  "transfer_code" varchar(255) DEFAULT NULL,
  "from_branch_id" bigint DEFAULT NULL,
  "requested_by_user_id" bigint DEFAULT NULL,
  "to_branch_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UKel3lrfowfromgwcvxlve9xvow" ("transfer_code"),
  KEY "FKfl7cgq3gi53g313v51dky6pd5" ("from_branch_id"),
  KEY "FK7mxruwhwbygg7enrv72i2dw64" ("requested_by_user_id"),
  KEY "FKsqcl96ejd42qe81pqw69wj1vc" ("to_branch_id"),
  CONSTRAINT "FK7mxruwhwbygg7enrv72i2dw64" FOREIGN KEY ("requested_by_user_id") REFERENCES "users" ("id"),
  CONSTRAINT "FKfl7cgq3gi53g313v51dky6pd5" FOREIGN KEY ("from_branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKsqcl96ejd42qe81pqw69wj1vc" FOREIGN KEY ("to_branch_id") REFERENCES "branches" ("id")
);


-- ------------------------------------------------------------
-- Structure and Data for table `sub_order_items`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sub_order_items`;
CREATE TABLE "sub_order_items" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "quantity" int DEFAULT NULL,
  "subtotal" decimal(38,2) DEFAULT NULL,
  "unit_price" decimal(38,2) DEFAULT NULL,
  "book_item_id" bigint DEFAULT NULL,
  "sub_order_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "FKiifrcgrbm8sn6rh4mk4phomlj" ("book_item_id"),
  KEY "FK5v0m24lqb26wr46210sdxhduu" ("sub_order_id"),
  CONSTRAINT "FK5v0m24lqb26wr46210sdxhduu" FOREIGN KEY ("sub_order_id") REFERENCES "sub_orders" ("id"),
  CONSTRAINT "FKiifrcgrbm8sn6rh4mk4phomlj" FOREIGN KEY ("book_item_id") REFERENCES "book_items" ("id")
);

INSERT INTO `sub_order_items` VALUES 
(10, 1, 55000.00, 55000.00, 2, 8),
(11, 1, 55000.00, 55000.00, 4, 8),
(12, 1, 55000.00, 55000.00, 6, 8),
(13, 1, 65000.00, 65000.00, 3, 9),
(14, 1, 55000.00, 55000.00, 2, 10),
(15, 1, 65000.00, 65000.00, 7, 11),
(16, 1, 55000.00, 55000.00, 14, 12),
(17, 1, 55000.00, 55000.00, 8, 13),
(18, 1, 55000.00, 55000.00, 6, 13);

-- ------------------------------------------------------------
-- Structure and Data for table `sub_orders`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sub_orders`;
CREATE TABLE "sub_orders" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "branch_shipping_fee" decimal(38,2) DEFAULT NULL,
  "branch_subtotal" decimal(38,2) DEFAULT NULL,
  "cancel_reason" varchar(500) DEFAULT NULL,
  "cancelled_at" datetime(6) DEFAULT NULL,
  "completed_at" datetime(6) DEFAULT NULL,
  "confirmed_at" datetime(6) DEFAULT NULL,
  "created_at" datetime(6) DEFAULT NULL,
  "shipped_at" datetime(6) DEFAULT NULL,
  "status" varchar(255) DEFAULT NULL,
  "sub_order_code" varchar(255) DEFAULT NULL,
  "tracking_number" varchar(255) DEFAULT NULL,
  "updated_at" datetime(6) DEFAULT NULL,
  "branch_id" bigint DEFAULT NULL,
  "master_order_id" bigint DEFAULT NULL,
  "delivery_notes" varchar(500) DEFAULT NULL,
  "delivery_status" varchar(30) DEFAULT NULL,
  "estimated_delivery_time" datetime(6) DEFAULT NULL,
  "shipper_name" varchar(100) DEFAULT NULL,
  "shipper_phone" varchar(20) DEFAULT NULL,
  "shipper_vehicle" varchar(50) DEFAULT NULL,
  "shipping_carrier" varchar(100) DEFAULT NULL,
  "driver_id" bigint DEFAULT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UKaooivds2njjxkmyv0xfdb5woo" ("sub_order_code"),
  KEY "FKpvvcc817vvhbcs3p9p6tb8c2k" ("branch_id"),
  KEY "FKlwrxu5fjcvj1mddlp37cqy6im" ("master_order_id"),
  KEY "FK83thw210fyo0wcy7dxsaogcay" ("driver_id"),
  CONSTRAINT "FK83thw210fyo0wcy7dxsaogcay" FOREIGN KEY ("driver_id") REFERENCES "drivers" ("id"),
  CONSTRAINT "FKlwrxu5fjcvj1mddlp37cqy6im" FOREIGN KEY ("master_order_id") REFERENCES "master_orders" ("id"),
  CONSTRAINT "FKpvvcc817vvhbcs3p9p6tb8c2k" FOREIGN KEY ("branch_id") REFERENCES "branches" ("id")
);

INSERT INTO `sub_orders` VALUES 
(8, 0.00, 165000.00, NULL, NULL, '2026-10-29 19:38:12.091783', '2026-10-29 19:36:30.326083', '2026-10-29 19:34:59.594601', '2026-10-29 19:37:22.370154', 'COMPLETED', 'SUB-2-1793302499493-90', 'B4L-SHIP-1793302642370', '2026-10-29 19:38:12.222075', 2, 8, 'Đóng gói cẩn thận, giao tận tay nhé shopppp iu', 'DELIVERED', '2026-10-29 21:37:22.370154', 'Shipper Đối Tác GHN', '1900.636677', 'MOTORBIKE - GHN-FAST', 'Giao Hàng Nhanh', 6),
(9, 4500.00, 65000.00, NULL, NULL, NULL, '2026-10-29 20:12:56.140015', '2026-10-29 19:47:14.468870', NULL, 'CONFIRMED', 'SUB-1-1793303234423-41', NULL, '2026-10-29 20:12:56.429999', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(10, 4500.00, 55000.00, NULL, NULL, NULL, '2026-10-29 19:47:43.071903', '2026-10-29 19:47:14.556003', NULL, 'CONFIRMED', 'SUB-2-1793303234423-53', NULL, '2026-10-29 19:47:43.367518', 2, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(11, 0.00, 65000.00, NULL, NULL, NULL, '2026-10-29 20:12:50.167147', '2026-10-29 20:07:01.982336', '2026-10-29 20:13:32.677934', 'SHIPPED', 'SUB-1-1793304421893-30', 'B4L-SHIP-1793304812677', '2026-10-29 20:13:32.967977', 1, 10, NULL, 'DELIVERING', '2026-10-29 22:13:32.677934', 'Trần Quốc Bảo', '0903.222.333', 'MOTORBIKE - 59P2-777.99', 'Đội xe Book4Life Q1', 2),
(12, 0.00, 55000.00, NULL, NULL, NULL, NULL, '2026-10-29 20:07:02.070669', NULL, 'PENDING', 'SUB-2-1793304421893-12', NULL, NULL, 2, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(13, 0.00, 110000.00, NULL, NULL, '2026-10-29 20:11:10.564558', '2026-10-29 20:11:10.564557', '2026-10-29 20:11:10.564559', NULL, 'COMPLETED', 'SUB-POS-1793304670563', NULL, NULL, 2, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ------------------------------------------------------------
-- Structure and Data for table `trade_in_images`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `trade_in_images`;
CREATE TABLE "trade_in_images" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "cloudinary_public_id" varchar(255) NOT NULL,
  "image_type" varchar(30) NOT NULL,
  "image_url" varchar(500) NOT NULL,
  "trade_in_request_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  KEY "FK5vmt7gc6b5ifup25uw1uwm91o" ("trade_in_request_id"),
  CONSTRAINT "FK5vmt7gc6b5ifup25uw1uwm91o" FOREIGN KEY ("trade_in_request_id") REFERENCES "trade_in_requests" ("id")
);

INSERT INTO `trade_in_images` VALUES 
(1, 'cov_front', 'COVER_FRONT', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793302938/trade-in/sfo4u29l0wgjhe0htmxx.webp', 1),
(2, 'cov_back', 'COVER_BACK', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793302942/trade-in/v3omj9lllbrkt9dyz6sa.webp', 1),
(3, 'spine', 'SPINE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793302947/trade-in/oderwfkujtdzop4shkrs.webp', 1),
(4, 'top_edge', 'TOP_EDGE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793302951/trade-in/u9vo0q562vjopeww7wan.webp', 1),
(5, 'bottom_edge', 'BOTTOM_EDGE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793302955/trade-in/gc0zdo8ml4ertvv0kz0c.webp', 1),
(6, 'fore_edge', 'FORE_EDGE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793302959/trade-in/tumu2c9l79zdmqrac3l3.webp', 1),
(7, 'cov_front', 'COVER_FRONT', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793304495/trade-in/lhecglrhnrruomb7spmh.webp', 2),
(8, 'cov_back', 'COVER_BACK', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793304498/trade-in/w62dvaaia7xrgtu3pgw4.webp', 2),
(9, 'spine', 'SPINE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793304501/trade-in/zqak3tlet3zzoskzshvp.webp', 2),
(10, 'top_edge', 'TOP_EDGE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793304504/trade-in/kb6d13dtlocqrp1sqlii.webp', 2),
(11, 'bottom_edge', 'BOTTOM_EDGE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793304509/trade-in/ujn04l5hz2anhcflpagh.webp', 2),
(12, 'fore_edge', 'FORE_EDGE', 'https://res.cloudinary.com/dhpbqf4l/image/upload/v1793304513/trade-in/hwszrgnugmxwwc6mjara.webp', 2);

-- ------------------------------------------------------------
-- Structure and Data for table `trade_in_requests`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `trade_in_requests`;
CREATE TABLE "trade_in_requests" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "auto_estimated_price" decimal(12,2) NOT NULL,
  "created_at" datetime(6) NOT NULL,
  "delivery_method" varchar(30) NOT NULL,
  "request_code" varchar(50) NOT NULL,
  "staff_condition_grade" varchar(30) DEFAULT NULL,
  "staff_notes" varchar(500) DEFAULT NULL,
  "staff_reviewed_price" decimal(12,2) DEFAULT NULL,
  "status" varchar(35) NOT NULL,
  "survey_answers_json" text NOT NULL,
  "user_condition_grade" varchar(30) NOT NULL,
  "book_id" bigint NOT NULL,
  "reviewed_by_staff_id" bigint DEFAULT NULL,
  "target_branch_id" bigint NOT NULL,
  "user_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UKjswes6htcqxjrs5tvr3tst44h" ("request_code"),
  KEY "FKobaeg8toche7c2x8s8hcle230" ("book_id"),
  KEY "FKpg16khd8iduqgqv6vup67dl0n" ("reviewed_by_staff_id"),
  KEY "FKg4r6dy0nao361okxpfwlua17a" ("target_branch_id"),
  KEY "FKobt4yxc30cp7yamk2eifi38lk" ("user_id"),
  CONSTRAINT "FKg4r6dy0nao361okxpfwlua17a" FOREIGN KEY ("target_branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKobaeg8toche7c2x8s8hcle230" FOREIGN KEY ("book_id") REFERENCES "books" ("id"),
  CONSTRAINT "FKobt4yxc30cp7yamk2eifi38lk" FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
  CONSTRAINT "FKpg16khd8iduqgqv6vup67dl0n" FOREIGN KEY ("reviewed_by_staff_id") REFERENCES "users" ("id")
);

INSERT INTO `trade_in_requests` VALUES 
(1, 48000.00, '2026-10-29 19:43:50.183027', 'BRING_TO_STORE', 'BL-20261029-8792', 'LIKE_NEW', '', 90000.00, 'ACCEPTED_AWAITING_DELIVERY', '{"bookInfo":{"title":"Cô gái đến từ hôm qua","category":"Văn Học","publisher":"Nhà xuất bản trẻ","publicationYear":2024,"originalPrice":95000},"conditionSurvey":{"coverTorn":false,"coverPeeling":false,"spineLoose":false,"yellowPages":false,"markingsPresent":false,"dogEared":false,"tornPages":false,"missingAccessory":false,"moldySmell":false,"otherDefectDescription":"","otherDefectImageUrl":""},"receipt":{"receiptCode":"BL-20261029-8792","customerName":"Đinh Phú Sỹ","customerPhone":"0346033591","customerEmail":"dinhsy06112006@gmail.com","customerAddress":"Số 1, VNN, Phường Thủ Đức, TP. HCM","targetBranchId":2,"targetBranchName":"Book4Life Thủ Đức","deliveryMethod":"BRING_TO_STORE","expectedArrivalTime":"2026-10-31 18:00 - 20:30 (Buổi Tối)"}}', 'LIKE_NEW', 1, 3, 2, 14),
(2, 19000.00, '2026-10-29 20:09:14.918109', 'BRING_TO_STORE', 'BL-20261029-5188', 'GOOD', '', 19000.00, 'OFFERED', '{"bookInfo":{"title":"Cô gái đến từ hôm qua","category":"Kinh Điển","publisher":"Nhà xuất bản trẻ","publicationYear":2023,"originalPrice":95000},"conditionSurvey":{"coverTorn":false,"coverPeeling":false,"spineLoose":false,"yellowPages":false,"markingsPresent":false,"dogEared":false,"tornPages":false,"missingAccessory":false,"moldySmell":false,"otherDefectDescription":"","otherDefectImageUrl":""},"receipt":{"receiptCode":"BL-20261029-5188","customerName":"Đinh Phú Sỹ","customerPhone":"0346033591","customerEmail":"dinhsy06112006@gmail.com","customerAddress":"Số 1, VNN, Phường Thủ Đức, TP. HCM","targetBranchId":2,"targetBranchName":"Book4Life Thủ Đức","deliveryMethod":"BRING_TO_STORE","expectedArrivalTime":"2026-10-30 14:00 - 17:30 (Buổi Chiều)"}}', 'ACCEPTABLE', 1, 3, 2, 16);

-- ------------------------------------------------------------
-- Structure and Data for table `users`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE "users" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "avatar_url" varchar(500) DEFAULT NULL,
  "created_at" datetime(6) NOT NULL,
  "email" varchar(150) NOT NULL,
  "full_name" varchar(100) NOT NULL,
  "is_active" bit(1) NOT NULL,
  "password" varchar(255) NOT NULL,
  "phone" varchar(20) DEFAULT NULL,
  "branch_id" bigint DEFAULT NULL,
  "role_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  UNIQUE KEY "UK6dotkott2kjsp8vw4d0m25fb7" ("email"),
  KEY "FK9o70sp9ku40077y38fk4wieyk" ("branch_id"),
  KEY "FKp56c1712k691lhsyewcssf40f" ("role_id"),
  CONSTRAINT "FK9o70sp9ku40077y38fk4wieyk" FOREIGN KEY ("branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKp56c1712k691lhsyewcssf40f" FOREIGN KEY ("role_id") REFERENCES "roles" ("id")
);

INSERT INTO `users` VALUES 
(1, NULL, '2026-10-22 05:38:26.124620', 'admin.book4life@gmail.com', 'Nguyễn Văn Admin', true, '$2a$10$FOo.mVGnyj9y7meN0wkSIen0VZehf4W9yy/3qBGoN/LqWeJPWzOyi', '0901234567', NULL, 1),
(2, NULL, '2026-10-22 05:38:28.525987', 'manage.book4life@gmail.com', 'Trần Thị Quản Lý (HN)', true, '$2a$10$NYLXtreCjdlJyPq6qEIN1OY/g0e50rz..k./BcZcyifgC2QHRKZT6', '0902345678', 1, 2),
(3, NULL, '2026-10-22 05:38:30.924009', 'warehouse.book4life@gmail.com', 'Lê Văn Thủ Kho', true, '$2a$10$UvjjyZlxKWOqOAPjFLpbpuWRV7kLUfv/axFQK8uyurK0L8zUQnAXW', '0903456789', 1, 3),
(4, NULL, '2026-10-22 05:38:34.720650', 'cashier.book4life@gmail.com', 'Phạm Thu Ngân', true, '$2a$10$yWVKh.Zuv4OPxSFsgW70wO8hbM8MMEzuGCxCAWztqzlv43c6x3Pdy', '0904567890', 2, 4),
(14, NULL, '2026-10-29 19:31:22.665707', 'user.book4life@gmail.com', 'Đinh Phú Sỹ', false, '$2a$10$iSK80yJBKHw.939pF1T/mOLf2vEBJ/jMdZd8uq23YydkLcGp5Dyvi', '0346033591', NULL, 6),
(15, 'https://lh3.googleusercontent.com/a/default-user=s96-c', '2026-10-29 20:03:43.725041', 'phusy779@gmail.com', 'Đinh Sỹ', false, '$2a$10$/814ZCDQEPinM5iV0M.JEuoiQm9GLLsgYpnxOBUgZ7WoQk1FDiCKS', '', NULL, 6),
(16, NULL, '2026-10-29 20:04:46.436596', 'dinhsy06112006@gmail.com', 'Đinh Phú Sỹ', true, '$2a$10$SmuoE28iwCTX6MN.JHqzUezct.dLV20ow03gPfgZ43PuSYReLiHPS', '0346033591', NULL, 6);

-- ------------------------------------------------------------
-- Structure and Data for table `wishlists`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `wishlists`;
CREATE TABLE "wishlists" (
  "id" bigint NOT NULL AUTO_INCREMENT,
  "created_at" datetime(6) NOT NULL,
  "is_notified" bit(1) NOT NULL,
  "min_condition_grade" varchar(30) NOT NULL,
  "notified_at" datetime(6) DEFAULT NULL,
  "book_id" bigint NOT NULL,
  "preferred_branch_id" bigint DEFAULT NULL,
  "user_id" bigint NOT NULL,
  PRIMARY KEY ("id"),
  KEY "FKn2q3ciks1phej0dwcwg4or158" ("preferred_branch_id"),
  KEY "FK330pyw2el06fn5g28ypyljt16" ("user_id"),
  KEY "FKo69vdp48axn9ovctfp0jrlgum" ("book_id"),
  CONSTRAINT "FK330pyw2el06fn5g28ypyljt16" FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
  CONSTRAINT "FKn2q3ciks1phej0dwcwg4or158" FOREIGN KEY ("preferred_branch_id") REFERENCES "branches" ("id"),
  CONSTRAINT "FKo69vdp48axn9ovctfp0jrlgum" FOREIGN KEY ("book_id") REFERENCES "books" ("id")
);

INSERT INTO `wishlists` VALUES 
(1, '2026-10-29 19:53:41.353030', false, 'GOOD', NULL, 1, NULL, 14);

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- BACKUP COMPLETE: 23 tables, 141 rows.
-- ============================================================

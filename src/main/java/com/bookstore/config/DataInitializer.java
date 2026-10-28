package com.bookstore.config;

import com.bookstore.entity.*;
import com.bookstore.entity.Driver;
import com.bookstore.repository.*;
import com.bookstore.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final DriverRepository driverRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BranchRepository branchRepository,
                           CategoryRepository categoryRepository,
                           AuthorRepository authorRepository,
                           BookRepository bookRepository,
                           BookItemRepository bookItemRepository,
                           PasswordEncoder passwordEncoder,
                           DataSource dataSource,
                           DriverRepository driverRepository,
                           CouponRepository couponRepository,
                           ReviewRepository reviewRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.bookItemRepository = bookItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
        this.driverRepository = driverRepository;
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("Checking and initializing standard SRS Roles and Users...");

        // 0. Ensure otp_verifications table exists via direct JDBC
        ensureOtpTableExists();

        // 0.1 Xóa sạch tất cả các đơn hàng cũ theo yêu cầu người dùng
        cleanUpAllOldOrders();

        // 1. Initialize 5 Roles from SRS
        Role roleAdmin = getOrCreateRole("ADMIN", "Quản trị viên toàn hệ thống - Toàn quyền cấu hình & quản lý");
        Role roleManager = getOrCreateRole("BRANCH_MANAGER", "Quản lý chi nhánh - Điều phối nhân viên & tồn kho chi nhánh");
        Role roleWarehouse = getOrCreateRole("WAREHOUSE_STAFF", "Nhân viên thu mua & kho - Đánh giá, định giá và nhập kho sách cũ");
        Role roleCashier = getOrCreateRole("CASHIER", "Nhân viên bán hàng - Thu ngân tại quầy POS & xử lý đơn online");
        Role roleCustomer = getOrCreateRole("CUSTOMER", "Khách hàng độc giả - Tìm kiếm, đặt mua sách và quản lý tài khoản");

        // 2. Khởi tạo danh mục, tác giả, chi nhánh và sách nếu CSDL chưa có dữ liệu (Cloud Aiven/MySQL)
        ensureMasterCatalogSeeded();

        // 3. Fetch sample branches if available
        List<Branch> branches = branchRepository.findAll();
        Branch branch1 = branches.size() > 0 ? branches.get(0) : null;
        Branch branch2 = branches.size() > 1 ? branches.get(1) : branch1;

        // 4. Initialize 5 Standard Accounts (Default Password: "1234@abc")
        User adminUser = createOrUpdateUser("admin.book4life@gmail.com", "1234@abc", "Nguyễn Văn Admin", "0901234567", roleAdmin, null);
        User managerUser = createOrUpdateUser("manage.book4life@gmail.com", "1234@abc", "Trần Thị Quản Lý (HN)", "0902345678", roleManager, branch1);
        User warehouseUser = createOrUpdateUser("warehouse.book4life@gmail.com", "1234@abc", "Lê Văn Thủ Kho", "0903456789", roleWarehouse, branch1);
        User cashierUser = createOrUpdateUser("cashier.book4life@gmail.com", "1234@abc", "Phạm Thu Ngân", "0904567890", roleCashier, branch2);
        User customerUser = createOrUpdateUser("user.book4life@gmail.com", "1234@abc", "Hoàng Độc Giả", "0905678901", roleCustomer, null);

        // 5. Xóa bỏ tất cả các tài khoản ngoài 5 tài khoản được chỉ định
        cleanUpExtraneousUsers(adminUser.getId(), managerUser.getId(), warehouseUser.getId(), cashierUser.getId(), customerUser.getId());

        // 6. Đảm bảo ID của 5 tài khoản luôn cố định là 1..5
        ensureStandardUserIds();

        // 7. Khởi tạo danh sách Tài xế (Drivers) và Mã Freeship / Ưu đãi giao hàng
        ensureDriversAndCouponsSeeded(branches);

        // 8. Khởi tạo các đánh giá mẫu (mặc định 5 sao) nếu chưa có đánh giá nào
        ensureSampleReviewsSeeded(customerUser);

        logger.info("DataInitializer completed successfully. 5 Roles & default catalog are ready!");
    }

    private void ensureMasterCatalogSeeded() {
        try {
            // 1. Seed Branches if empty
            if (branchRepository.count() == 0) {
                logger.info("CSDL chưa có chi nhánh, đang tạo 3 chi nhánh mẫu...");
                Branch b1 = new Branch(null, "Book4Life Quận 1", "123 Nguyễn Thị Minh Khai, P. Bến Thành, Q.1", "028.3912.8888", "TP. Hồ Chí Minh", new BigDecimal("10.7719"), new BigDecimal("106.6983"), true);
                Branch b2 = new Branch(null, "Book4Life Thủ Đức", "456 Võ Văn Ngân, TP. Thủ Đức", "028.3896.7777", "TP. Hồ Chí Minh", new BigDecimal("10.8504"), new BigDecimal("106.7718"), true);
                Branch b3 = new Branch(null, "Book4Life Hoàn Kiếm", "78 Tràng Tiền, Q. Hoàn Kiếm", "024.3934.6666", "Hà Nội", new BigDecimal("21.0285"), new BigDecimal("105.8542"), true);
                branchRepository.saveAll(List.of(b1, b2, b3));
            }

            // 2. Seed Categories if empty
            if (categoryRepository.count() == 0) {
                logger.info("CSDL chưa có thể loại, đang tạo các danh mục sách tiêu chuẩn...");
                Category cKinhDien = new Category(null, "Kinh Điển", null);
                Category cVanHoc = new Category(null, "Văn Học", null);
                Category cKinhTe = new Category(null, "Kinh Tế & Quản Trị", null);
                Category cKyNang = new Category(null, "Kỹ Năng Sống", null);
                Category cLichSu = new Category(null, "Lịch Sử & Văn Hóa", null);
                Category cKhoaHoc = new Category(null, "Khoa Học & Triết Học", null);
                categoryRepository.saveAll(List.of(cKinhDien, cVanHoc, cKinhTe, cKyNang, cLichSu, cKhoaHoc));
            }

            // 3. Seed Authors if empty
            if (authorRepository.count() == 0) {
                logger.info("CSDL chưa có tác giả, đang tạo các tác giả tiêu biểu...");
                Author aNamCao = new Author(null, "Nam Cao", "Nhà văn hiện thực xuất sắc của văn học Việt Nam.", null);
                Author aVuTrongPhung = new Author(null, "Vũ Trọng Phụng", "Ông vua phóng sự đất Bắc thời kỳ 1930 - 1945.", null);
                Author aNguyenDu = new Author(null, "Nguyễn Du", "Đại thi hào dân tộc Việt Nam, danh nhân văn hóa thế giới.", null);
                Author aToHoai = new Author(null, "Tô Hoài", "Cây đại thụ của nền văn học thiếu nhi hiện đại Việt Nam.", null);
                Author aAntoine = new Author(null, "Antoine de Saint-Exupéry", "Nhà văn, phi công tiên phong người Pháp, tác giả Hoàng Tử Bé.", null);
                Author aPaulo = new Author(null, "Paulo Coelho", "Tiểu thuyết gia người Brazil, tác giả kiệt tác Nhà Giả Kim.", null);
                Author aDale = new Author(null, "Dale Carnegie", "Bậc thầy phát triển bản thân, tác giả Đắc Nhân Tâm.", null);
                authorRepository.saveAll(List.of(aNamCao, aVuTrongPhung, aNguyenDu, aToHoai, aAntoine, aPaulo, aDale));
            }

            // 4. Seed Books & BookItems if empty
            if (bookRepository.count() == 0) {
                logger.info("CSDL chưa có sách, đang khởi tạo 7 đầu sách kinh điển mẫu...");
                Category cKinhDien = categoryRepository.findByNameIgnoreCase("Kinh Điển").orElse(null);
                Category cKyNang = categoryRepository.findByNameIgnoreCase("Kỹ Năng Sống").orElse(cKinhDien);
                List<Author> authors = authorRepository.findAll();
                Author aNamCao = authors.stream().filter(a -> a.getName().contains("Nam Cao")).findFirst().orElse(null);
                Author aVuTrongPhung = authors.stream().filter(a -> a.getName().contains("Vũ Trọng Phụng")).findFirst().orElse(null);
                Author aNguyenDu = authors.stream().filter(a -> a.getName().contains("Nguyễn Du")).findFirst().orElse(null);
                Author aToHoai = authors.stream().filter(a -> a.getName().contains("Tô Hoài")).findFirst().orElse(null);
                Author aAntoine = authors.stream().filter(a -> a.getName().contains("Antoine")).findFirst().orElse(null);
                Author aPaulo = authors.stream().filter(a -> a.getName().contains("Paulo")).findFirst().orElse(null);
                Author aDale = authors.stream().filter(a -> a.getName().contains("Dale")).findFirst().orElse(null);

                List<Branch> allBranches = branchRepository.findAll();
                Branch b1 = !allBranches.isEmpty() ? allBranches.get(0) : null;
                Branch b2 = allBranches.size() > 1 ? allBranches.get(1) : b1;

                if (cKinhDien != null && aNamCao != null && b1 != null) {
                    Book bChiPheo = new Book(null, "Chí Phèo", "978-604-1-12345-1", aNamCao, cKinhDien, "NXB Văn Học", 1941, new BigDecimal("85000"), "/images/vintage-hero-books.png", "Tuyệt phẩm văn học hiện thực phê phán kinh điển của Nam Cao.");
                    Book bSoDo = new Book(null, "Số Đỏ", "978-604-1-12345-2", aVuTrongPhung != null ? aVuTrongPhung : aNamCao, cKinhDien, "NXB Văn Học", 1936, new BigDecimal("95000"), "/images/vintage-hero-books.png", "Kiệt tác châm biếm kinh điển của văn học Việt Nam thời kỳ tiền chiến.");
                    Book bKieu = new Book(null, "Truyện Kiều", "978-604-1-12345-3", aNguyenDu != null ? aNguyenDu : aNamCao, cKinhDien, "NXB Văn Học", 1820, new BigDecimal("120000"), "/images/vintage-hero-books.png", "Đỉnh cao thi ca văn học cổ điển Việt Nam.");
                    Book bDeMen = new Book(null, "Dế Mèn Phiêu Lưu Ký", "978-604-1-12345-4", aToHoai != null ? aToHoai : aNamCao, cKinhDien, "NXB Kim Đồng", 1941, new BigDecimal("65000"), "/images/vintage-hero-books.png", "Tác phẩm thiếu nhi kinh điển gắn liền với tuổi thơ nhiều thế hệ.");
                    Book bHoangTuBe = new Book(null, "Hoàng Tử Bé (The Little Prince)", "978-604-1-12345-5", aAntoine != null ? aAntoine : aNamCao, cKinhDien, "NXB Hội Nhà Văn", 1943, new BigDecimal("75000"), "/images/vintage-hero-books.png", "Cuốn sách kinh điển thế giới về tình bạn và triết lý sống.");
                    Book bNhaGiaKim = new Book(null, "Nhà Giả Kim (The Alchemist)", "978-604-1-12345-6", aPaulo != null ? aPaulo : aNamCao, cKinhDien, "NXB Nhã Nam", 1988, new BigDecimal("89000"), "/images/vintage-hero-books.png", "Tác phẩm truyền cảm hứng kinh điển dịch ra hơn 80 thứ tiếng trên toàn thế giới.");
                    Book bDacNhanTam = new Book(null, "Đắc Nhân Tâm", "978-604-1-12345-7", aDale != null ? aDale : aNamCao, cKyNang, "NXB First News", 1936, new BigDecimal("110000"), "/images/vintage-hero-books.png", "Cuốn sách kỹ năng kinh điển bán chạy nhất mọi thời đại.");

                    List<Book> savedBooks = bookRepository.saveAll(List.of(bChiPheo, bSoDo, bKieu, bDeMen, bHoangTuBe, bNhaGiaKim, bDacNhanTam));

                    for (int i = 0; i < savedBooks.size(); i++) {
                        Book book = savedBooks.get(i);
                        BookItem item1 = new BookItem(null, book, b1, "SKU-B1-" + (1000 + i), "LIKE_NEW", book.getOriginalPrice().multiply(new BigDecimal("0.65")), 5, "Kệ A1-0" + (i + 1), "Sách tuyển chọn 95%, giấy đẹp nguyên vẹn", LocalDateTime.now(), "AVAILABLE");
                        BookItem item2 = new BookItem(null, book, b2, "SKU-B2-" + (2000 + i), "VERY_GOOD", book.getOriginalPrice().multiply(new BigDecimal("0.55")), 3, "Kệ B2-0" + (i + 1), "Sách còn tốt 85%, giấy ngả vàng tự nhiên", LocalDateTime.now(), "AVAILABLE");
                        bookItemRepository.saveAll(List.of(item1, item2));
                    }
                    logger.info("Đã khởi tạo thành công 7 đầu sách kinh điển và dữ liệu tồn kho tại các chi nhánh!");
                }
            }
        } catch (Exception e) {
            logger.warn("ensureMasterCatalogSeeded caught exception: {}", e.getMessage());
        }
    }

    private void ensureStandardUserIds() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (!dbName.contains("sql server") && !dbName.contains("microsoft")) {
                logger.info("Non-MSSQL database detected ({}), skipping T-SQL specific renumbering.", dbName);
                return;
            }

            // Check if all 5 users already have IDs 1, 2, 3, 4, 5
            try (java.sql.ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM users WHERE " +
                    "(email = 'admin.book4life@gmail.com' AND id = 1) OR " +
                    "(email = 'manage.book4life@gmail.com' AND id = 2) OR " +
                    "(email = 'warehouse.book4life@gmail.com' AND id = 3) OR " +
                    "(email = 'cashier.book4life@gmail.com' AND id = 4) OR " +
                    "(email = 'user.book4life@gmail.com' AND id = 5)")) {
                if (rs.next() && rs.getInt(1) == 5) {
                    logger.info("5 standard user accounts already have exact IDs 1..5.");
                    return;
                }
            }

            logger.info("Renumbering standard user accounts to exact IDs 1, 2, 3, 4, 5...");

            // Step A: Disable constraints & triggers, reassign FKs in dependent tables
            stmt.execute("ALTER TABLE users NOCHECK CONSTRAINT ALL;");

            stmt.execute("IF OBJECT_ID('master_orders', 'U') IS NOT NULL UPDATE master_orders SET user_id = 9005 WHERE user_id = (SELECT id FROM users WHERE email = 'user.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('ai_chat_messages', 'U') IS NOT NULL UPDATE ai_chat_messages SET user_id = 9005 WHERE user_id = (SELECT id FROM users WHERE email = 'user.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('wishlists', 'U') IS NOT NULL UPDATE wishlists SET user_id = 9005 WHERE user_id = (SELECT id FROM users WHERE email = 'user.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL UPDATE trade_in_requests SET user_id = 9005 WHERE user_id = (SELECT id FROM users WHERE email = 'user.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL UPDATE trade_in_requests SET reviewed_by_staff_id = 9001 WHERE reviewed_by_staff_id = (SELECT id FROM users WHERE email = 'admin.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('stock_transfers', 'U') IS NOT NULL UPDATE stock_transfers SET requested_by_user_id = 9001 WHERE requested_by_user_id = (SELECT id FROM users WHERE email = 'admin.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('order_status_history', 'U') IS NOT NULL UPDATE order_status_history SET updated_by_user_id = 9001 WHERE updated_by_user_id = (SELECT id FROM users WHERE email = 'admin.book4life@gmail.com');");
            stmt.execute("IF OBJECT_ID('coupon_usages', 'U') IS NOT NULL UPDATE coupon_usages SET user_id = 9005 WHERE user_id = (SELECT id FROM users WHERE email = 'user.book4life@gmail.com');");

            // Step B: Copy the 5 users to a temp table
            stmt.execute("IF OBJECT_ID('tempdb..#TempStandardUsers') IS NOT NULL DROP TABLE #TempStandardUsers;");
            stmt.execute("SELECT * INTO #TempStandardUsers FROM users WHERE email IN ('admin.book4life@gmail.com', 'manage.book4life@gmail.com', 'warehouse.book4life@gmail.com', 'cashier.book4life@gmail.com', 'user.book4life@gmail.com');");

            // Step C: Delete all users
            stmt.execute("DELETE FROM users;");

            // Step D: Enable IDENTITY_INSERT and re-insert with exact IDs 1..5
            stmt.execute("SET IDENTITY_INSERT users ON;");
            stmt.execute("INSERT INTO users (id, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at) " +
                    "SELECT 1, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at FROM #TempStandardUsers WHERE email = 'admin.book4life@gmail.com';");
            stmt.execute("INSERT INTO users (id, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at) " +
                    "SELECT 2, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at FROM #TempStandardUsers WHERE email = 'manage.book4life@gmail.com';");
            stmt.execute("INSERT INTO users (id, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at) " +
                    "SELECT 3, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at FROM #TempStandardUsers WHERE email = 'warehouse.book4life@gmail.com';");
            stmt.execute("INSERT INTO users (id, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at) " +
                    "SELECT 4, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at FROM #TempStandardUsers WHERE email = 'cashier.book4life@gmail.com';");
            stmt.execute("INSERT INTO users (id, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at) " +
                    "SELECT 5, email, password_hash, full_name, phone, role_id, branch_id, is_active, created_at FROM #TempStandardUsers WHERE email = 'user.book4life@gmail.com';");
            stmt.execute("SET IDENTITY_INSERT users OFF;");

            // Step E: Reseed identity to 5 so next auto-generated ID is 6
            stmt.execute("DBCC CHECKIDENT ('users', RESEED, 5);");

            // Step F: Restore FK references in dependent tables
            stmt.execute("IF OBJECT_ID('master_orders', 'U') IS NOT NULL UPDATE master_orders SET user_id = 5 WHERE user_id = 9005;");
            stmt.execute("IF OBJECT_ID('ai_chat_messages', 'U') IS NOT NULL UPDATE ai_chat_messages SET user_id = 5 WHERE user_id = 9005;");
            stmt.execute("IF OBJECT_ID('wishlists', 'U') IS NOT NULL UPDATE wishlists SET user_id = 5 WHERE user_id = 9005;");
            stmt.execute("IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL UPDATE trade_in_requests SET user_id = 5 WHERE user_id = 9005;");
            stmt.execute("IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL UPDATE trade_in_requests SET reviewed_by_staff_id = 1 WHERE reviewed_by_staff_id = 9001;");
            stmt.execute("IF OBJECT_ID('stock_transfers', 'U') IS NOT NULL UPDATE stock_transfers SET requested_by_user_id = 1 WHERE requested_by_user_id = 9001;");
            stmt.execute("IF OBJECT_ID('order_status_history', 'U') IS NOT NULL UPDATE order_status_history SET updated_by_user_id = 1 WHERE updated_by_user_id = 9001;");
            stmt.execute("IF OBJECT_ID('coupon_usages', 'U') IS NOT NULL UPDATE coupon_usages SET user_id = 5 WHERE user_id = 9005;");

            // Step G: Re-enable constraints
            stmt.execute("ALTER TABLE users CHECK CONSTRAINT ALL;");
            stmt.execute("DROP TABLE #TempStandardUsers;");

            logger.info("Standard user IDs renumbered successfully: 1=Admin, 2=Manager, 3=Warehouse, 4=Cashier, 5=Customer");

        } catch (Exception ex) {
            logger.warn("Could not renumber standard user IDs: {}", ex.getMessage());
        }
    }

    private void ensureOtpTableExists() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (!dbName.contains("sql server") && !dbName.contains("microsoft")) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS otp_verifications (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  email VARCHAR(255) NOT NULL," +
                    "  otp_code VARCHAR(6) NOT NULL," +
                    "  password_hash VARCHAR(255) NOT NULL," +
                    "  full_name VARCHAR(255)," +
                    "  phone VARCHAR(50)," +
                    "  otp_type VARCHAR(50) DEFAULT 'REGISTER'," +
                    "  attempts INT DEFAULT 0," +
                    "  is_used BOOLEAN DEFAULT FALSE," +
                    "  created_at DATETIME NOT NULL," +
                    "  expires_at DATETIME NOT NULL" +
                    ")"
                );
                try {
                    stmt.execute("ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS otp_type VARCHAR(50) DEFAULT 'REGISTER'");
                } catch (Exception ignored) {}
                logger.info("Table otp_verifications verified/created for {}.", dbName);
                return;
            }
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='otp_verifications' AND xtype='U') " +
                "BEGIN " +
                "  CREATE TABLE otp_verifications ( " +
                "    id BIGINT IDENTITY(1,1) PRIMARY KEY, " +
                "    email VARCHAR(255) NOT NULL, " +
                "    otp_code VARCHAR(6) NOT NULL, " +
                "    password_hash VARCHAR(255) NOT NULL, " +
                "    full_name NVARCHAR(255), " +
                "    phone VARCHAR(50), " +
                "    otp_type VARCHAR(50) DEFAULT 'REGISTER', " +
                "    attempts INT DEFAULT 0, " +
                "    is_used BIT DEFAULT 0, " +
                "    created_at DATETIME2 NOT NULL, " +
                "    expires_at DATETIME2 NOT NULL " +
                "  ); " +
                "  CREATE INDEX idx_otp_email_created ON otp_verifications(email, created_at); " +
                "END"
            );
            try {
                stmt.execute("IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('otp_verifications') AND name='otp_type') ALTER TABLE otp_verifications ADD otp_type VARCHAR(50) DEFAULT 'REGISTER';");
            } catch (Exception ignored) {}
            logger.info("Table otp_verifications verified/created successfully.");
        } catch (Exception ex) {
            logger.warn("Could not verify/create otp_verifications table: {}", ex.getMessage());
        }
    }

    private void cleanUpExtraneousUsers(Long aid, Long mid, Long wid, Long chid, Long cid) {
        String idList = aid + ", " + mid + ", " + wid + ", " + chid + ", " + cid;
        String validEmails = "'admin.book4life@gmail.com', 'manage.book4life@gmail.com', 'warehouse.book4life@gmail.com', 'cashier.book4life@gmail.com', 'user.book4life@gmail.com'";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (dbName.contains("sql server") || dbName.contains("microsoft")) {
                stmt.execute("IF OBJECT_ID('master_orders', 'U') IS NOT NULL UPDATE master_orders SET user_id = " + cid + " WHERE user_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('ai_chat_messages', 'U') IS NOT NULL UPDATE ai_chat_messages SET user_id = " + cid + " WHERE user_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('wishlists', 'U') IS NOT NULL UPDATE wishlists SET user_id = " + cid + " WHERE user_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL UPDATE trade_in_requests SET user_id = " + cid + " WHERE user_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL UPDATE trade_in_requests SET reviewed_by_staff_id = " + aid + " WHERE reviewed_by_staff_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('stock_transfers', 'U') IS NOT NULL UPDATE stock_transfers SET requested_by_user_id = " + aid + " WHERE requested_by_user_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('order_status_history', 'U') IS NOT NULL UPDATE order_status_history SET updated_by_user_id = " + aid + " WHERE updated_by_user_id NOT IN (" + idList + ")");
                stmt.execute("IF OBJECT_ID('coupon_usages', 'U') IS NOT NULL UPDATE coupon_usages SET user_id = " + cid + " WHERE user_id NOT IN (" + idList + ")");
            }

            int deleted = stmt.executeUpdate("DELETE FROM users WHERE email NOT IN (" + validEmails + ")");
            logger.info("Đã xóa bỏ {} tài khoản ngoài 5 tài khoản Book4Life tiêu chuẩn.", deleted);
        } catch (Exception ex) {
            logger.warn("Dọn dẹp tài khoản thừa: {}", ex.getMessage());
        }
    }

    private Role getOrCreateRole(String name, String description) {
        return roleRepository.findByRoleName(name)
                .orElseGet(() -> roleRepository.save(new Role(null, name, description)));
    }

    private User createOrUpdateUser(String email, String rawPassword, String fullName, String phone, Role role, Branch branch) {
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setPassword(passwordEncoder.encode(rawPassword));
                    existingUser.setFullName(fullName);
                    existingUser.setPhone(phone);
                    existingUser.setRole(role);
                    existingUser.setBranch(branch);
                    existingUser.setIsActive(true);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPassword(passwordEncoder.encode(rawPassword));
                    newUser.setFullName(fullName);
                    newUser.setPhone(phone);
                    newUser.setRole(role);
                    newUser.setBranch(branch);
                    newUser.setIsActive(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }

    private void ensureDriversAndCouponsSeeded(List<Branch> branches) {
        try {
            // Seed Drivers
            if (driverRepository.count() == 0 && branches != null && !branches.isEmpty()) {
                logger.info("CSDL chưa có danh sách tài xế, đang tạo đội tài xế Book4Life...");
                Branch bHanoi = branches.stream().filter(b -> b.getBranchName().contains("Hoàn Kiếm") || b.getCity().contains("Hà Nội")).findFirst().orElse(branches.get(0));
                Branch bQ1 = branches.stream().filter(b -> b.getBranchName().contains("Quận 1")).findFirst().orElse(branches.get(0));
                Branch bThuDuc = branches.stream().filter(b -> b.getBranchName().contains("Thủ Đức")).findFirst().orElse(branches.size() > 1 ? branches.get(1) : branches.get(0));

                Driver d1 = new Driver(null, "Nguyễn Văn Tuấn", "0987.111.222", "29B1-686.88", "MOTORBIKE", "Đội xe Book4Life HN", bHanoi, "AVAILABLE");
                d1.setRating(4.9);
                Driver d2 = new Driver(null, "Trần Quốc Bảo", "0903.222.333", "59P2-777.99", "MOTORBIKE", "Đội xe Book4Life Q1", bQ1, "AVAILABLE");
                d2.setRating(5.0);
                Driver d3 = new Driver(null, "Lê Hữu Thắng", "0912.333.444", "59X3-555.22", "MOTORBIKE", "Đội xe Book4Life Thủ Đức", bThuDuc, "AVAILABLE");
                d3.setRating(4.8);
                Driver d4 = new Driver(null, "Bùi Minh Quân (Xe Tải)", "0938.444.555", "51D-888.33", "TRUCK", "Book4Life Fast Truck", bQ1, "AVAILABLE");
                d4.setRating(5.0);
                Driver d5 = new Driver(null, "Shipper Đối Tác GHTK", "1900.6092", "GHTK-EXPRESS", "MOTORBIKE", "Giao Hàng Tiết Kiệm", null, "AVAILABLE");
                d5.setRating(4.7);
                Driver d6 = new Driver(null, "Shipper Đối Tác GHN", "1900.636677", "GHN-FAST", "MOTORBIKE", "Giao Hàng Nhanh", null, "AVAILABLE");
                d6.setRating(4.8);

                driverRepository.saveAll(List.of(d1, d2, d3, d4, d5, d6));
                logger.info("Đã tạo thành công 6 tài xế mẫu phục vụ giao hàng chuỗi Book4Life.");
            }

            // Seed Freeship & Discount Coupons
            if (couponRepository.count() == 0) {
                logger.info("CSDL chưa có mã giảm giá, đang tạo các mã Freeship và ưu đãi...");
                LocalDateTime now = LocalDateTime.now();

                Coupon cFreeship = new Coupon();
                cFreeship.setCode("FREESHIP");
                cFreeship.setCouponCategory("SHIPPING");
                cFreeship.setDiscountType("FREESHIP");
                cFreeship.setDiscountValue(new BigDecimal("25000"));
                cFreeship.setMinOrderAmount(BigDecimal.ZERO);
                cFreeship.setMaxDiscountAmount(new BigDecimal("25000"));
                cFreeship.setUsageLimit(500);
                cFreeship.setUsedCount(0);
                cFreeship.setStartDate(now.minusDays(1));
                cFreeship.setEndDate(now.plusMonths(6));
                cFreeship.setIsActive(true);

                Coupon cFreeshipMax = new Coupon();
                cFreeshipMax.setCode("FREESHIPMAX");
                cFreeshipMax.setCouponCategory("SHIPPING");
                cFreeshipMax.setDiscountType("FREESHIP");
                cFreeshipMax.setDiscountValue(new BigDecimal("25000"));
                cFreeshipMax.setMinOrderAmount(new BigDecimal("50000"));
                cFreeshipMax.setMaxDiscountAmount(new BigDecimal("25000"));
                cFreeshipMax.setUsageLimit(1000);
                cFreeshipMax.setUsedCount(0);
                cFreeshipMax.setStartDate(now.minusDays(1));
                cFreeshipMax.setEndDate(now.plusMonths(6));
                cFreeshipMax.setIsActive(true);

                Coupon cShip10k = new Coupon();
                cShip10k.setCode("SHIP10K");
                cShip10k.setCouponCategory("SHIPPING");
                cShip10k.setDiscountType("SHIP_DISCOUNT");
                cShip10k.setDiscountValue(new BigDecimal("10000"));
                cShip10k.setMinOrderAmount(new BigDecimal("50000"));
                cShip10k.setMaxDiscountAmount(new BigDecimal("10000"));
                cShip10k.setUsageLimit(1000);
                cShip10k.setUsedCount(0);
                cShip10k.setStartDate(now.minusDays(1));
                cShip10k.setEndDate(now.plusMonths(6));
                cShip10k.setIsActive(true);

                Coupon cBook10 = new Coupon();
                cBook10.setCode("BOOK10");
                cBook10.setCouponCategory("ORDER_DISCOUNT");
                cBook10.setDiscountType("PERCENT");
                cBook10.setDiscountValue(new BigDecimal("10"));
                cBook10.setMinOrderAmount(new BigDecimal("100000"));
                cBook10.setMaxDiscountAmount(new BigDecimal("50000"));
                cBook10.setUsageLimit(500);
                cBook10.setUsedCount(0);
                cBook10.setStartDate(now.minusDays(1));
                cBook10.setEndDate(now.plusMonths(6));
                cBook10.setIsActive(true);

                Coupon cTietKiem = new Coupon();
                cTietKiem.setCode("TIETKIEM");
                cTietKiem.setCouponCategory("ORDER_DISCOUNT");
                cTietKiem.setDiscountType("FIXED_AMOUNT");
                cTietKiem.setDiscountValue(new BigDecimal("20000"));
                cTietKiem.setMinOrderAmount(new BigDecimal("150000"));
                cTietKiem.setMaxDiscountAmount(new BigDecimal("20000"));
                cTietKiem.setUsageLimit(500);
                cTietKiem.setUsedCount(0);
                cTietKiem.setStartDate(now.minusDays(1));
                cTietKiem.setEndDate(now.plusMonths(6));
                cTietKiem.setIsActive(true);

                couponRepository.saveAll(List.of(cFreeship, cFreeshipMax, cShip10k, cBook10, cTietKiem));
                logger.info("Đã tạo thành công các mã Freeship & Khuyến mãi (FREESHIP, FREESHIPMAX, SHIP10K, BOOK10, TIETKIEM).");
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khởi tạo drivers/coupons: {}", ex.getMessage());
        }
    }

    private void ensureSampleReviewsSeeded(User customerUser) {
        try {
            if (reviewRepository.count() == 0 && customerUser != null) {
                List<Book> books = bookRepository.findAll();
                if (!books.isEmpty()) {
                    Book book1 = books.get(0);
                    Review r1 = new Review();
                    r1.setBook(book1);
                    r1.setUser(customerUser);
                    r1.setRating(5);
                    r1.setTitle("Sách Like New rất đẹp, khử trùng sạch sẽ!");
                    r1.setComment("Sách cũ tình trạng nguyên vẹn, gáy sách phẳng phiu, trang giấy đẹp và được bọc màng co cẩn thận. Giao hàng từ chi nhánh nhanh bất ngờ. Chắc chắn sẽ tiếp tục ủng hộ Book4Life!");
                    r1.setIsVerifiedPurchase(true);
                    r1.setCreatedAt(LocalDateTime.now().minusDays(2));

                    Review r2 = new Review();
                    r2.setBook(book1);
                    r2.setUser(customerUser);
                    r2.setRating(5);
                    r2.setTitle("Tuyệt phẩm kinh điển, giá tiết kiệm");
                    r2.setComment("Một ấn bản tuyệt vời để bổ sung vào tủ sách gia đình. Giá sách cũ rẻ hơn một nửa so với giá bìa mới mà chất lượng gần như mới tinh. 5 sao cho Book4Life!");
                    r2.setIsVerifiedPurchase(true);
                    r2.setCreatedAt(LocalDateTime.now().minusDays(1));

                    if (books.size() > 1) {
                        Book book2 = books.get(1);
                        Review r3 = new Review();
                        r3.setBook(book2);
                        r3.setUser(customerUser);
                        r3.setRating(5);
                        r3.setTitle("Chất lượng sách vượt mong đợi");
                        r3.setComment("Giấy thơm mùi sách, nét chữ rõ ràng, không bị ố vàng hay quăn góc. Quy trình thẩm định sách cũ của cửa hàng rất uy tín!");
                        r3.setIsVerifiedPurchase(true);
                        r3.setCreatedAt(LocalDateTime.now().minusDays(3));
                        reviewRepository.saveAll(List.of(r1, r2, r3));
                    } else {
                        reviewRepository.saveAll(List.of(r1, r2));
                    }
                    logger.info("Đã khởi tạo thành công các đánh giá 5 sao mẫu cho sách.");
                }
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khởi tạo đánh giá mẫu: {}", ex.getMessage());
        }
    }

    private void cleanUpAllOldOrders() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            logger.info("Đang thực hiện xóa sạch tất cả đơn hàng cũ theo yêu cầu...");
            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            } catch (Exception ignored) {}

            try {
                stmt.executeUpdate("DELETE FROM order_status_history");
            } catch (Exception e) {
                logger.debug("Lỗi xóa order_status_history: {}", e.getMessage());
            }

            try {
                stmt.executeUpdate("DELETE FROM sub_order_items");
            } catch (Exception e) {
                logger.debug("Lỗi xóa sub_order_items: {}", e.getMessage());
            }

            try {
                stmt.executeUpdate("DELETE FROM coupon_usages");
            } catch (Exception e) {
                logger.debug("Lỗi xóa coupon_usages: {}", e.getMessage());
            }

            try {
                stmt.executeUpdate("DELETE FROM sub_orders");
            } catch (Exception e) {
                logger.debug("Lỗi xóa sub_orders: {}", e.getMessage());
            }

            try {
                stmt.executeUpdate("DELETE FROM master_orders");
            } catch (Exception e) {
                logger.debug("Lỗi xóa master_orders: {}", e.getMessage());
            }

            try {
                stmt.executeUpdate("UPDATE drivers SET active_orders_count = 0");
            } catch (Exception ignored) {}

            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ignored) {}

            logger.info("Đã xóa sạch toàn bộ các đơn hàng cũ thành công!");
        } catch (Exception ex) {
            logger.warn("Cảnh báo khi xóa đơn hàng cũ: {}", ex.getMessage());
        }
    }
}

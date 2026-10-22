package com.bookstore.config;

import com.bookstore.entity.Branch;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BranchRepository branchRepository,
                           PasswordEncoder passwordEncoder,
                           DataSource dataSource) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        logger.info("Checking and initializing standard SRS Roles and Users...");

        // 0. Ensure otp_verifications table exists via direct JDBC
        ensureOtpTableExists();

        // 1. Initialize 5 Roles from SRS
        Role roleAdmin = getOrCreateRole("ADMIN", "Quản trị viên toàn hệ thống - Toàn quyền cấu hình & quản lý");
        Role roleManager = getOrCreateRole("BRANCH_MANAGER", "Quản lý chi nhánh - Điều phối nhân viên & tồn kho chi nhánh");
        Role roleWarehouse = getOrCreateRole("WAREHOUSE_STAFF", "Nhân viên thu mua & kho - Đánh giá, định giá và nhập kho sách cũ");
        Role roleCashier = getOrCreateRole("CASHIER", "Nhân viên bán hàng - Thu ngân tại quầy POS & xử lý đơn online");
        Role roleCustomer = getOrCreateRole("CUSTOMER", "Khách hàng độc giả - Tìm kiếm, đặt mua sách và quản lý tài khoản");

        // 2. Fetch sample branches if available
        List<Branch> branches = branchRepository.findAll();
        Branch branch1 = branches.size() > 0 ? branches.get(0) : null;
        Branch branch2 = branches.size() > 1 ? branches.get(1) : branch1;

        // 3. Initialize 5 Standard Accounts (Default Password: "1234@abc")
        User adminUser = createOrUpdateUser("admin.book4life@gmail.com", "1234@abc", "Nguyễn Văn Admin", "0901234567", roleAdmin, null);
        User managerUser = createOrUpdateUser("manage.book4life@gmail.com", "1234@abc", "Trần Thị Quản Lý (HN)", "0902345678", roleManager, branch1);
        User warehouseUser = createOrUpdateUser("warehouse.book4life@gmail.com", "1234@abc", "Lê Văn Thủ Kho", "0903456789", roleWarehouse, branch1);
        User cashierUser = createOrUpdateUser("cashier.book4life@gmail.com", "1234@abc", "Phạm Thu Ngân", "0904567890", roleCashier, branch2);
        User customerUser = createOrUpdateUser("user.book4life@gmail.com", "1234@abc", "Hoàng Độc Giả", "0905678901", roleCustomer, null);

        // 4. Xóa bỏ tất cả các tài khoản ngoài 5 tài khoản được chỉ định
        cleanUpExtraneousUsers(adminUser.getId(), managerUser.getId(), warehouseUser.getId(), cashierUser.getId(), customerUser.getId());

        // 5. Đảm bảo ID của 5 tài khoản luôn cố định là 1..5
        ensureStandardUserIds();

        logger.info("DataInitializer completed successfully. 5 Roles & default users are ready!");
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

            logger.info("Renumbering standard user accounts to exact IDs 1..5...");

            String sql =
                "BEGIN TRANSACTION;\n" +
                "IF OBJECT_ID('fk_trade_in_user', 'F') IS NOT NULL ALTER TABLE trade_in_requests DROP CONSTRAINT fk_trade_in_user;\n" +
                "IF OBJECT_ID('fk_trade_in_staff', 'F') IS NOT NULL ALTER TABLE trade_in_requests DROP CONSTRAINT fk_trade_in_staff;\n" +
                "IF OBJECT_ID('fk_transfers_user', 'F') IS NOT NULL ALTER TABLE stock_transfers DROP CONSTRAINT fk_transfers_user;\n" +
                "IF OBJECT_ID('fk_wishlists_user', 'F') IS NOT NULL ALTER TABLE wishlists DROP CONSTRAINT fk_wishlists_user;\n" +
                "IF OBJECT_ID('fk_reviews_user', 'F') IS NOT NULL ALTER TABLE reviews DROP CONSTRAINT fk_reviews_user;\n" +
                "IF OBJECT_ID('fk_chat_sender', 'F') IS NOT NULL ALTER TABLE chat_messages DROP CONSTRAINT fk_chat_sender;\n" +
                "IF OBJECT_ID('fk_chat_receiver', 'F') IS NOT NULL ALTER TABLE chat_messages DROP CONSTRAINT fk_chat_receiver;\n" +
                "IF OBJECT_ID('FK_osh_user', 'F') IS NOT NULL ALTER TABLE order_status_history DROP CONSTRAINT FK_osh_user;\n" +
                "IF OBJECT_ID('fk_ai_chat_user', 'F') IS NOT NULL ALTER TABLE ai_chat_messages DROP CONSTRAINT fk_ai_chat_user;\n" +
                "IF OBJECT_ID('fk_master_orders_user', 'F') IS NOT NULL ALTER TABLE master_orders DROP CONSTRAINT fk_master_orders_user;\n" +
                "IF OBJECT_ID('fk_coupon_usages_user', 'F') IS NOT NULL ALTER TABLE coupon_usages DROP CONSTRAINT fk_coupon_usages_user;\n" +
                "\n" +
                "IF OBJECT_ID('master_orders', 'U') IS NOT NULL UPDATE master_orders SET user_id = 5 WHERE user_id NOT IN (1,2,3,4,5);\n" +
                "IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL BEGIN UPDATE trade_in_requests SET user_id = 5 WHERE user_id NOT IN (1,2,3,4,5); UPDATE trade_in_requests SET reviewed_by_staff_id = 1 WHERE reviewed_by_staff_id IS NOT NULL AND reviewed_by_staff_id NOT IN (1,2,3,4,5); END\n" +
                "IF OBJECT_ID('stock_transfers', 'U') IS NOT NULL UPDATE stock_transfers SET requested_by_user_id = 1 WHERE requested_by_user_id NOT IN (1,2,3,4,5);\n" +
                "IF OBJECT_ID('wishlists', 'U') IS NOT NULL UPDATE wishlists SET user_id = 5 WHERE user_id NOT IN (1,2,3,4,5);\n" +
                "IF OBJECT_ID('order_status_history', 'U') IS NOT NULL UPDATE order_status_history SET updated_by_user_id = 1 WHERE updated_by_user_id NOT IN (1,2,3,4,5);\n" +
                "IF OBJECT_ID('ai_chat_messages', 'U') IS NOT NULL UPDATE ai_chat_messages SET user_id = 5 WHERE user_id IS NOT NULL AND user_id NOT IN (1,2,3,4,5);\n" +
                "\n" +
                "SELECT \n" +
                "    CASE \n" +
                "        WHEN email = 'admin.book4life@gmail.com' THEN 1\n" +
                "        WHEN email = 'manage.book4life@gmail.com' THEN 2\n" +
                "        WHEN email = 'warehouse.book4life@gmail.com' THEN 3\n" +
                "        WHEN email = 'cashier.book4life@gmail.com' THEN 4\n" +
                "        WHEN email = 'user.book4life@gmail.com' THEN 5\n" +
                "    END AS new_id,\n" +
                "    email, password, full_name, phone, avatar_url, role_id, branch_id, is_active, created_at\n" +
                "INTO #temp_users_init\n" +
                "FROM users\n" +
                "WHERE email IN (\n" +
                "    'admin.book4life@gmail.com',\n" +
                "    'manage.book4life@gmail.com',\n" +
                "    'warehouse.book4life@gmail.com',\n" +
                "    'cashier.book4life@gmail.com',\n" +
                "    'user.book4life@gmail.com'\n" +
                ");\n" +
                "\n" +
                "DELETE FROM users;\n" +
                "\n" +
                "SET IDENTITY_INSERT users ON;\n" +
                "\n" +
                "INSERT INTO users (id, email, password, full_name, phone, avatar_url, role_id, branch_id, is_active, created_at)\n" +
                "SELECT new_id, email, password, full_name, phone, avatar_url, role_id, branch_id, is_active, created_at\n" +
                "FROM #temp_users_init\n" +
                "ORDER BY new_id ASC;\n" +
                "\n" +
                "SET IDENTITY_INSERT users OFF;\n" +
                "\n" +
                "DROP TABLE #temp_users_init;\n" +
                "\n" +
                "DBCC CHECKIDENT ('users', RESEED, 5);\n" +
                "\n" +
                "IF OBJECT_ID('trade_in_requests', 'U') IS NOT NULL BEGIN ALTER TABLE trade_in_requests ADD CONSTRAINT fk_trade_in_user FOREIGN KEY (user_id) REFERENCES users(id); ALTER TABLE trade_in_requests ADD CONSTRAINT fk_trade_in_staff FOREIGN KEY (reviewed_by_staff_id) REFERENCES users(id); END\n" +
                "IF OBJECT_ID('stock_transfers', 'U') IS NOT NULL ALTER TABLE stock_transfers ADD CONSTRAINT fk_transfers_user FOREIGN KEY (requested_by_user_id) REFERENCES users(id);\n" +
                "IF OBJECT_ID('wishlists', 'U') IS NOT NULL ALTER TABLE wishlists ADD CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id);\n" +
                "IF OBJECT_ID('reviews', 'U') IS NOT NULL ALTER TABLE reviews ADD CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id);\n" +
                "IF OBJECT_ID('chat_messages', 'U') IS NOT NULL BEGIN ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id); ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_receiver FOREIGN KEY (receiver_id) REFERENCES users(id); END\n" +
                "IF OBJECT_ID('order_status_history', 'U') IS NOT NULL ALTER TABLE order_status_history ADD CONSTRAINT FK_osh_user FOREIGN KEY (updated_by_user_id) REFERENCES users(id);\n" +
                "IF OBJECT_ID('ai_chat_messages', 'U') IS NOT NULL ALTER TABLE ai_chat_messages ADD CONSTRAINT fk_ai_chat_user FOREIGN KEY (user_id) REFERENCES users(id);\n" +
                "IF OBJECT_ID('master_orders', 'U') IS NOT NULL ALTER TABLE master_orders ADD CONSTRAINT fk_master_orders_user FOREIGN KEY (user_id) REFERENCES users(id);\n" +
                "IF OBJECT_ID('coupon_usages', 'U') IS NOT NULL ALTER TABLE coupon_usages ADD CONSTRAINT fk_coupon_usages_user FOREIGN KEY (user_id) REFERENCES users(id);\n" +
                "\n" +
                "COMMIT TRANSACTION;";

            stmt.execute(sql);
            logger.info("Standard user accounts have been successfully verified/renumbered to IDs 1..5!");
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
                    "  attempts INT DEFAULT 0," +
                    "  is_used BOOLEAN DEFAULT FALSE," +
                    "  created_at DATETIME NOT NULL," +
                    "  expires_at DATETIME NOT NULL" +
                    ")"
                );
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
                "    attempts INT DEFAULT 0, " +
                "    is_used BIT DEFAULT 0, " +
                "    created_at DATETIME2 NOT NULL, " +
                "    expires_at DATETIME2 NOT NULL " +
                "  ); " +
                "  CREATE INDEX idx_otp_email_created ON otp_verifications(email, created_at); " +
                "END"
            );
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
                // Reassign foreign keys if tables exist (MSSQL T-SQL syntax)
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

    private Role getOrCreateRole(String roleName, String description) {
        return roleRepository.findByRoleName(roleName).orElseGet(() -> {
            Role role = Role.builder()
                    .roleName(roleName)
                    .description(description)
                    .build();
            return roleRepository.save(role);
        });
    }

    private User createOrUpdateUser(String email, String rawPassword, String fullName, String phone, Role role, Branch branch) {
        return userRepository.findByEmail(email).map(user -> {
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(role);
            if (branch != null) {
                user.setBranch(branch);
            }
            user.setIsActive(true);
            return userRepository.save(user);
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .phone(phone)
                    .role(role)
                    .branch(branch)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });
    }
}

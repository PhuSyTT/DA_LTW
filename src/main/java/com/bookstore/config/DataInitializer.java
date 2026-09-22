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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BranchRepository branchRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Checking and initializing standard SRS Roles and Users...");

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

        // 3. Initialize 5 Sample Users for each Role (Default Password: "123456")
        createOrUpdateUser("admin@oldbooks.vn", "123456", "Nguyễn Văn Admin", "0901234567", roleAdmin, null);
        createOrUpdateUser("manager.hn@oldbooks.vn", "123456", "Trần Thị Quản Lý (HN)", "0902345678", roleManager, branch1);
        createOrUpdateUser("warehouse@oldbooks.vn", "123456", "Lê Văn Thủ Kho", "0903456789", roleWarehouse, branch1);
        createOrUpdateUser("cashier@oldbooks.vn", "123456", "Phạm Thu Ngân", "0904567890", roleCashier, branch2);
        createOrUpdateUser("customer@gmail.com", "123456", "Hoàng Độc Giả", "0905678901", roleCustomer, null);

        logger.info("DataInitializer completed successfully. 5 Roles & default users are ready!");
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

    private void createOrUpdateUser(String email, String rawPassword, String fullName, String phone, Role role, Branch branch) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    // Update password and role to ensure login works
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setRole(role);
                    if (branch != null) {
                        user.setBranch(branch);
                    }
                    user.setIsActive(true);
                    userRepository.save(user);
                },
                () -> {
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
                    userRepository.save(newUser);
                }
        );
    }
}

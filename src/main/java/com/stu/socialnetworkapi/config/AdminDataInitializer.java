package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Khởi tạo tài khoản quản trị viên mặc định khi ứng dụng khởi động lần đầu.
 * Nếu tài khoản admin đã tồn tại, bước này sẽ bị bỏ qua.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("constrainsAndIndexInitializer")
public class AdminDataInitializer {

    // ── Thông tin admin mặc định ──────────────────────────────────────────────
    private static final String ADMIN_EMAIL    = "truonggenz2003@gmail.com";
    private static final String ADMIN_PASSWORD = "Admin@123456";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_GIVEN_NAME  = "Super";
    private static final String ADMIN_FAMILY_NAME = "Admin";
    // ─────────────────────────────────────────────────────────────────────────

    private final AccountRepository accountRepository;
    private final PasswordEncoder   passwordEncoder;

    @PostConstruct
    public void seedAdminAccount() {
        boolean adminExists = accountRepository.findByEmail(ADMIN_EMAIL).isPresent();
        if (adminExists) {
            log.info("[AdminInit] Tài khoản admin đã tồn tại – bỏ qua bước khởi tạo.");
            return;
        }

        try {
            UUID adminId = UUID.randomUUID();

            User adminUser = User.builder()
                    .id(adminId)
                    .username(ADMIN_USERNAME)
                    .givenName(ADMIN_GIVEN_NAME)
                    .familyName(ADMIN_FAMILY_NAME)
                    .birthdate(LocalDate.of(1990, 1, 1))
                    .build();

            Account adminAccount = Account.builder()
                    .id(adminId)
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(AccountRole.ADMIN)
                    .isVerified(true)          // admin không cần xác minh email
                    .user(adminUser)
                    .build();

            accountRepository.save(adminAccount);

            log.info("╔══════════════════════════════════════════════════╗");
            log.info("║       TÀI KHOẢN ADMIN MẶC ĐỊNH ĐÃ ĐƯỢC TẠO      ║");
            log.info("╠══════════════════════════════════════════════════╣");
            log.info("║  Email    : {}                     ║", ADMIN_EMAIL);
            log.info("║  Password : {}                          ║", ADMIN_PASSWORD);
            log.info("║  Role     : ADMIN                                ║");
            log.info("╠══════════════════════════════════════════════════╣");
            log.info("║  ⚠  Hãy đổi mật khẩu ngay sau lần đăng nhập    ║");
            log.info("║     đầu tiên để đảm bảo an toàn hệ thống!       ║");
            log.info("╚══════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("[AdminInit] Không thể tạo tài khoản admin mặc định: {}", e.getMessage(), e);
        }
    }
}
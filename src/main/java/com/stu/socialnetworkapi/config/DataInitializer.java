package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Admin mặc định ──────────────────────────────────────────────
    private static final String ADMIN_EMAIL       = "admin@gmail.com";
    private static final String ADMIN_PASSWORD    = "Admin@123";
    private static final String ADMIN_GIVEN_NAME  = "Admin";
    private static final String ADMIN_FAMILY_NAME = "Default";
    private static final String ADMIN_USERNAME    = "admin";

    // ── User mặc định ───────────────────────────────────────────────
    private static final String USER_EMAIL        = "user@gmail.com";
    private static final String USER_PASSWORD     = "User@123";
    private static final String USER_GIVEN_NAME   = "User";
    private static final String USER_FAMILY_NAME  = "Default";
    private static final String USER_USERNAME     = "defaultuser";

    @Override
    public void run(ApplicationArguments args) {
        initAdmin();
        initDefaultUser();
    }

    private void initAdmin() {
        if (accountRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            log.info("ℹ️  Tài khoản admin đã tồn tại, bỏ qua khởi tạo.");
            return;
        }

        // Account.id và User.id phải cùng UUID — theo convention của RegisterServiceImpl
        UUID sharedId = UUID.randomUUID();

        User adminUser = User.builder()
                .id(sharedId)
                .givenName(ADMIN_GIVEN_NAME)
                .familyName(ADMIN_FAMILY_NAME)
                .username(ADMIN_USERNAME)
                .birthdate(LocalDate.of(2000, 1, 1))
                .build();

        Account admin = Account.builder()
                .id(sharedId)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(AccountRole.ADMIN)
                .isVerified(true)
                .user(adminUser)
                .build();

        accountRepository.save(admin);
        log.info("✅ Đã tạo tài khoản admin mặc định: {}", ADMIN_EMAIL);
    }

    private void initDefaultUser() {
        if (accountRepository.findByEmail(USER_EMAIL).isPresent()) {
            log.info("ℹ️  Tài khoản user mặc định đã tồn tại, bỏ qua khởi tạo.");
            return;
        }

        // Account.id và User.id phải cùng UUID — theo convention của RegisterServiceImpl
        UUID sharedId = UUID.randomUUID();

        User userInfo = User.builder()
                .id(sharedId)
                .givenName(USER_GIVEN_NAME)
                .familyName(USER_FAMILY_NAME)
                .username(USER_USERNAME)
                .birthdate(LocalDate.of(2000, 1, 1))
                .build();

        Account user = Account.builder()
                .id(sharedId)
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .role(AccountRole.USER)
                .isVerified(true)
                .user(userInfo)
                .build();

        accountRepository.save(user);
        log.info("✅ Đã tạo tài khoản user mặc định: {}", USER_EMAIL);
    }
}
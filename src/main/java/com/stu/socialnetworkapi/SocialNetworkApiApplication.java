package com.stu.socialnetworkapi;

import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@EnableRetry
@EnableAsync
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class SocialNetworkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialNetworkApiApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAdmin(AccountRepository accountRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@admin.com";

            boolean exists = accountRepository.findByEmailAndRoleIs(adminEmail, AccountRole.ADMIN)
                    .isPresent();

            if (exists) {
                log.info("[Seed] Admin account already exists — skipping.");
                return;
            }

            UUID id = UUID.randomUUID();

            User user = User.builder()
                    .id(id)
                    .username("admin")
                    .givenName("Admin")
                    .familyName("System")
                    .birthdate(LocalDate.of(2000, 1, 1))
                    .build();

            Account admin = Account.builder()
                    .id(id)
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(AccountRole.ADMIN)
                    .isVerified(true)
                    .user(user)
                    .build();

            accountRepository.save(admin);
            log.info("[Seed] Admin account created — email: {}", adminEmail);
        };
    }
}
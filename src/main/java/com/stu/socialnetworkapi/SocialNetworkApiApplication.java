package com.stu.socialnetworkapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

@EnableRetry
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SocialNetworkApiApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        System.setProperty("NEO4J_USER", dotenv.get("NEO4J_USER"));
        System.setProperty("NEO4J_PASS", dotenv.get("NEO4J_PASS"));
        System.setProperty("NEO4J_URI", dotenv.get("NEO4J_URI"));

        System.setProperty("JWT_ACCESS_TOKEN_KEY", dotenv.get("JWT_ACCESS_TOKEN_KEY"));
        System.setProperty("JWT_ACCESS_TOKEN_DURATION", dotenv.get("JWT_ACCESS_TOKEN_DURATION"));
        System.setProperty("JWT_REFRESH_TOKEN_DURATION", dotenv.get("JWT_REFRESH_TOKEN_DURATION"));

        System.setProperty("VERIFY_EMAIL_DURATION", dotenv.get("VERIFY_EMAIL_DURATION"));

        System.setProperty("MAIL", dotenv.get("MAIL"));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));

        System.setProperty("FRONTEND_ORIGIN", dotenv.get("FRONTEND_ORIGIN"));
        System.setProperty("SELF_ORIGIN", dotenv.get("SELF_ORIGIN"));

        System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
        System.setProperty("REDIS_USERNAME", dotenv.get("REDIS_USERNAME"));
        System.setProperty("REDIS_PASSWORD", dotenv.get("REDIS_PASSWORD"));
        System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT"));

        System.setProperty("STRINGEE_API_SID", dotenv.get("STRINGEE_API_SID"));
        System.setProperty("STRINGEE_API_SECRET_KEY", dotenv.get("STRINGEE_API_SECRET_KEY"));

        System.setProperty("GEMINI_KEY", dotenv.get("GEMINI_KEY"));

        SpringApplication.run(SocialNetworkApiApplication.class, args);
    }
}

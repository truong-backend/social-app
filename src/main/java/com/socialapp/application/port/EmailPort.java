package com.socialapp.application.port;

/**
 * Port (outbound) — Infrastructure sẽ implement bằng JavaMailSender.
 */
public interface EmailPort {
    void sendVerificationEmail(String toEmail, String code);
}
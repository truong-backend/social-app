package com.socialapp.application.shared.port;

/**
 * Outbound Port: EmailSender
 * Được implement ở infrastructure (JavaMailSender / SES / SMTP)
 */
public interface EmailSender {
    void sendVerificationEmail(String toEmail, String verifyCode);
    void sendPasswordResetEmail(String toEmail, String resetCode);
}
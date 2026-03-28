package com.socialapp.infrastructure.email;

import com.socialapp.application.shared.port.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String toEmail, String verifyCode) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("[SocialApp Z] Verify your email");
        msg.setText("Your verification code is: " + verifyCode
                + "\nThis code expires in 15 minutes.");
        send(msg);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("[SocialApp Z] Reset your password");
        msg.setText("Your password reset code is: " + resetCode
                + "\nThis code expires in 15 minutes.");
        send(msg);
    }

    private void send(SimpleMailMessage msg) {
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", msg.getTo(), e.getMessage());
        }
    }
}
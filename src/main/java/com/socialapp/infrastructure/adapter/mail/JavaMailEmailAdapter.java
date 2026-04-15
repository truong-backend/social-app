package com.socialapp.infrastructure.adapter.mail;

import com.socialapp.application.port.EmailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class JavaMailEmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final String         fromAddress;
    private final String         appName;

    public JavaMailEmailAdapter(JavaMailSender mailSender,
                                @Value("${spring.mail.username}") String fromAddress,
                                @Value("${app.name:SocialApp}") String appName) {
        this.mailSender  = mailSender;
        this.fromAddress = fromAddress;
        this.appName     = appName;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("[" + appName + "] Xác thực tài khoản của bạn");
            helper.setText(buildHtml(code), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email to: " + toEmail, e);
        }
    }

    // ── HTML template ─────────────────────────────────────────

    private String buildHtml(String code) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
              <h2 style="color: #4A90D9;">Xác thực tài khoản %s</h2>
              <p>Xin chào,</p>
              <p>Mã xác thực của bạn là:</p>
              <div style="
                font-size: 32px;
                font-weight: bold;
                letter-spacing: 8px;
                color: #333;
                background: #f5f5f5;
                padding: 16px 24px;
                border-radius: 8px;
                text-align: center;
                margin: 24px 0;
              ">%s</div>
              <p style="color: #888;">Mã này sẽ hết hạn sau <strong>15 phút</strong>.</p>
              <p style="color: #888;">Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
            </body>
            </html>
            """.formatted(appName, code);
    }
}
package com.restaurant.pos.auth.service;

import com.restaurant.pos.common.exception.EmailDeliveryException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.annotation.Async;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.otp.log-code:false}")
    private boolean logOtpCode;

    @PostConstruct
    void logMailConfiguration() {
        log.info("Mail configuration loaded. host={}, port={}, usernameConfigured={}, passwordConfigured={}",
                safe(mailHost),
                mailPort,
                !isBlank(fromEmail),
                !isBlank(mailPassword));
    }

    public void sendOtpEmail(String toEmail, String otp) {
        validateMailConfiguration();
        log.info("Sending OTP email to {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your CafeQR verification code");
            message.setText("""
                    Welcome to CafeQR!

                    Your one-time password (OTP) is: %s

                    This OTP is valid for 5 minutes.
                    """.formatted(otp));

            logOtpForDebugging(toEmail, otp);

            emailSender.send(message);
            log.info("OTP email successfully sent to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send OTP email to {} using {}:{} as {}: {}",
                    toEmail,
                    safe(mailHost),
                    mailPort,
                    safe(fromEmail),
                    e.getMessage(),
                    e);
            throw new EmailDeliveryException(buildOtpDeliveryFailureMessage(e), e);
        }
    }

    @Async
    public void sendTableQREmail(String toEmail, String tableNumber, String qrLink) {
        // Log the link prominently in the terminal as per "login/signup" strategy
        System.out.println("\n[MAIL SIMULATOR] ------------------------------------------------");
        System.out.println("[MAIL SIMULATOR] TABLE: " + tableNumber);
        System.out.println("[MAIL SIMULATOR] TO: " + toEmail);
        System.out.println("[MAIL SIMULATOR] LINK: " + qrLink);
        System.out.println("[MAIL SIMULATOR] ------------------------------------------------\n");
        
        log.info("Requested QR email for Table {} to {}. LINK: {}", tableNumber, toEmail, qrLink);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Table " + tableNumber + " Digital Access - Cafe-QR");
            message.setText("Hello,\n\nYour digital menu access link for Table " + tableNumber + " is: " + qrLink + "\n\nUse this link to browse the menu and place orders.");

            emailSender.send(message);
            log.info("QR email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("FAILED to send QR email to {}: {}. !!! LINK IS LOGGED ABOVE !!!", toEmail, e.getMessage());
        }
    }

    private void validateMailConfiguration() {
        if (isBlank(mailHost) || isBlank(fromEmail) || isBlank(mailPassword)) {
            log.error("SMTP is not fully configured. hostConfigured={}, usernameConfigured={}, passwordConfigured={}",
                    !isBlank(mailHost),
                    !isBlank(fromEmail),
                    !isBlank(mailPassword));
            throw new EmailDeliveryException("Email delivery is not configured. Please set SMTP_USERNAME and SMTP_PASSWORD on the backend.");
        }
    }

    private void logOtpForDebugging(String toEmail, String otp) {
        if (!logOtpCode) {
            return;
        }

        log.warn("OTP debug logging is enabled. Disable OTP_LOG_CODE in production.");
        System.out.println("\n[OTP DEBUG] ------------------------------------------------");
        System.out.println("[OTP DEBUG] TO: " + toEmail);
        System.out.println("[OTP DEBUG] CODE: " + otp);
        System.out.println("[OTP DEBUG] ------------------------------------------------\n");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return isBlank(value) ? "<not configured>" : value;
    }

    private String buildOtpDeliveryFailureMessage(MailException e) {
        if (isStandardSmtpPort(mailPort) && looksLikeConnectionFailure(e)) {
            return "Unable to connect to the SMTP server. If this backend is running on Render free, SMTP ports 25, 465, and 587 are blocked. Use a paid Render instance or an email provider that supports HTTPS API delivery or SMTP on port 2525.";
        }
        return "Unable to send verification email. Please check SMTP settings and try again.";
    }

    private boolean isStandardSmtpPort(int port) {
        return port == 25 || port == 465 || port == 587;
    }

    private boolean looksLikeConnectionFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("connect") || lower.contains("timed out") || lower.contains("connection refused")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}

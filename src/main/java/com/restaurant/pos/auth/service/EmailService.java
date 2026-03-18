package com.restaurant.pos.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.annotation.Async;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username:testcafeqr@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Requested OTP email to {}", toEmail);

        // Email sending logic below

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Cafe-QR Registration OTP");
            message.setText("Welcome to Cafe-QR!\n\nYour one-time password (OTP) for registration is: " + otp + "\n\nThis OTP is valid for 5 minutes.");

            emailSender.send(message);
            log.info("OTP email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("FAILED to send OTP email to {}: {}. !!! CHECK TERMINAL LOGS FOR THE OTP !!!", toEmail, e.getMessage());
            // We do NOT throw an exception here so the user can still register by looking at the backend logs
        }
    }
}

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

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Cafe-QR Registration OTP");
            message.setText("Welcome to Cafe-QR!\n\nYour one-time password (OTP) for registration is: " + otp + "\n\nThis OTP is valid for 5 minutes.");

            // Log the OTP to console for local development convenience
            System.out.println("\n[OTP DEBUG] ------------------------------------------------");
            System.out.println("[OTP DEBUG] TO: " + toEmail);
            System.out.println("[OTP DEBUG] CODE: " + otp);
            System.out.println("[OTP DEBUG] ------------------------------------------------\n");

            emailSender.send(message);
            log.info("OTP email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("FAILED to send OTP email to {}: {}", toEmail, e.getMessage());
            // We do NOT throw an exception here so the user can still register by looking at the backend logs
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
}

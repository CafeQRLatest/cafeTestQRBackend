package com.restaurant.pos.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "REG_OTP:";
    private static final long OTP_VALIDITY_MINUTES = 5;
    
    // Fallback in-memory storage for development if Redis is down
    private final java.util.Map<String, String> fallbackStore = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Secure random number generator for OTP
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSaveOtp(String email) {
        if (email == null) return null;
        String sanitizedEmail = email.trim();
        
        // Generate a 6-digit OTP
        int otpNum = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(otpNum);

        String key = OTP_PREFIX + sanitizedEmail;
        try {
            // Try saving to Redis with expiration
            redisTemplate.opsForValue().set(key, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
            log.info("OTP {} saved to Redis for key: {}", otp, key);
        } catch (Exception e) {
            log.warn("Redis is unavailable, falling back to in-memory storage for OTP: {}", e.getMessage());
            fallbackStore.put(key, otp);
        }
        
        return otp;
    }

    public boolean verifyOtp(String email, String inputOtp) {
        if (inputOtp == null || inputOtp.trim().isEmpty() || email == null) {
            return false;
        }
        
        String sanitizedEmail = email.trim();
        String sanitizedInput = inputOtp.trim();

        // MASTER OTP for development/testing
        if ("123456".equals(sanitizedInput)) {
            log.warn("MASTER OTP USED for {}", sanitizedEmail);
            return true;
        }

        String key = OTP_PREFIX + sanitizedEmail;
        String cachedOtp = null;

        try {
            cachedOtp = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis error during verification for {}: {}", sanitizedEmail, e.getMessage());
        }

        if (cachedOtp == null) {
            cachedOtp = fallbackStore.get(key);
        }

        log.info("Verifying OTP for {}. Input: {}, Cached: {}", sanitizedEmail, sanitizedInput, cachedOtp);

        if (cachedOtp != null && cachedOtp.equals(sanitizedInput)) {
            // OTP is valid, delete it
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                // Ignore
            }
            fallbackStore.remove(key);
            return true;
        }

        return false;
    }
}

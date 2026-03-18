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
        // Generate a 6-digit OTP
        int otpNum = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(otpNum);

        String key = OTP_PREFIX + email;
        try {
            // Try saving to Redis with expiration
            redisTemplate.opsForValue().set(java.util.Objects.requireNonNull(key), java.util.Objects.requireNonNull(otp), OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
            log.info("OTP saved to Redis for {}", email);
        } catch (Exception e) {
            log.warn("Redis is unavailable, falling back to in-memory storage for OTP: {}", e.getMessage());
            fallbackStore.put(key, otp);
            // In a real system we'd use a scheduled task to expire this, but for dev this is fine
        }
        
        log.info("Generated new OTP for {} (valid for {} minutes)", email, OTP_VALIDITY_MINUTES);
        return otp;
    }

    public boolean verifyOtp(String email, String inputOtp) {
        if (inputOtp == null || inputOtp.trim().isEmpty()) {
            return false;
        }

        String key = OTP_PREFIX + email;
        String cachedOtp = null;

        try {
            cachedOtp = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis is unavailable during verification, checking in-memory storage");
        }

        if (cachedOtp == null) {
            cachedOtp = fallbackStore.get(key);
        }

        if (cachedOtp != null && cachedOtp.equals(inputOtp)) {
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

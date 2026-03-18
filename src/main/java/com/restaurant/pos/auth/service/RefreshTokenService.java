package com.restaurant.pos.auth.service;

import com.restaurant.pos.auth.domain.RefreshToken;
import com.restaurant.pos.auth.domain.User;
import com.restaurant.pos.auth.repository.RefreshTokenRepository;
import com.restaurant.pos.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .clientId(user.getClientId())
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusNanos(refreshExpiration * 1000000))
                .createdByIp(ipAddress)
                .userAgent(userAgent)
                .build();

        return refreshTokenRepository.save(java.util.Objects.requireNonNull(refreshToken));
    }

    @Transactional
    public RefreshToken verifyAndRotate(String token, String ipAddress, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            // GRACE PERIOD: If the token was rotated within the last 60 seconds,
            // it's likely a parallel request that started before the first refresh completed.
            // Return the replacement token instead of revoking the entire session.
            LocalDateTime revokedAt = refreshToken.getRevokedAt();
            String replacedBy = refreshToken.getReplacedByToken();
            
            if (revokedAt != null && replacedBy != null 
                    && revokedAt.plusSeconds(60).isAfter(LocalDateTime.now())) {
                log.info("Refresh token grace period: returning replacement token for user {}", 
                        refreshToken.getUser().getEmail());
                RefreshToken replacement = refreshTokenRepository.findByToken(replacedBy)
                        .orElse(null);
                if (replacement != null && !replacement.isRevoked() && !replacement.isExpired()) {
                    return replacement;
                }
            }
            
            // Outside grace period or replacement not found => genuine reuse attack
            revokeAllTokensForUser(refreshToken.getUser());
            throw new BusinessException("Refresh token has been revoked due to suspected reuse");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token was expired. Please make a new signin request");
        }

        // Rotate: Revoke old, create new
        refreshToken.setRevokedAt(LocalDateTime.now());
        RefreshToken newToken = createRefreshToken(refreshToken.getUser(), ipAddress, userAgent);
        refreshToken.setReplacedByToken(newToken.getToken());
        refreshTokenRepository.save(refreshToken);

        return newToken;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}

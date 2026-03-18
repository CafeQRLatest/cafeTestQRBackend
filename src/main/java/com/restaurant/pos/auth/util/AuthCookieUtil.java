package com.restaurant.pos.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AuthCookieUtil {

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public void createAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // We use manual Set-Cookie headers to ensure SameSite=Lax and HttpOnly are set correctly 
        // without duplicate headers from response.addCookie()
        
        // access_token - Max-Age is slightly longer than JWT to let backend handle expiry exception
        long accessMaxAge = (jwtExpiration / 1000) + 300; // JWT + 5 minutes
        long refreshMaxAge = refreshExpiration / 1000;

        // access_token
        String accessCookieHeader = String.format(
            "access_token=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d", 
            accessToken, accessMaxAge
        );
        
        // token (alias)
        String tokenAliasHeader = String.format(
            "token=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d", 
            accessToken, accessMaxAge
        );
        
        // refresh_token
        String refreshCookieHeader = String.format(
            "refresh_token=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d", 
            refreshToken, refreshMaxAge
        );

        // Add each header individually
        response.addHeader("Set-Cookie", accessCookieHeader);
        response.addHeader("Set-Cookie", tokenAliasHeader);
        response.addHeader("Set-Cookie", refreshCookieHeader);
        
        System.out.println("===> [DEBUG] AuthCookieUtil: Set-Cookie headers added for access_token, token, and refresh_token");
    }

    public void clearAuthCookies(HttpServletResponse response) {
        // To clear, we set Max-Age=0
        response.addHeader("Set-Cookie", "access_token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
        response.addHeader("Set-Cookie", "token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
        response.addHeader("Set-Cookie", "refresh_token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
        
        System.out.println("===> [DEBUG] AuthCookieUtil: Set-Cookie headers added to CLEAR cookies");
    }
}

package com.zoonza.sns.member.internal.adapter.in.support;

import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(CookieProperties.class)
public class TokenCookieManager {
    private final CookieProperties properties;

    public ResponseCookie createRefreshTokenCookie(RefreshToken refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken.value())
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(refreshToken.ttl())
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}

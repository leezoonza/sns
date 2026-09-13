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
    private static final String COOKIE_NAME = "refreshToken";
    private static final String SAME_SITE = "Lax";
    private static final String PATH = "/api/auth";

    private final CookieProperties properties;

    public ResponseCookie createRefreshTokenCookie(RefreshToken refreshToken) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken.value())
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(SAME_SITE)
                .path(PATH)
                .maxAge(refreshToken.ttl())
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(SAME_SITE)
                .path(PATH)
                .maxAge(0)
                .build();
    }
}

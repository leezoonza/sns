package com.zoonza.sns.member.internal.adapter.out.token;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class TokenProviderAdapterTests {
    @Test
    @DisplayName("회원과 역할 및 전달한 발급 시각을 서명된 JWT에 담고 매번 새 토큰을 발급한다")
    void issuesTokens() throws Exception {
        var access = new AccessTokenProperties();
        access.setSecret("test-only-secret-key-at-least-32-bytes-long");
        access.setAccessTokenTtl(Duration.ofMinutes(15));
        var refresh = new RefreshTokenProperties();
        refresh.setRefreshTokenTtl(Duration.ofDays(14));
        var provider = new TokenProviderAdapter(access,
                new JwtAccessTokenGenerator(new JwtEncoderConfig().jwtEncoder(access)),
                refresh, new SecureRefreshTokenGenerator());
        Instant issuedAt = Instant.parse("2026-09-14T00:00:00Z");

        var first = provider.issue(42L, "ADMIN", issuedAt);
        var second = provider.issue(42L, "ADMIN", issuedAt);
        var jwt = SignedJWT.parse(first.accessToken().value());

        assertThat(jwt.verify(new MACVerifier(access.getSecret()))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("42");
        assertThat(jwt.getJWTClaimsSet().getStringClaim("role")).isEqualTo("ADMIN");
        assertThat(jwt.getJWTClaimsSet().getJWTID()).isNotBlank();
        assertThat(jwt.getJWTClaimsSet().getIssueTime().toInstant()).isEqualTo(issuedAt);
        assertThat(jwt.getJWTClaimsSet().getExpirationTime().toInstant()).isEqualTo(issuedAt.plusSeconds(900));
        assertThat(second.accessToken()).isNotEqualTo(first.accessToken());
        assertThat(first.refreshToken().ttl()).isEqualTo(Duration.ofDays(14));
        assertThat(Base64.getUrlDecoder().decode(first.refreshToken().value())).hasSize(32);
        assertThat(second.refreshToken().value()).isNotEqualTo(first.refreshToken().value());
    }
}

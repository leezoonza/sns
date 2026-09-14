package com.zoonza.sns.member.internal.adapter.out.token;

import com.zoonza.sns.member.internal.application.dto.AccessToken;
import com.zoonza.sns.member.internal.application.dto.IssuedToken;
import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import com.zoonza.sns.member.internal.application.port.out.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@EnableConfigurationProperties(RefreshTokenProperties.class)
@RequiredArgsConstructor
public class TokenProviderAdapter implements TokenProvider {
    private final AccessTokenProperties accessTokenProperties;
    private final JwtAccessTokenGenerator accessTokenGenerator;
    private final RefreshTokenProperties refreshTokenProperties;
    private final SecureRefreshTokenGenerator refreshTokenGenerator;

    @Override
    public IssuedToken issue(
            Long memberId,
            String memberRole,
            Instant issuedAt
    ) {
        String accessTokenValue = accessTokenGenerator.generate(
                memberId,
                memberRole,
                issuedAt,
                accessTokenProperties.getAccessTokenTtl()
        );

        String refreshTokenValue = refreshTokenGenerator.generate();

        AccessToken accessToken = new AccessToken(accessTokenValue);
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                refreshTokenProperties.getRefreshTokenTtl()
        );

        return new IssuedToken(accessToken, refreshToken);
    }
}

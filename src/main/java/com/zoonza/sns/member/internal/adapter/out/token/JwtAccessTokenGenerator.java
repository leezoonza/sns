package com.zoonza.sns.member.internal.adapter.out.token;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenGenerator {
    private static final String ROLE_CLAIM = "role";

    private final JwtEncoder encoder;

    public String generate(
            Long memberId,
            String memberRole,
            Duration ttl
    ) {
        Instant issuedAt = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(memberId.toString())
                .claim(ROLE_CLAIM, memberRole)
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ttl))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return encoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}

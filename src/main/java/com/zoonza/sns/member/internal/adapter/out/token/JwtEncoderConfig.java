package com.zoonza.sns.member.internal.adapter.out.token;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(AccessTokenProperties.class)
public class JwtEncoderConfig {
    private static final String ALGORITHM = "HmacSHA256";

    @Bean
    public JwtEncoder jwtEncoder(AccessTokenProperties properties) {
        return NimbusJwtEncoder.withSecretKey(secretKey(properties))
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    private SecretKeySpec secretKey(AccessTokenProperties properties) {
        return new SecretKeySpec(
                properties.getSecret().getBytes(StandardCharsets.UTF_8),
                ALGORITHM
        );
    }
}

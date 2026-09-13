package com.zoonza.sns.member.internal.adapter.out.token;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecureRefreshTokenGenerator {
    private static final int TOKEN_BYTE_SIZE = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(randomBytes);

        return encoder.encodeToString(randomBytes);
    }
}

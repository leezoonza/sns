package com.zoonza.sns.member.internal.application.dto;

import java.time.Duration;

public record RefreshToken(
        String value,
        Duration ttl
) {
}

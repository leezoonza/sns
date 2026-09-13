package com.zoonza.sns.member.internal.adapter.out.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.token.refresh-token")
public class RefreshTokenProperties {
    private Duration refreshTokenTtl;
}

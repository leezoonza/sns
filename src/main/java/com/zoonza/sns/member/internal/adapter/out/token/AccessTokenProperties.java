package com.zoonza.sns.member.internal.adapter.out.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.token.access-token")
public class AccessTokenProperties {
    private String secret;
    private Duration accessTokenTtl;
}

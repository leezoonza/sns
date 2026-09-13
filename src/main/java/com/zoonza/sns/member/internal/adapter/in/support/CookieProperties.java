package com.zoonza.sns.member.internal.adapter.in.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("auth.cookie")
public class CookieProperties {
    private boolean secure;
}

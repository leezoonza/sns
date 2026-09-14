package com.zoonza.sns.shared.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(
        expression = "T(java.lang.Long).valueOf(subject)",
        errorOnInvalidType = true
)
public @interface CurrentMemberId {
}
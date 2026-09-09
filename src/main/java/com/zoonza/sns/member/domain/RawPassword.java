package com.zoonza.sns.member.domain;

import java.util.regex.Pattern;

public record RawPassword(String value) {
    private static final Pattern PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).{8,20}$"
    );

    public RawPassword {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상 20자 이하이며, 영문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
            );
        }
    }
}

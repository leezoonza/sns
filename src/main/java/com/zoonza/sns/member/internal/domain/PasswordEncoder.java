package com.zoonza.sns.member.internal.domain;

public interface PasswordEncoder {
    EncodedPassword encode(NewPassword newPassword);

    boolean matches(String rawPassword, EncodedPassword encodedPassword);
}

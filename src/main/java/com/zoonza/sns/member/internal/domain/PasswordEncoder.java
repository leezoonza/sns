package com.zoonza.sns.member.internal.domain;

public interface PasswordEncoder {
    EncodedPassword encode(RawPassword rawPassword);

    boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword);
}

package com.zoonza.sns.member.domain;

public interface PasswordEncoder {
    EncodedPassword encode(RawPassword rawPassword);

    boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword);
}

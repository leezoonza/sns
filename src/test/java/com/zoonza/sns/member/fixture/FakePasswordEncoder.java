package com.zoonza.sns.member.fixture;

import com.zoonza.sns.member.domain.EncodedPassword;
import com.zoonza.sns.member.domain.PasswordEncoder;
import com.zoonza.sns.member.domain.RawPassword;

public final class FakePasswordEncoder implements PasswordEncoder {

    private static final String PREFIX = "encoded:";

    @Override
    public EncodedPassword encode(RawPassword rawPassword) {
        return new EncodedPassword(PREFIX + rawPassword.value());
    }

    @Override
    public boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword) {
        return encodedPassword.equals(encode(rawPassword));
    }
}

package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.domain.EncodedPassword;
import com.zoonza.sns.member.internal.domain.NewPassword;
import com.zoonza.sns.member.internal.domain.PasswordEncoder;

public final class FakePasswordEncoder implements PasswordEncoder {

    private static final String PREFIX = "encoded:";

    @Override
    public EncodedPassword encode(NewPassword newPassword) {
        return new EncodedPassword(PREFIX + newPassword.value());
    }

    @Override
    public boolean matches(String rawPassword, EncodedPassword encodedPassword) {
        return encodedPassword.value().equals(PREFIX + rawPassword);
    }
}

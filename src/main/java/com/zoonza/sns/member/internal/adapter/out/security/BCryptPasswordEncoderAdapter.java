package com.zoonza.sns.member.internal.adapter.out.security;

import com.zoonza.sns.member.internal.domain.EncodedPassword;
import com.zoonza.sns.member.internal.domain.PasswordEncoder;
import com.zoonza.sns.member.internal.domain.RawPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public EncodedPassword encode(RawPassword rawPassword) {
        String encodedValue = bCryptPasswordEncoder.encode(rawPassword.value());
        return new EncodedPassword(encodedValue);
    }

    @Override
    public boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword.value(), encodedPassword.value());
    }
}

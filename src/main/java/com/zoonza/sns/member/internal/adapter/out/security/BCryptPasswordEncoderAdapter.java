package com.zoonza.sns.member.internal.adapter.out.security;

import com.zoonza.sns.member.internal.domain.EncodedPassword;
import com.zoonza.sns.member.internal.domain.NewPassword;
import com.zoonza.sns.member.internal.domain.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public EncodedPassword encode(NewPassword newPassword) {
        String encodedValue = bCryptPasswordEncoder.encode(newPassword.value());
        return new EncodedPassword(encodedValue);
    }

    @Override
    public boolean matches(String rawPassword, EncodedPassword encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword.value());
    }
}

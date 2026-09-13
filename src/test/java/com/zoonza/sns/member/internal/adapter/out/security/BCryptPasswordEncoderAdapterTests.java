package com.zoonza.sns.member.internal.adapter.out.security;

import com.zoonza.sns.member.internal.domain.EncodedPassword;
import com.zoonza.sns.member.internal.domain.NewPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTests {

    private final BCryptPasswordEncoderAdapter passwordEncoder = new BCryptPasswordEncoderAdapter();

    @Test
    @DisplayName("비밀번호를 BCrypt로 인코딩하고 일치 여부를 확인한다")
    void encodesAndMatchesPassword() {
        NewPassword newPassword = new NewPassword("Abcde1!@");

        EncodedPassword encodedPassword = passwordEncoder.encode(newPassword);

        assertThat(encodedPassword.value()).isNotEqualTo(newPassword.value());
        assertThat(passwordEncoder.matches(newPassword.value(), encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("Other1!@", encodedPassword)).isFalse();
        assertThat(passwordEncoder.matches("short", encodedPassword)).isFalse();
    }
}

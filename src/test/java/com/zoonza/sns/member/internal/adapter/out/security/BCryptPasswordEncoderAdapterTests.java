package com.zoonza.sns.member.internal.adapter.out.security;

import com.zoonza.sns.member.internal.domain.EncodedPassword;
import com.zoonza.sns.member.internal.domain.RawPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTests {

    private final BCryptPasswordEncoderAdapter passwordEncoder = new BCryptPasswordEncoderAdapter();

    @Test
    @DisplayName("비밀번호를 BCrypt로 인코딩하고 일치 여부를 확인한다")
    void encodesAndMatchesPassword() {
        RawPassword rawPassword = new RawPassword("Abcde1!@");

        EncodedPassword encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword.value()).isNotEqualTo(rawPassword.value());
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches(new RawPassword("Other1!@"), encodedPassword)).isFalse();
    }
}

package com.zoonza.sns.member.internal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncodedPasswordTests {

    @Test
    @DisplayName("값이 비어 있지 않으면 인코딩된 비밀번호를 생성한다")
    void createsEncodedPasswordWhenValueIsNotBlank() {
        EncodedPassword encodedPassword = new EncodedPassword("encoded-password");

        assertThat(encodedPassword.value()).isEqualTo("encoded-password");
    }

    @NullSource
    @ParameterizedTest
    @DisplayName("값이 비어 있으면 인코딩된 비밀번호를 생성할 수 없다")
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankEncodedPassword(String value) {
        assertThatThrownBy(() -> new EncodedPassword(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 필수입니다.");
    }
}

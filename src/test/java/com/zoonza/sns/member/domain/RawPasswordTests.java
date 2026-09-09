package com.zoonza.sns.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPasswordTests {

    @DisplayName("값이 비밀번호 정책을 만족하면 원본 비밀번호를 생성한다")
    @ParameterizedTest
    @ValueSource(strings = {"Abcde1!@", "Abcdefghijklmnopq1!@"})
    void createsRawPasswordWhenValueIsValid(String value) {
        RawPassword rawPassword = new RawPassword(value);

        assertThat(rawPassword.value()).isEqualTo(value);
    }

    @DisplayName("값이 비밀번호 정책을 만족하지 않으면 원본 비밀번호를 생성할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "Abcde1!",
            "Abcdefghijklmnopqr1!@",
            "1234567!",
            "Abcdefg!",
            "Abcdefg1"
    })
    void rejectsInvalidRawPassword(String value) {
        assertThatThrownBy(() -> new RawPassword(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 8자 이상 20자 이하이며, 영문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.");
    }
}

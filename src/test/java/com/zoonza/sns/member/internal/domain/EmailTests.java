package com.zoonza.sns.member.internal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTests {

    @ParameterizedTest
    @DisplayName("값이 이메일 형식에 맞으면 이메일을 생성한다")
    @ValueSource(strings = {
            "member@example.com",
            "member.name_1-test@example-domain.co.kr"
    })
    void createsEmailWhenValueIsValid(String value) {
        Email email = new Email(value);

        assertThat(email.value()).isEqualTo(value);
    }


    @NullSource
    @ParameterizedTest
    @DisplayName("값이 이메일 형식에 맞지 않으면 이메일을 생성할 수 없다")
    @ValueSource(strings = {
            "",
            "memberexample.com",
            "member@example",
            "member@example.c",
            "member @example.com"
    })
    void rejectsInvalidEmail(String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 올바르지 않습니다.");
    }
}

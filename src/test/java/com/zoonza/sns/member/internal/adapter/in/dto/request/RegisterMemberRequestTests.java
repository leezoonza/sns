package com.zoonza.sns.member.internal.adapter.in.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static com.zoonza.sns.member.internal.fixture.RegisterMemberRequestFixture.registerMemberRequest;
import static org.assertj.core.api.Assertions.assertThat;

class RegisterMemberRequestTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("회원가입 정보가 모든 정책을 만족하면 요청 검증을 통과한다")
    void acceptsValidRequest() {
        RegisterMemberRequest request = registerMemberRequest().create();

        assertThat(validator.validate(request)).isEmpty();
    }

    @NullSource
    @ParameterizedTest
    @DisplayName("이메일이 올바른 형식이 아니면 요청 검증을 통과할 수 없다")
    @ValueSource(strings = {"", "memberexample.com", "member@example", "member@example.c"})
    void rejectsInvalidEmail(String email) {
        RegisterMemberRequest request = registerMemberRequest()
                .email(email)
                .create();

        assertViolation(request, "email", "이메일 형식이 올바르지 않습니다.");
    }

    @NullSource
    @ParameterizedTest
    @DisplayName("비밀번호가 정책을 만족하지 않으면 요청 검증을 통과할 수 없다")
    @ValueSource(strings = {"Abcde1!", "Abcdefghijklmnopqr1!@", "1234567!", "Abcdefg!", "Abcdefg1"})
    void rejectsInvalidPassword(String rawPassword) {
        RegisterMemberRequest request = registerMemberRequest()
                .rawPassword(rawPassword)
                .create();

        assertViolation(
                request,
                "rawPassword",
                "비밀번호는 8자 이상 20자 이하이며, 영문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
        );
    }


    @NullSource
    @ParameterizedTest
    @DisplayName("사용자 이름이 정책을 만족하지 않으면 요청 검증을 통과할 수 없다")
    @ValueSource(strings = {"", "Member", "member-name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void rejectsInvalidUsername(String username) {
        RegisterMemberRequest request = registerMemberRequest()
                .username(username)
                .create();

        assertViolation(
                request,
                "username",
                "사용자 이름은 영문 소문자, 숫자, 점, 밑줄만 사용할 수 있습니다."
        );
    }

    @NullSource
    @ParameterizedTest
    @DisplayName("이름이 비어 있으면 요청 검증을 통과할 수 없다")
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankDisplayName(String displayName) {
        RegisterMemberRequest request = registerMemberRequest()
                .displayName(displayName)
                .create();

        assertViolation(request, "displayName", "이름은 필수입니다.");
    }

    @Test
    @DisplayName("이름이 17자를 초과하면 요청 검증을 통과할 수 없다")
    void rejectsTooLongDisplayName() {
        RegisterMemberRequest request = registerMemberRequest()
                .displayName("가".repeat(18))
                .create();

        assertViolation(request, "displayName", "이름은 17자 이하여야 합니다.");
    }

    private void assertViolation(RegisterMemberRequest request, String property, String message) {
        Set<ConstraintViolation<RegisterMemberRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo(property);
            assertThat(violation.getMessage()).isEqualTo(message);
        });
    }
}

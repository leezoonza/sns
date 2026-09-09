package com.zoonza.sns.member.adapter.in.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterMemberRequestTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @DisplayName("회원가입 정보가 모든 정책을 만족하면 요청 검증을 통과한다")
    @Test
    void acceptsValidRequest() {
        RegisterMemberRequest request = createRequest(
                "member@example.com",
                "Abcde1!@",
                "member_name",
                "표시 이름"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @DisplayName("이메일이 올바른 형식이 아니면 요청 검증을 통과할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "memberexample.com", "member@example", "member@example.c"})
    void rejectsInvalidEmail(String email) {
        RegisterMemberRequest request = createRequest(email, "Abcde1!@", "member_name", "표시 이름");

        assertViolation(request, "email", "이메일 형식이 올바르지 않습니다.");
    }

    @DisplayName("비밀번호가 정책을 만족하지 않으면 요청 검증을 통과할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Abcde1!", "Abcdefghijklmnopqr1!@", "1234567!", "Abcdefg!", "Abcdefg1"})
    void rejectsInvalidPassword(String rawPassword) {
        RegisterMemberRequest request = createRequest(
                "member@example.com",
                rawPassword,
                "member_name",
                "표시 이름"
        );

        assertViolation(
                request,
                "rawPassword",
                "비밀번호는 8자 이상 20자 이하이며, 영문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
        );
    }

    @DisplayName("사용자 이름이 정책을 만족하지 않으면 요청 검증을 통과할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Member", "member-name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void rejectsInvalidUsername(String username) {
        RegisterMemberRequest request = createRequest("member@example.com", "Abcde1!@", username, "표시 이름");

        assertViolation(
                request,
                "username",
                "사용자 이름은 영문 소문자, 숫자, 점, 밑줄만 사용할 수 있습니다."
        );
    }

    @DisplayName("이름이 비어 있으면 요청 검증을 통과할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankDisplayName(String displayName) {
        RegisterMemberRequest request = createRequest(
                "member@example.com",
                "Abcde1!@",
                "member_name",
                displayName
        );

        assertViolation(request, "displayName", "이름은 필수입니다.");
    }

    @DisplayName("이름이 17자를 초과하면 요청 검증을 통과할 수 없다")
    @Test
    void rejectsTooLongDisplayName() {
        RegisterMemberRequest request = createRequest(
                "member@example.com",
                "Abcde1!@",
                "member_name",
                "가".repeat(18)
        );

        assertViolation(request, "displayName", "이름은 17자 이하여야 합니다.");
    }

    private RegisterMemberRequest createRequest(
            String email,
            String rawPassword,
            String username,
            String displayName
    ) {
        return new RegisterMemberRequest(email, rawPassword, username, displayName);
    }

    private void assertViolation(RegisterMemberRequest request, String property, String message) {
        Set<ConstraintViolation<RegisterMemberRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo(property);
            assertThat(violation.getMessage()).isEqualTo(message);
        });
    }
}

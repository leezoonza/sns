package com.zoonza.sns.member.internal.adapter.in.dto.request;

import com.zoonza.sns.member.internal.application.dto.RegisterMemberCommand;
import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.MemberProfile;
import com.zoonza.sns.member.internal.domain.RawPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterMemberRequest(
        @NotNull(message = "이메일 형식이 올바르지 않습니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "이메일 형식이 올바르지 않습니다."
        )
        String email,

        @NotNull(message = "비밀번호는 8자 이상 20자 이하이며, 영문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).{8,20}$",
                message = "비밀번호는 8자 이상 20자 이하이며, 영문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
        )
        String rawPassword,

        @NotNull(message = "사용자 이름은 영문 소문자, 숫자, 점, 밑줄만 사용할 수 있습니다.")
        @Pattern(
                regexp = "^[a-z0-9._]{1,30}$",
                message = "사용자 이름은 영문 소문자, 숫자, 점, 밑줄만 사용할 수 있습니다."
        )
        String username,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 17, message = "이름은 17자 이하여야 합니다.")
        String displayName
) {
    public RegisterMemberCommand toCommand() {
        return new RegisterMemberCommand(
                new Email(this.email),
                new RawPassword(this.rawPassword),
                new MemberProfile(
                        this.username,
                        this.displayName,
                        null,
                        null
                )
        );
    }
}

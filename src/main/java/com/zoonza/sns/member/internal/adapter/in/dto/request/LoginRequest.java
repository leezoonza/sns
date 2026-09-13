package com.zoonza.sns.member.internal.adapter.in.dto.request;

import com.zoonza.sns.member.internal.application.dto.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String rawPassword
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, rawPassword);
    }
}

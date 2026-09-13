package com.zoonza.sns.member.internal.application.dto.command;

public record LoginCommand(
        String email,
        String rawPassword
) {
}

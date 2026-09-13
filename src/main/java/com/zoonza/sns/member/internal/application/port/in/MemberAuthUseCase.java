package com.zoonza.sns.member.internal.application.port.in;

import com.zoonza.sns.member.internal.application.dto.command.LoginCommand;
import com.zoonza.sns.member.internal.application.dto.result.TokenResult;

public interface MemberAuthUseCase {
    TokenResult login(LoginCommand command);
}

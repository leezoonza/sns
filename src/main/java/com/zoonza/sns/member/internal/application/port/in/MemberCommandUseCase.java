package com.zoonza.sns.member.internal.application.port.in;

import com.zoonza.sns.member.internal.application.dto.RegisterMemberCommand;

public interface MemberCommandUseCase {
    void register(RegisterMemberCommand command);
}

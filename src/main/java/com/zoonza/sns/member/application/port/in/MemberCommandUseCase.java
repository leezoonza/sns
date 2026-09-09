package com.zoonza.sns.member.application.port.in;

import com.zoonza.sns.member.application.dto.RegisterMemberCommand;

public interface MemberCommandUseCase {
    void register(RegisterMemberCommand command);
}

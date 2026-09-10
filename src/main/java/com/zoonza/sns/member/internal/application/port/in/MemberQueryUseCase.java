package com.zoonza.sns.member.internal.application.port.in;

import com.zoonza.sns.member.internal.domain.Email;

public interface MemberQueryUseCase {
    boolean isEmailAvailable(Email email);

    boolean isUsernameAvailable(String username);
}

package com.zoonza.sns.member.application.port.in;

import com.zoonza.sns.member.domain.Email;

public interface MemberQueryUseCase {
    boolean isEmailAvailable(Email email);

    boolean isUsernameAvailable(String username);
}

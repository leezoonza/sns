package com.zoonza.sns.member.internal.application.port.out;

import com.zoonza.sns.member.internal.application.dto.IssuedToken;

public interface TokenProvider {
    IssuedToken issue(Long memberId, String memberRole);
}

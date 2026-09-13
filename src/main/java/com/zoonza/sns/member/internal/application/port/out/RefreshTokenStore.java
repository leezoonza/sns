package com.zoonza.sns.member.internal.application.port.out;

import com.zoonza.sns.member.internal.application.dto.RefreshToken;

public interface RefreshTokenStore {
    void save(Long memberId, RefreshToken refreshToken);

    void delete(String refreshTokenValue);
}

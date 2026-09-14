package com.zoonza.sns.member.internal.application.port.out;

import com.zoonza.sns.member.internal.application.dto.RefreshToken;

import java.util.Optional;

public interface RefreshTokenStore {
    void save(Long memberId, RefreshToken refreshToken);

    Optional<Long> consume(String refreshTokenValue);

    void delete(String refreshTokenValue);
}

package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import com.zoonza.sns.member.internal.application.port.out.RefreshTokenStore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class FakeRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, StoredRefreshToken> tokens = new LinkedHashMap<>();

    @Override
    public void save(Long memberId, RefreshToken refreshToken) {
        tokens.put(
                refreshToken.value(),
                new StoredRefreshToken(memberId, refreshToken)
        );
    }

    @Override
    public Optional<Long> consume(String refreshTokenValue) {
        return Optional.ofNullable(tokens.remove(refreshTokenValue))
                .map(StoredRefreshToken::memberId);
    }

    @Override
    public void delete(String refreshTokenValue) {
        tokens.remove(refreshTokenValue);
    }

    public Optional<StoredRefreshToken> findByValue(String refreshTokenValue) {
        return Optional.ofNullable(tokens.get(refreshTokenValue));
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    public record StoredRefreshToken(
            Long memberId,
            RefreshToken refreshToken
    ) {
    }
}

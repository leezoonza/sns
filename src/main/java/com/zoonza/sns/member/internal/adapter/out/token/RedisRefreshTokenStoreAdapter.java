package com.zoonza.sns.member.internal.adapter.out.token;

import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import com.zoonza.sns.member.internal.application.port.out.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStoreAdapter implements RefreshTokenStore {
    private static final String KEY_PREFIX = "auth:refresh:token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(Long memberId, RefreshToken refreshToken) {
        redisTemplate
                .opsForValue()
                .set(
                    createKey(refreshToken.value()),
                    memberId.toString(),
                    refreshToken.ttl()
                );
    }

    @Override
    public Optional<Long> consume(String refreshTokenValue) {
        String memberId = redisTemplate
                .opsForValue()
                .getAndDelete(createKey(refreshTokenValue));

        return Optional.ofNullable(memberId)
                .map(Long::valueOf);
    }

    @Override
    public void delete(String refreshTokenValue) {
        redisTemplate.delete(createKey(refreshTokenValue));
    }

    private String createKey(String refreshTokenValue) {
        return KEY_PREFIX + refreshTokenValue;
    }
}

package com.zoonza.sns.member.internal.adapter.out.token;

import com.zoonza.sns.TestcontainersConfiguration;
import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@Import({RedisRefreshTokenStoreAdapter.class, TestcontainersConfiguration.class})
class RedisRefreshTokenStoreAdapterTests {

    private static final String REFRESH_TOKEN_VALUE = "refresh-token";
    private static final String REFRESH_TOKEN_KEY = "auth:refresh:token:" + REFRESH_TOKEN_VALUE;

    @Autowired
    private RedisRefreshTokenStoreAdapter refreshTokenStore;

    @Autowired
    private StringRedisTemplate redis;

    @AfterEach
    void cleanUp() {
        redis.delete(REFRESH_TOKEN_KEY);
    }

    @Test
    @DisplayName("저장된 리프레시 토큰을 한 번만 소비한다")
    void consumesRefreshTokenOnce() {
        refreshTokenStore.save(42L, new RefreshToken(REFRESH_TOKEN_VALUE, Duration.ofDays(14)));

        assertThat(redis.opsForValue().get(REFRESH_TOKEN_KEY)).isEqualTo("42");
        assertThat(redis.getExpire(REFRESH_TOKEN_KEY, TimeUnit.SECONDS))
                .isBetween(1209500L, 1209600L);
        assertThat(refreshTokenStore.consume(REFRESH_TOKEN_VALUE)).contains(42L);
        assertThat(refreshTokenStore.consume(REFRESH_TOKEN_VALUE)).isEmpty();
        assertThat(redis.hasKey(REFRESH_TOKEN_KEY)).isFalse();
    }

    @Test
    @DisplayName("저장되지 않은 리프레시 토큰을 소비하면 빈 결과를 반환한다")
    void returnsEmptyForMissingRefreshToken() {
        assertThat(refreshTokenStore.consume(REFRESH_TOKEN_VALUE)).isEmpty();
    }
}

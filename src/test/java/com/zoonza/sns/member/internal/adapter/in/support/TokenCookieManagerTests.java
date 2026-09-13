package com.zoonza.sns.member.internal.adapter.in.support;

import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

class TokenCookieManagerTests {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("발급과 삭제 쿠키에 동일한 보안 속성과 경로를 적용한다")
    void createsAndClearsCookie(boolean secure) {
        var properties = new CookieProperties();
        properties.setSecure(secure);
        var manager = new TokenCookieManager(properties);
        var issued = manager.createRefreshTokenCookie(new RefreshToken("refresh", Duration.ofDays(14)));
        var cleared = manager.clearRefreshTokenCookie();

        assertThat(issued.getName()).isEqualTo("refreshToken");
        assertThat(issued.getValue()).isEqualTo("refresh");
        assertThat(issued.getMaxAge()).isEqualTo(Duration.ofDays(14));
        assertThat(cleared.getName()).isEqualTo(issued.getName());
        assertThat(cleared.getValue()).isEmpty();
        assertThat(cleared.getMaxAge()).isEqualTo(Duration.ZERO);
        for (var cookie : java.util.List.of(issued, cleared)) {
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isEqualTo(secure);
            assertThat(cookie.getSameSite()).isEqualTo("Lax");
            assertThat(cookie.getPath()).isEqualTo("/api/auth");
        }
    }
}

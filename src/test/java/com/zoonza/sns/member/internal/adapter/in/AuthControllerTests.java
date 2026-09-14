package com.zoonza.sns.member.internal.adapter.in;

import com.zoonza.sns.bootstrap.security.ApiAccessDeniedHandler;
import com.zoonza.sns.bootstrap.security.ApiAuthenticationEntryPoint;
import com.zoonza.sns.bootstrap.security.ApiSecurityConfiguration;
import com.zoonza.sns.bootstrap.web.GlobalExceptionHandler;
import com.zoonza.sns.member.internal.adapter.in.support.TokenCookieManager;
import com.zoonza.sns.member.internal.application.dto.AccessToken;
import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import com.zoonza.sns.member.internal.application.dto.result.TokenResult;
import com.zoonza.sns.member.internal.application.port.in.MemberAuthUseCase;
import com.zoonza.sns.member.internal.domain.AuthErrorCode;
import com.zoonza.sns.shared.error.BusinessException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static com.zoonza.sns.member.internal.fixture.LoginRequestFixture.loginRequest;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({
        GlobalExceptionHandler.class,
        ApiSecurityConfiguration.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class,
        TokenCookieManager.class
})
class AuthControllerTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean MemberAuthUseCase auth;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    @DisplayName("익명 로그인 요청에 액세스 토큰과 HttpOnly 리프레시 쿠키를 반환한다")
    void logsIn() throws Exception {
        var request = loginRequest().create();
        when(auth.login(request.toCommand())).thenReturn(new TokenResult(
                new AccessToken("access"), new RefreshToken("refresh", Duration.ofDays(14))));

        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessTokenValue").value("access"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().value("refreshToken", "refresh"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth"))
                .andExpect(cookie().maxAge("refreshToken", 1209600));
        verify(auth).login(request.toCommand());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("빈 이메일은 지정한 검증 메시지로 거절한다")
    void rejectsBlankEmail(String email) throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsBytes(loginRequest().email(email).create())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-001"))
                .andExpect(jsonPath("$.detail").value("이메일을 입력해 주세요."));
        verifyNoInteractions(auth);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("빈 비밀번호는 지정한 검증 메시지로 거절한다")
    void rejectsBlankPassword(String password) throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsBytes(loginRequest().rawPassword(password).create())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-001"))
                .andExpect(jsonPath("$.detail").value("비밀번호를 입력해 주세요."));
        verifyNoInteractions(auth);
    }

    @Test
    @DisplayName("인증 실패 시 오류 코드와 메시지를 반환하고 쿠키를 발급하지 않는다")
    void rejectsCredentials() throws Exception {
        var request = loginRequest().rawPassword("short").create();
        when(auth.login(request.toCommand())).thenThrow(new BusinessException(AuthErrorCode.INVALID_CREDENTIAL));
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"))
                .andExpect(jsonPath("$.detail").value("이메일 또는 비밀번호를 확인해 주세요"))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @DisplayName("리프레시 토큰을 재발급하면 새 액세스 토큰과 HttpOnly 리프레시 쿠키를 반환한다")
    void reissuesTokens() throws Exception {
        when(auth.reissue("old-refresh")).thenReturn(new TokenResult(
                new AccessToken("new-access"), new RefreshToken("new-refresh", Duration.ofDays(14))));

        mvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refreshToken", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessTokenValue").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().value("refreshToken", "new-refresh"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth"))
                .andExpect(cookie().maxAge("refreshToken", 1209600));

        verify(auth).reissue("old-refresh");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰은 오류 메시지를 반환하고 쿠키를 발급하지 않는다")
    void rejectsInvalidRefreshToken() throws Exception {
        when(auth.reissue("invalid-refresh"))
                .thenThrow(new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        mvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refreshToken", "invalid-refresh")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"))
                .andExpect(jsonPath("$.detail").value("인증이 만료되었습니다. 다시 로그인해 주세요."))
                .andExpect(header().doesNotExist("Set-Cookie"));

        verify(auth).reissue("invalid-refresh");
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키가 없으면 인증 만료 오류를 반환한다")
    void rejectsMissingRefreshTokenCookie() throws Exception {
        when(auth.reissue(isNull()))
                .thenThrow(new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        mvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"))
                .andExpect(jsonPath("$.detail").value("인증이 만료되었습니다. 다시 로그인해 주세요."))
                .andExpect(header().doesNotExist("Set-Cookie"));

        verify(auth).reissue(isNull());
    }

    @Test
    @DisplayName("로그아웃하면 리프레시 토큰을 삭제하고 쿠키를 만료한다")
    void logout() throws Exception {
        mvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "refresh")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(auth).logout("refresh");
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키가 없어도 로그아웃하고 쿠키를 만료한다")
    void logsOutWithoutRefreshTokenCookie() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(auth).logout(isNull());
    }
}

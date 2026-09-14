package com.zoonza.sns.bootstrap.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.zoonza.sns.shared.auth.CurrentMemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ApiSecurityConfigurationTests.SecurityTestController.class,
        properties = "auth.token.access-token.secret=test-only-secret-key-at-least-32-bytes-long"
)
@Import({
        ApiSecurityConfiguration.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class,
        JwtDecoderConfig.class,
        ApiSecurityConfigurationTests.SecurityTestController.class
})
class ApiSecurityConfigurationTests {

    private static final String SECRET = "test-only-secret-key-at-least-32-bytes-long";
    private static final String OTHER_SECRET = "other-test-secret-key-at-least-32-bytes-long";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApiAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("인증 API는 액세스 토큰 없이 접근할 수 있다")
    void permitsAuthenticationEndpointWithoutToken() throws Exception {
        mvc.perform(get("/api/auth/security-test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("보호 API에 액세스 토큰이 없으면 인증 오류 코드와 메시지를 반환한다")
    void rejectsProtectedEndpointWithoutToken() throws Exception {
        mvc.perform(get("/api/security-test/identity"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTH-003"))
                .andExpect(jsonPath("$.detail").value("인증 정보가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("서명이 유효하지 않은 액세스 토큰은 인증 오류로 거절한다")
    void rejectsAccessTokenWithInvalidSignature() throws Exception {
        String accessToken = createAccessToken(
                OTHER_SECRET,
                "42",
                "MEMBER",
                Instant.now().plusSeconds(900)
        );

        mvc.perform(get("/api/security-test/identity")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"))
                .andExpect(jsonPath("$.detail").value("인증 정보가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("만료된 액세스 토큰은 인증 오류로 거절한다")
    void rejectsExpiredAccessToken() throws Exception {
        String accessToken = createAccessToken(
                SECRET,
                "42",
                "MEMBER",
                Instant.now().minusSeconds(600)
        );

        mvc.perform(get("/api/security-test/identity")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"))
                .andExpect(jsonPath("$.detail").value("인증 정보가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("유효한 액세스 토큰의 subject와 role을 회원 ID와 권한으로 변환한다")
    void authenticatesMemberAndConvertsRole() throws Exception {
        String accessToken = createAccessToken(
                SECRET,
                "42",
                "ADMIN",
                Instant.now().plusSeconds(900)
        );

        mvc.perform(get("/api/security-test/identity")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(42))
                .andExpect(jsonPath("$.authorities", hasItem("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("인증 회원의 접근이 거부되면 인가 오류 코드와 메시지를 반환한다")
    void handlesAccessDenied() throws Exception {
        var response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("forbidden")
        );

        var body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(body.get("code").asText()).isEqualTo("AUTH-004");
        assertThat(body.get("detail").asText()).isEqualTo("접근 권한이 없습니다.");
    }

    private String createAccessToken(
            String secret,
            String subject,
            String role,
            Instant expiresAt
    ) throws Exception {
        Instant issuedAt = Instant.now().minusSeconds(60);
        var claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("role", role)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).build(), claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));

        return jwt.serialize();
    }

    @RestController
    public static class SecurityTestController {

        @GetMapping("/api/auth/security-test")
        void publicEndpoint() {
        }

        @GetMapping("/api/security-test/identity")
        SecurityIdentity identity(
                @CurrentMemberId Long memberId,
                Authentication authentication
        ) {
            List<String> authorities = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();

            return new SecurityIdentity(memberId, authorities);
        }
    }

    record SecurityIdentity(Long memberId, List<String> authorities) {
    }
}

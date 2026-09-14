package com.zoonza.sns.member.internal.integration;

import com.nimbusds.jwt.SignedJWT;
import com.zoonza.sns.TestcontainersConfiguration;
import com.zoonza.sns.member.internal.adapter.out.persistence.MemberJpaRepository;
import com.zoonza.sns.member.internal.domain.PasswordEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static com.zoonza.sns.member.internal.fixture.LoginRequestFixture.loginRequest;
import static com.zoonza.sns.member.internal.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MemberLoginIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberJpaRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired StringRedisTemplate redis;
    private Long memberId;
    private String refreshKey;

    @BeforeEach
    void setUp() {
        memberId = repository.saveAndFlush(member().create(encoder)).getId();
    }

    @AfterEach
    void cleanUp() {
        if (refreshKey != null) {
            redis.delete(refreshKey);
        }
        repository.deleteById(memberId);
    }

    @Test
    @DisplayName("로그인하면 JWT와 쿠키를 발급하고 Redis 및 DB에 변경을 커밋한다")
    void logsInAndPersists() throws Exception {
        Instant before = Instant.now().minusSeconds(1);
        var response = mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsBytes(loginRequest().create())))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andReturn().getResponse();
        String accessToken = mapper.readTree(response.getContentAsString()).get("accessTokenValue").asText();
        var jwt = SignedJWT.parse(accessToken);
        assertThat(jwt.verify(new com.nimbusds.jose.crypto.MACVerifier(
                "test-only-secret-key-at-least-32-bytes-long"))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(memberId.toString());
        assertThat(jwt.getJWTClaimsSet().getStringClaim("role")).isEqualTo("MEMBER");
        assertThat(jwt.getJWTClaimsSet().getExpirationTime().toInstant())
                .isEqualTo(jwt.getJWTClaimsSet().getIssueTime().toInstant().plusSeconds(900));

        refreshKey = "auth:refresh:token:" + response.getCookie("refreshToken").getValue();
        assertThat(redis.opsForValue().get(refreshKey)).isEqualTo(memberId.toString());
        assertThat(redis.getExpire(refreshKey, TimeUnit.SECONDS)).isBetween(1209500L, 1209600L);
        Instant lastLoginAt = repository.findById(memberId).orElseThrow().getLastLoginAt();
        assertThat(lastLoginAt).isBetween(before, Instant.now());
        assertThat(jwt.getJWTClaimsSet().getIssueTime().toInstant())
                .isEqualTo(lastLoginAt.truncatedTo(ChronoUnit.SECONDS));
    }

    @ParameterizedTest
    @CsvSource({"missing@example.com, Abcde1!@", "member@example.com, wrong", "invalid-email, Abcde1!@"})
    @DisplayName("잘못된 자격 증명은 동일한 오류를 반환하고 로그인 시각을 변경하지 않는다")
    void rejectsCredentials(String email, String password) throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsBytes(loginRequest().email(email).rawPassword(password).create())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"))
                .andExpect(jsonPath("$.detail").value("이메일 또는 비밀번호를 확인해 주세요"))
                .andExpect(header().doesNotExist("Set-Cookie"));
        assertThat(repository.findById(memberId).orElseThrow().getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("로그아웃하면 Redis의 리프레시 토큰을 삭제하고 쿠키를 만료한다")
    void logsOutAndDeletesRefreshToken() throws Exception {
        var loginResponse = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(mapper.writeValueAsBytes(loginRequest().create())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var refreshTokenCookie = loginResponse.getCookie("refreshToken");
        refreshKey = "auth:refresh:token:" + refreshTokenCookie.getValue();
        assertThat(redis.hasKey(refreshKey)).isTrue();

        mvc.perform(post("/api/auth/logout").cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        assertThat(redis.hasKey(refreshKey)).isFalse();
    }
}

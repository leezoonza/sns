package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.member.internal.application.dto.AccessToken;
import com.zoonza.sns.member.internal.application.dto.IssuedToken;
import com.zoonza.sns.member.internal.application.dto.RefreshToken;
import com.zoonza.sns.member.internal.application.dto.command.LoginCommand;
import com.zoonza.sns.member.internal.application.port.out.RefreshTokenStore;
import com.zoonza.sns.member.internal.domain.AuthErrorCode;
import com.zoonza.sns.member.internal.domain.Member;
import com.zoonza.sns.member.internal.domain.MemberErrorCode;
import com.zoonza.sns.member.internal.domain.MemberStatus;
import com.zoonza.sns.member.internal.fixture.FakeMemberRepository;
import com.zoonza.sns.member.internal.fixture.FakePasswordEncoder;
import com.zoonza.sns.member.internal.fixture.FakeRefreshTokenStore;
import com.zoonza.sns.member.internal.fixture.StubTokenProvider;
import com.zoonza.sns.shared.error.BusinessException;
import com.zoonza.sns.shared.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.zoonza.sns.member.internal.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberAuthServiceTests {

    private static final LoginCommand LOGIN_COMMAND =
            new LoginCommand("member@example.com", "Abcde1!@");
    private static final IssuedToken ISSUED_TOKEN = new IssuedToken(
            new AccessToken("access"),
            new RefreshToken("refresh", Duration.ofDays(14))
    );
    private static final RefreshToken OLD_REFRESH_TOKEN =
            new RefreshToken("old-refresh", Duration.ofDays(14));

    private FakePasswordEncoder passwordEncoder;
    private FakeMemberRepository memberRepository;
    private FakeRefreshTokenStore refreshTokenStore;
    private StubTokenProvider tokenProvider;
    private MemberAuthService memberAuthService;
    private Member member;

    @BeforeEach
    void setUp() {
        passwordEncoder = new FakePasswordEncoder();
        memberRepository = new FakeMemberRepository();
        refreshTokenStore = new FakeRefreshTokenStore();
        tokenProvider = new StubTokenProvider(ISSUED_TOKEN);
        memberAuthService = new MemberAuthService(
                passwordEncoder,
                memberRepository,
                refreshTokenStore,
                tokenProvider
        );
        member = memberRepository.save(member().create(passwordEncoder));
    }

    @Test
    @DisplayName("로그인하면 회원 권한으로 토큰을 발급하고 저장한 뒤 로그인 시각을 갱신한다")
    void logsIn() {
        Instant beforeLogin = Instant.now();

        var result = memberAuthService.login(LOGIN_COMMAND);

        assertThat(result.accessToken()).isEqualTo(ISSUED_TOKEN.accessToken());
        assertThat(result.refreshToken()).isEqualTo(ISSUED_TOKEN.refreshToken());
        assertThat(tokenProvider.requestedMemberId()).isEqualTo(member.getId());
        assertThat(tokenProvider.requestedMemberRole()).isEqualTo("MEMBER");
        assertThat(tokenProvider.requestedIssuedAt()).isEqualTo(member.getLastLoginAt());
        assertThat(refreshTokenStore.findByValue("refresh")).hasValueSatisfying(storedToken -> {
            assertThat(storedToken.memberId()).isEqualTo(member.getId());
            assertThat(storedToken.refreshToken()).isEqualTo(ISSUED_TOKEN.refreshToken());
        });
        assertThat(member.getLastLoginAt()).isBetween(beforeLogin, Instant.now());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰을 소비하고 새 토큰을 발급해 저장한다")
    void reissuesTokens() {
        refreshTokenStore.save(member.getId(), OLD_REFRESH_TOKEN);
        Instant beforeReissue = Instant.now();

        var result = memberAuthService.reissue(OLD_REFRESH_TOKEN.value());

        assertThat(result.accessToken()).isEqualTo(ISSUED_TOKEN.accessToken());
        assertThat(result.refreshToken()).isEqualTo(ISSUED_TOKEN.refreshToken());
        assertThat(refreshTokenStore.findByValue(OLD_REFRESH_TOKEN.value())).isEmpty();
        assertThat(refreshTokenStore.findByValue(ISSUED_TOKEN.refreshToken().value()))
                .hasValueSatisfying(storedToken -> {
                    assertThat(storedToken.memberId()).isEqualTo(member.getId());
                    assertThat(storedToken.refreshToken()).isEqualTo(ISSUED_TOKEN.refreshToken());
                });
        assertThat(tokenProvider.requestedMemberId()).isEqualTo(member.getId());
        assertThat(tokenProvider.requestedMemberRole()).isEqualTo("MEMBER");
        assertThat(tokenProvider.requestedIssuedAt()).isBetween(beforeReissue, Instant.now());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "invalid-refresh"})
    @DisplayName("저장되지 않은 리프레시 토큰은 인증 만료 오류로 거절한다")
    void rejectsInvalidRefreshToken(String refreshTokenValue) {
        assertThatThrownBy(() -> memberAuthService.reissue(refreshTokenValue))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertNoIssuedToken();
        assertThat(refreshTokenStore.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("리프레시 토큰의 회원이 없으면 토큰을 소비하고 인증 만료 오류로 거절한다")
    void rejectsRefreshTokenForMissingMember() {
        refreshTokenStore.save(999L, OLD_REFRESH_TOKEN);

        assertThatThrownBy(() -> memberAuthService.reissue(OLD_REFRESH_TOKEN.value()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertThat(refreshTokenStore.findByValue(OLD_REFRESH_TOKEN.value())).isEmpty();
        assertNoIssuedToken();
    }

    @Test
    @DisplayName("로그아웃하면 저장된 리프레시 토큰을 삭제한다")
    void logsOut() {
        refreshTokenStore.save(member.getId(), ISSUED_TOKEN.refreshToken());

        memberAuthService.logout(ISSUED_TOKEN.refreshToken().value());

        assertThat(refreshTokenStore.findByValue(ISSUED_TOKEN.refreshToken().value())).isEmpty();
    }

    @Test
    @DisplayName("리프레시 토큰이 없으면 저장소를 변경하지 않고 로그아웃한다")
    void logsOutWithoutRefreshToken() {
        refreshTokenStore.save(member.getId(), ISSUED_TOKEN.refreshToken());

        memberAuthService.logout(null);

        assertThat(refreshTokenStore.findByValue(ISSUED_TOKEN.refreshToken().value())).isPresent();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "invalid-email"})
    @DisplayName("잘못된 이메일 형식은 동일한 인증 오류로 처리한다")
    void rejectsMalformedEmail(String email) {
        assertError(new LoginCommand(email, "password"), AuthErrorCode.INVALID_CREDENTIAL);
        assertNoAuthenticationResult();
    }

    @Test
    @DisplayName("없는 회원은 인증 오류를 반환하고 토큰을 발급하지 않는다")
    void rejectsUnknownMember() {
        assertError(
                new LoginCommand("missing@example.com", LOGIN_COMMAND.rawPassword()),
                AuthErrorCode.INVALID_CREDENTIAL
        );
        assertNoAuthenticationResult();
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong", "", "Other1!@"})
    @DisplayName("일치하지 않는 비밀번호는 신규 비밀번호 정책과 무관하게 인증 오류로 처리한다")
    void rejectsWrongPassword(String password) {
        assertError(
                new LoginCommand(LOGIN_COMMAND.email(), password),
                AuthErrorCode.INVALID_CREDENTIAL
        );
        assertNoAuthenticationResult();
    }

    @Test
    @DisplayName("탈퇴 회원은 올바른 비밀번호로도 로그인할 수 없다")
    void rejectsWithdrawnMember() {
        ReflectionTestUtils.setField(member, "status", MemberStatus.WITHDRAWN);

        assertError(LOGIN_COMMAND, MemberErrorCode.WITHDRAWN_MEMBER);
        assertNoAuthenticationResult();
    }

    @Test
    @DisplayName("리프레시 토큰 저장 실패를 그대로 전파한다")
    void propagatesRefreshTokenStoreFailure() {
        var failure = new IllegalStateException("Redis unavailable");
        RefreshTokenStore failingStore = new RefreshTokenStore() {
            @Override
            public void save(Long memberId, RefreshToken refreshToken) {
                throw failure;
            }

            @Override
            public Optional<Long> consume(String refreshTokenValue) {
                return Optional.empty();
            }

            @Override
            public void delete(String refreshTokenValue) {
            }
        };
        var serviceWithFailingStore = new MemberAuthService(
                passwordEncoder,
                memberRepository,
                failingStore,
                tokenProvider
        );

        assertThatThrownBy(() -> serviceWithFailingStore.login(LOGIN_COMMAND)).isSameAs(failure);
    }

    private void assertError(LoginCommand command, ErrorCode errorCode) {
        assertThatThrownBy(() -> memberAuthService.login(command))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertNoAuthenticationResult() {
        assertNoIssuedToken();
        assertThat(refreshTokenStore.isEmpty()).isTrue();
        assertThat(member.getLastLoginAt()).isNull();
    }

    private void assertNoIssuedToken() {
        assertThat(tokenProvider.requestedMemberId()).isNull();
        assertThat(tokenProvider.requestedMemberRole()).isNull();
        assertThat(tokenProvider.requestedIssuedAt()).isNull();
    }
}

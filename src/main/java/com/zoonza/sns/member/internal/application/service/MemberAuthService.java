package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.member.internal.application.dto.IssuedToken;
import com.zoonza.sns.member.internal.application.dto.command.LoginCommand;
import com.zoonza.sns.member.internal.application.dto.result.TokenResult;
import com.zoonza.sns.member.internal.application.port.in.MemberAuthUseCase;
import com.zoonza.sns.member.internal.application.port.out.RefreshTokenStore;
import com.zoonza.sns.member.internal.application.port.out.TokenProvider;
import com.zoonza.sns.member.internal.domain.*;
import com.zoonza.sns.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class MemberAuthService implements MemberAuthUseCase {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public TokenResult login(LoginCommand command) {
        Member member = requireMember(command.email());

        validatePassword(member, command.rawPassword());

        requireActiveMember(member);

        IssuedToken issuedToken = tokenProvider.issue(member.getId(), member.getRole().name());
        refreshTokenStore.save(member.getId(), issuedToken.refreshToken());

        member.updateLastLoginAt(Instant.now());

        return new TokenResult(
                issuedToken.accessToken(),
                issuedToken.refreshToken()
        );
    }

    @Override
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null) {
            return;
        }

        refreshTokenStore.delete(refreshTokenValue);
    }

    private Member requireMember(String email) {
        return memberRepository.findByEmail(parseEmail(email))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIAL));
    }

    private Email parseEmail(String email) {
        try {
            return new Email(email);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL);
        }
    }

    private void validatePassword(Member member, String rawPassword) {
        if (!member.verifyPassword(rawPassword, passwordEncoder)) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL);
        }
    }

    private void requireActiveMember(Member member) {
        switch (member.getStatus()) {
            case ACTIVE -> {}
            case WITHDRAWN -> throw new BusinessException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }
}

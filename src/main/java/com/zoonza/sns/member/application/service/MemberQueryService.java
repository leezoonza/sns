package com.zoonza.sns.member.application.service;

import com.zoonza.sns.member.application.port.in.MemberQueryUseCase;
import com.zoonza.sns.member.domain.Email;
import com.zoonza.sns.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryService implements MemberQueryUseCase {
    private final MemberRepository memberRepository;

    @Override
    public boolean isEmailAvailable(Email email) {
        return !memberRepository.existsByEmail(email);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !memberRepository.existsByUsername(username);
    }
}

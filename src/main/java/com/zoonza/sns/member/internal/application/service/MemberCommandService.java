package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.common.BusinessException;
import com.zoonza.sns.member.internal.application.dto.RegisterMemberCommand;
import com.zoonza.sns.member.internal.application.port.in.MemberCommandUseCase;
import com.zoonza.sns.member.internal.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCommandService implements MemberCommandUseCase {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public void register(RegisterMemberCommand command) {
        validateEmailDuplication(command.email());
        validateUsernameDuplication(command.profile().username());

        Member member = Member.of(
                command.email(),
                command.rawPassword(),
                command.profile(),
                passwordEncoder
        );

        memberRepository.save(member);
    }

    private void validateEmailDuplication(Email email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateUsernameDuplication(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_USERNAME);
        }
    }
}

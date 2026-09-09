package com.zoonza.sns.member.application.service;

import com.zoonza.sns.member.application.dto.RegisterMemberCommand;
import com.zoonza.sns.member.application.port.in.MemberCommandUseCase;
import com.zoonza.sns.member.domain.Email;
import com.zoonza.sns.member.domain.Member;
import com.zoonza.sns.member.domain.MemberRepository;
import com.zoonza.sns.member.domain.PasswordEncoder;
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
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateUsernameDuplication(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 사용 중인 사용자 이름입니다.");
        }
    }
}

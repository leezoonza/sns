package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.common.BusinessException;
import com.zoonza.sns.member.internal.application.dto.RegisterMemberCommand;
import com.zoonza.sns.member.internal.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTests {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    private MemberCommandService memberCommandService;

    @BeforeEach
    void setUp() {
        memberCommandService = new MemberCommandService(passwordEncoder, memberRepository);
    }

    @Test
    @DisplayName("중복되지 않은 회원 정보로 회원을 등록한다")
    void registersMember() {
        RegisterMemberCommand command = createCommand();
        EncodedPassword encodedPassword = new EncodedPassword("encoded-password");
        when(passwordEncoder.encode(command.rawPassword())).thenReturn(encodedPassword);

        memberCommandService.register(command);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getEmail()).isEqualTo(command.email());
        assertThat(savedMember.getEncodedPassword()).isEqualTo(encodedPassword);
        assertThat(savedMember.getProfile()).isEqualTo(command.profile());
        assertThat(savedMember.getVisibility()).isEqualTo(AccountVisibility.PUBLIC);
        assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(savedMember.getRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(savedMember.getRegisteredAt()).isNotNull();
        assertThat(savedMember.getUpdatedAt()).isEqualTo(savedMember.getRegisteredAt());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원을 등록할 수 없다")
    void rejectsDuplicateEmail() {
        RegisterMemberCommand command = createCommand();
        when(memberRepository.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.DUPLICATE_EMAIL);

        verify(memberRepository, never()).existsByUsername(any());
        verify(memberRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("이미 사용 중인 사용자 이름이면 회원을 등록할 수 없다")
    void rejectsDuplicateUsername() {
        RegisterMemberCommand command = createCommand();
        when(memberRepository.existsByUsername(command.profile().username())).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.DUPLICATE_USERNAME);

        verify(memberRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    private RegisterMemberCommand createCommand() {
        return new RegisterMemberCommand(
                new Email("member@example.com"),
                new RawPassword("Abcde1!@"),
                new MemberProfile("member_name", "표시 이름", null, null)
        );
    }
}

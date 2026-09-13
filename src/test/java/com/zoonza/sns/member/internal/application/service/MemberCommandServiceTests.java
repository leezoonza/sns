package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.member.internal.application.dto.command.RegisterMemberCommand;
import com.zoonza.sns.member.internal.domain.*;
import com.zoonza.sns.member.internal.fixture.FakeMemberRepository;
import com.zoonza.sns.member.internal.fixture.FakePasswordEncoder;
import com.zoonza.sns.shared.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.zoonza.sns.member.internal.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberCommandServiceTests {

    private FakePasswordEncoder passwordEncoder;
    private FakeMemberRepository memberRepository;
    private MemberCommandService memberCommandService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new FakePasswordEncoder();
        memberRepository = new FakeMemberRepository();
        memberCommandService = new MemberCommandService(passwordEncoder, memberRepository);
    }

    @Test
    @DisplayName("중복되지 않은 회원 정보로 회원을 등록한다")
    void registersMember() {
        RegisterMemberCommand command = createCommand();

        memberCommandService.register(command);

        Member savedMember = memberRepository.findByEmail(command.email()).orElseThrow();
        assertThat(savedMember.getEmail()).isEqualTo(command.email());
        assertThat(savedMember.getEncodedPassword()).isEqualTo(new EncodedPassword("encoded:Abcde1!@"));
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
        Member existingMember = memberRepository.save(member().create(passwordEncoder));

        assertThatThrownBy(() -> memberCommandService.register(createCommand()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.DUPLICATE_EMAIL));
        assertThat(memberRepository.findAll()).containsExactly(existingMember);
    }

    @Test
    @DisplayName("이미 사용 중인 사용자 이름이면 회원을 등록할 수 없다")
    void rejectsDuplicateUsername() {
        Member existingMember = memberRepository.save(
                member().email("other@example.com").create(passwordEncoder)
        );

        assertThatThrownBy(() -> memberCommandService.register(createCommand()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.DUPLICATE_USERNAME));

        assertThat(memberRepository.findAll()).containsExactly(existingMember);
    }

    private RegisterMemberCommand createCommand() {
        return new RegisterMemberCommand(
                new Email("member@example.com"),
                new NewPassword("Abcde1!@"),
                new MemberProfile("member_name", "표시 이름", null, null)
        );
    }
}

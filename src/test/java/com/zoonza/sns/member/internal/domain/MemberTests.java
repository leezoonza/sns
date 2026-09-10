package com.zoonza.sns.member.internal.domain;

import com.zoonza.sns.member.internal.fixture.FakePasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.zoonza.sns.member.internal.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;

class MemberTests {

    private final PasswordEncoder passwordEncoder = new FakePasswordEncoder();

    @DisplayName("유효한 회원 정보가 주어지면 활성 상태의 회원을 생성한다")
    @Test
    void createsMember() {
        Member member = member().create(passwordEncoder);

        assertThat(member.getEmail()).isEqualTo(new Email("member@example.com"));
        assertThat(member.getEncodedPassword()).isEqualTo(new EncodedPassword("encoded:Abcde1!@"));
        assertThat(member.getProfile()).isEqualTo(new MemberProfile(
                "member_name",
                "표시 이름",
                null,
                null
        ));
        assertThat(member.getVisibility()).isEqualTo(AccountVisibility.PUBLIC);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getRegisteredAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isEqualTo(member.getRegisteredAt());
        assertThat(member.getWithdrawnAt()).isNull();
        assertThat(member.getLastLoginAt()).isNull();
    }
}

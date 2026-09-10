package com.zoonza.sns.member.internal.adapter.out.persistence;

import com.zoonza.sns.TestcontainersConfiguration;
import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.Member;
import com.zoonza.sns.member.internal.fixture.FakePasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static com.zoonza.sns.member.internal.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@Import({JpaMemberRepositoryAdapter.class, TestcontainersConfiguration.class})
class JpaMemberRepositoryAdapterTests {

    @Autowired
    private JpaMemberRepositoryAdapter memberRepository;

    @DisplayName("회원을 저장하고 이메일과 사용자 이름의 존재 여부를 조회한다")
    @Test
    void savesAndChecksMemberExistence() {
        Member savedMember = memberRepository.save(member().create(new FakePasswordEncoder()));

        assertThat(savedMember.getId()).isNotNull();
        assertThat(memberRepository.existsByEmail(new Email("member@example.com"))).isTrue();
        assertThat(memberRepository.existsByEmail(new Email("other@example.com"))).isFalse();
        assertThat(memberRepository.existsByUsername("member_name")).isTrue();
        assertThat(memberRepository.existsByUsername("other_name")).isFalse();
    }
}

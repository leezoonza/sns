package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.fixture.FakeMemberRepository;
import com.zoonza.sns.member.internal.fixture.FakePasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.zoonza.sns.member.internal.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;

class MemberQueryServiceTests {

    private FakeMemberRepository memberRepository;
    private MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberRepository();
        memberQueryService = new MemberQueryService(memberRepository);
    }

    @ParameterizedTest
    @DisplayName("이메일은 사용 중이지 않을 때만 사용할 수 있다")
    @CsvSource({"false, true", "true, false"})
    void checksEmailAvailability(boolean exists, boolean expectedAvailability) {
        if (exists) {
            memberRepository.save(member().create(new FakePasswordEncoder()));
        }

        boolean available = memberQueryService.isEmailAvailable(new Email("member@example.com"));

        assertThat(available).isEqualTo(expectedAvailability);
    }

    @ParameterizedTest
    @DisplayName("사용자 이름은 사용 중이지 않을 때만 사용할 수 있다")
    @CsvSource({"false, true", "true, false"})
    void checksUsernameAvailability(boolean exists, boolean expectedAvailability) {
        if (exists) {
            memberRepository.save(member().create(new FakePasswordEncoder()));
        }

        boolean available = memberQueryService.isUsernameAvailable("member_name");

        assertThat(available).isEqualTo(expectedAvailability);
    }
}

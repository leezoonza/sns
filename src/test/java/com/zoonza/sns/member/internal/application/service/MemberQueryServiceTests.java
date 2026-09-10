package com.zoonza.sns.member.internal.application.service;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTests {

    @Mock
    private MemberRepository memberRepository;

    private MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        memberQueryService = new MemberQueryService(memberRepository);
    }

    @ParameterizedTest
    @DisplayName("이메일은 사용 중이지 않을 때만 사용할 수 있다")
    @CsvSource({"false, true", "true, false"})
    void checksEmailAvailability(boolean exists, boolean expectedAvailability) {
        Email email = new Email("member@example.com");
        when(memberRepository.existsByEmail(email)).thenReturn(exists);

        boolean available = memberQueryService.isEmailAvailable(email);

        assertThat(available).isEqualTo(expectedAvailability);
    }

    @ParameterizedTest
    @DisplayName("사용자 이름은 사용 중이지 않을 때만 사용할 수 있다")
    @CsvSource({"false, true", "true, false"})
    void checksUsernameAvailability(boolean exists, boolean expectedAvailability) {
        String username = "member_name";
        when(memberRepository.existsByUsername(username)).thenReturn(exists);

        boolean available = memberQueryService.isUsernameAvailable(username);

        assertThat(available).isEqualTo(expectedAvailability);
    }
}

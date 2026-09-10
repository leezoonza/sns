package com.zoonza.sns.member.internal.integration;

import com.zoonza.sns.TestcontainersConfiguration;
import com.zoonza.sns.member.internal.adapter.in.dto.request.RegisterMemberRequest;
import com.zoonza.sns.member.internal.adapter.out.persistence.MemberJpaRepository;
import com.zoonza.sns.member.internal.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static com.zoonza.sns.member.internal.fixture.RegisterMemberRequestFixture.registerMemberRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class MemberSignupIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("회원가입 요청은 비밀번호를 인코딩하고 회원을 데이터베이스에 저장한다")
    @Test
    void signsUpAndPersistsMember() throws Exception {
        RegisterMemberRequest request = registerMemberRequest().create();

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated());

        assertThat(memberRepository.findAll()).singleElement().satisfies(member -> {
            assertThat(member.getEmail().value()).isEqualTo(request.email());
            assertThat(member.getProfile().username()).isEqualTo(request.username());
            assertThat(member.getProfile().displayName()).isEqualTo(request.displayName());
            assertThat(member.getProfile().bio()).isNull();
            assertThat(member.getProfile().profileImageUrl()).isNull();
            assertThat(member.getVisibility()).isEqualTo(AccountVisibility.PUBLIC);
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.getRole()).isEqualTo(MemberRole.MEMBER);
            assertThat(passwordEncoder.matches(
                    new RawPassword(request.rawPassword()),
                    member.getEncodedPassword())
            ).isTrue();
        });
    }
}

package com.zoonza.sns.member.internal.adapter.in;

import com.zoonza.sns.bootstrap.web.GlobalExceptionHandler;
import com.zoonza.sns.member.internal.adapter.in.dto.request.RegisterMemberRequest;
import com.zoonza.sns.member.internal.application.dto.RegisterMemberCommand;
import com.zoonza.sns.member.internal.application.port.in.MemberCommandUseCase;
import com.zoonza.sns.member.internal.application.port.in.MemberQueryUseCase;
import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.MemberErrorCode;
import com.zoonza.sns.shared.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static com.zoonza.sns.member.internal.fixture.RegisterMemberRequestFixture.registerMemberRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MemberControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberQueryUseCase memberQueryUseCase;

    @MockitoBean
    private MemberCommandUseCase memberCommandUseCase;

    @Test
    @DisplayName("이메일 사용 가능 여부를 조회한다")
    void getsEmailAvailability() throws Exception {
        Email email = new Email("member@example.com");
        when(memberQueryUseCase.isEmailAvailable(email)).thenReturn(true);

        mockMvc.perform(get("/api/members/emails/availability")
                        .param("email", email.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(memberQueryUseCase).isEmailAvailable(email);
    }

    @Test
    @DisplayName("사용자 이름 사용 가능 여부를 조회한다")
    void getsUsernameAvailability() throws Exception {
        String username = "member_name";
        when(memberQueryUseCase.isUsernameAvailable(username)).thenReturn(false);

        mockMvc.perform(get("/api/members/usernames/availability")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(memberQueryUseCase).isUsernameAvailable(username);
    }

    @Test
    @DisplayName("유효한 요청으로 회원가입한다")
    void signsUpMember() throws Exception {
        RegisterMemberRequest request = registerMemberRequest().create();

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegisterMemberCommand> commandCaptor =
                ArgumentCaptor.forClass(RegisterMemberCommand.class);
        verify(memberCommandUseCase).register(commandCaptor.capture());

        RegisterMemberCommand command = commandCaptor.getValue();
        assertThat(command.email().value()).isEqualTo("member@example.com");
        assertThat(command.rawPassword().value()).isEqualTo("Abcde1!@");
        assertThat(command.profile().username()).isEqualTo("member_name");
        assertThat(command.profile().displayName()).isEqualTo("표시 이름");
        assertThat(command.profile().bio()).isNull();
        assertThat(command.profile().profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("회원가입 요청이 정책을 만족하지 않으면 400을 응답한다")
    void rejectsInvalidSignupRequest() throws Exception {
        RegisterMemberRequest request = registerMemberRequest()
                .email("invalid-email")
                .create();

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-001"))
                .andExpect(jsonPath("$.detail").value("이메일 형식이 올바르지 않습니다."));

        verify(memberCommandUseCase, never()).register(any());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일로 회원가입하면 오류 메시지를 응답한다")
    void respondsWithDuplicateEmailMessage() throws Exception {
        RegisterMemberRequest request = registerMemberRequest().create();
        doThrow(new BusinessException(MemberErrorCode.DUPLICATE_EMAIL))
                .when(memberCommandUseCase)
                .register(any());

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER-001"))
                .andExpect(jsonPath("$.detail").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("잘못된 이메일로 사용 가능 여부를 조회하면 400을 응답한다")
    void rejectsInvalidEmailAvailabilityRequest() throws Exception {
        mockMvc.perform(get("/api/members/emails/availability")
                        .param("email", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-001"))
                .andExpect(jsonPath("$.detail").value("이메일 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("예외 메시지가 없으면 공통 기본 메시지를 응답한다")
    void respondsWithDefaultMessageWhenExceptionMessageIsMissing() throws Exception {
        String username = "member_name";
        when(memberQueryUseCase.isUsernameAvailable(username))
                .thenThrow(new IllegalArgumentException());

        mockMvc.perform(get("/api/members/usernames/availability")
                        .param("username", username))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-001"))
                .andExpect(jsonPath("$.detail").value("요청 값이 올바르지 않습니다."));
    }
}

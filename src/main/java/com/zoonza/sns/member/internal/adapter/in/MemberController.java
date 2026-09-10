package com.zoonza.sns.member.internal.adapter.in;

import com.zoonza.sns.member.internal.adapter.in.dto.request.RegisterMemberRequest;
import com.zoonza.sns.member.internal.adapter.in.dto.response.EmailAvailabilityResponse;
import com.zoonza.sns.member.internal.adapter.in.dto.response.UsernameAvailabilityResponse;
import com.zoonza.sns.member.internal.application.port.in.MemberCommandUseCase;
import com.zoonza.sns.member.internal.application.port.in.MemberQueryUseCase;
import com.zoonza.sns.member.internal.domain.Email;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberQueryUseCase memberQueryUseCase;
    private final MemberCommandUseCase memberCommandUseCase;

    @GetMapping("/emails/availability")
    public ResponseEntity<EmailAvailabilityResponse> isEmailAvailable(
            @RequestParam String email
    ) {
        boolean available = memberQueryUseCase.isEmailAvailable(new Email(email));

        return ResponseEntity.ok(new EmailAvailabilityResponse(available));
    }

    @GetMapping("/usernames/availability")
    public ResponseEntity<UsernameAvailabilityResponse> isUsernameAvailable(
            @RequestParam String username
    ) {
        boolean available = memberQueryUseCase.isUsernameAvailable(username);

        return ResponseEntity.ok(new UsernameAvailabilityResponse(available));
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody RegisterMemberRequest request
    ) {
        memberCommandUseCase.register(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}

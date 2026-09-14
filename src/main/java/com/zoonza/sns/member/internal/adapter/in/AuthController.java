package com.zoonza.sns.member.internal.adapter.in;

import com.zoonza.sns.member.internal.adapter.in.dto.request.LoginRequest;
import com.zoonza.sns.member.internal.adapter.in.dto.response.LoginResponse;
import com.zoonza.sns.member.internal.adapter.in.dto.response.ReissueResponse;
import com.zoonza.sns.member.internal.adapter.in.support.TokenCookieManager;
import com.zoonza.sns.member.internal.application.dto.result.TokenResult;
import com.zoonza.sns.member.internal.application.port.in.MemberAuthUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberAuthUseCase memberAuthUseCase;
    private final TokenCookieManager tokenCookieManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResult result = memberAuthUseCase.login(request.toCommand());
        ResponseCookie cookie = tokenCookieManager.createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(result.accessToken().value()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        TokenResult result = memberAuthUseCase.reissue(refreshToken);
        ResponseCookie cookie = tokenCookieManager.createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ReissueResponse(result.accessToken().value()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        memberAuthUseCase.logout(refreshToken);
        ResponseCookie cookie = tokenCookieManager.clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}

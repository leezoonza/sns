package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.adapter.in.dto.request.LoginRequest;

public final class LoginRequestFixture {
    private String email = "member@example.com";
    private String rawPassword = "Abcde1!@";

    private LoginRequestFixture() {
    }

    public static LoginRequestFixture loginRequest() {
        return new LoginRequestFixture();
    }

    public LoginRequestFixture email(String email) {
        this.email = email;
        return this;
    }

    public LoginRequestFixture rawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
        return this;
    }

    public LoginRequest create() {
        return new LoginRequest(email, rawPassword);
    }
}

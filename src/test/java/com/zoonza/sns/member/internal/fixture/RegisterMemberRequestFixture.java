package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.adapter.in.dto.request.RegisterMemberRequest;

public final class RegisterMemberRequestFixture {

    private String email = "member@example.com";
    private String rawPassword = "Abcde1!@";
    private String username = "member_name";
    private String displayName = "표시 이름";

    private RegisterMemberRequestFixture() {
    }

    public static RegisterMemberRequestFixture registerMemberRequest() {
        return new RegisterMemberRequestFixture();
    }

    public RegisterMemberRequestFixture email(String email) {
        this.email = email;
        return this;
    }

    public RegisterMemberRequestFixture rawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
        return this;
    }

    public RegisterMemberRequestFixture username(String username) {
        this.username = username;
        return this;
    }

    public RegisterMemberRequestFixture displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public RegisterMemberRequest create() {
        return new RegisterMemberRequest(email, rawPassword, username, displayName);
    }
}

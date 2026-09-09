package com.zoonza.sns.member.fixture;

import com.zoonza.sns.member.domain.*;

public final class MemberFixture {

    private String email = "member@example.com";
    private String rawPassword = "Abcde1!@";
    private String username = "member_name";
    private String displayName = "표시 이름";
    private String bio;
    private String profileImageUrl;

    private MemberFixture() {
    }

    public static MemberFixture member() {
        return new MemberFixture();
    }

    public MemberFixture email(String email) {
        this.email = email;
        return this;
    }

    public MemberFixture password(String rawPassword) {
        this.rawPassword = rawPassword;
        return this;
    }

    public MemberFixture username(String username) {
        this.username = username;
        return this;
    }

    public MemberFixture displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public MemberFixture bio(String bio) {
        this.bio = bio;
        return this;
    }

    public MemberFixture profileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public Member create(PasswordEncoder passwordEncoder) {
        return Member.of(
                new Email(email),
                new RawPassword(rawPassword),
                new MemberProfile(username, displayName, bio, profileImageUrl),
                passwordEncoder
        );
    }
}

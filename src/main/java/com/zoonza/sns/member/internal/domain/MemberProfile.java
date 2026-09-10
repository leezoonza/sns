package com.zoonza.sns.member.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record MemberProfile(
        @Column(unique = true, nullable = false)
        String username,
        @Column(nullable = false)
        String displayName,
        @Column
        String bio,
        @Column
        String profileImageUrl
) {
    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9._]{1,30}$");

    public MemberProfile {
        if (username == null || !PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("사용자 이름은 영문 소문자, 숫자, 점, 밑줄만 사용할 수 있습니다.");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        if (displayName.length() > 17) {
            throw new IllegalArgumentException("이름은 17자 이하여야 합니다.");
        }

        if (bio != null && bio.length() > 150) {
            throw new IllegalArgumentException("소개글은 150자 이하여야 합니다.");
        }
    }
}
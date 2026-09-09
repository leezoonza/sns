package com.zoonza.sns.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberProfileTests {

    @DisplayName("프로필 정보가 정책을 만족하면 회원 프로필을 생성한다")
    @Test
    void createsMemberProfileWhenValuesAreValid() {
        String username = "a".repeat(30);
        String displayName = "가".repeat(17);
        String bio = "가".repeat(150);

        MemberProfile profile = new MemberProfile(
                username,
                displayName,
                bio,
                "https://example.com/profile.png"
        );

        assertThat(profile.username()).isEqualTo(username);
        assertThat(profile.displayName()).isEqualTo(displayName);
        assertThat(profile.bio()).isEqualTo(bio);
        assertThat(profile.profileImageUrl()).isEqualTo("https://example.com/profile.png");
    }

    @DisplayName("사용자 이름이 정책을 만족하지 않으면 회원 프로필을 생성할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Member", "member-name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void rejectsInvalidUsername(String username) {
        assertThatThrownBy(() -> createProfile(username, "표시 이름", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 이름은 영문 소문자, 숫자, 점, 밑줄만 사용할 수 있습니다.");
    }

    @DisplayName("이름이 비어 있으면 회원 프로필을 생성할 수 없다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankDisplayName(String displayName) {
        assertThatThrownBy(() -> createProfile("member_name", displayName, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 필수입니다.");
    }

    @DisplayName("이름이 17자를 초과하면 회원 프로필을 생성할 수 없다")
    @Test
    void rejectsTooLongDisplayName() {
        assertThatThrownBy(() -> createProfile("member_name", "가".repeat(18), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 17자 이하여야 합니다.");
    }

    @DisplayName("소개글이 150자를 초과하면 회원 프로필을 생성할 수 없다")
    @Test
    void rejectsTooLongBio() {
        assertThatThrownBy(() -> createProfile("member_name", "표시 이름", "가".repeat(151)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("소개글은 150자 이하여야 합니다.");
    }

    private MemberProfile createProfile(String username, String displayName, String bio) {
        return new MemberProfile(username, displayName, bio, null);
    }
}

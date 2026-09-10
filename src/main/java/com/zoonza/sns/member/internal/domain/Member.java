package com.zoonza.sns.member.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private EncodedPassword encodedPassword;

    @Embedded
    private MemberProfile profile;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountVisibility visibility;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant withdrawnAt;

    @Column
    private Instant lastLoginAt;

    private Member(
            Email email,
            EncodedPassword encodedPassword,
            MemberProfile profile,
            AccountVisibility visibility,
            MemberStatus status,
            MemberRole role,
            Instant registeredAt,
            Instant updatedAt,
            Instant withdrawnAt,
            Instant lastLoginAt
    ) {
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.profile = profile;
        this.visibility = visibility;
        this.status = status;
        this.role = role;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
        this.withdrawnAt = withdrawnAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static Member of(
            Email email,
            RawPassword rawPassword,
            MemberProfile profile,
            PasswordEncoder passwordEncoder
    ) {
        Instant now = Instant.now();

        return new Member(
                email,
                passwordEncoder.encode(rawPassword),
                profile,
                AccountVisibility.PUBLIC,
                MemberStatus.ACTIVE,
                MemberRole.MEMBER,
                now,
                now,
                null,
                null
        );
    }
}

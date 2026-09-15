package com.zoonza.sns.follow.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_follow_followerId_followingId",
                columnNames = {"follower_id", "following_id"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long followerId;

    @Column(nullable = false)
    private Long followeeId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FollowStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant acceptedAt;
}

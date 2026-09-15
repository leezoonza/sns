package com.zoonza.sns.follow.internal.application.port.in;

public interface FollowCommandUseCase {
    void follow(Long memberId, Long followeeId);
}

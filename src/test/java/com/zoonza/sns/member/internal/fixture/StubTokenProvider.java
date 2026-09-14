package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.application.dto.IssuedToken;
import com.zoonza.sns.member.internal.application.port.out.TokenProvider;

import java.time.Instant;

public final class StubTokenProvider implements TokenProvider {

    private final IssuedToken issuedToken;
    private Long requestedMemberId;
    private String requestedMemberRole;
    private Instant requestedIssuedAt;

    public StubTokenProvider(IssuedToken issuedToken) {
        this.issuedToken = issuedToken;
    }

    @Override
    public IssuedToken issue(Long memberId, String memberRole, Instant issuedAt) {
        this.requestedMemberId = memberId;
        this.requestedMemberRole = memberRole;
        this.requestedIssuedAt = issuedAt;

        return issuedToken;
    }

    public Long requestedMemberId() {
        return requestedMemberId;
    }

    public String requestedMemberRole() {
        return requestedMemberRole;
    }

    public Instant requestedIssuedAt() {
        return requestedIssuedAt;
    }
}

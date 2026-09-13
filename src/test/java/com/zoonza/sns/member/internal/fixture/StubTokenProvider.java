package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.application.dto.IssuedToken;
import com.zoonza.sns.member.internal.application.port.out.TokenProvider;

public final class StubTokenProvider implements TokenProvider {

    private final IssuedToken issuedToken;
    private Long requestedMemberId;
    private String requestedMemberRole;

    public StubTokenProvider(IssuedToken issuedToken) {
        this.issuedToken = issuedToken;
    }

    @Override
    public IssuedToken issue(Long memberId, String memberRole) {
        this.requestedMemberId = memberId;
        this.requestedMemberRole = memberRole;

        return issuedToken;
    }

    public Long requestedMemberId() {
        return requestedMemberId;
    }

    public String requestedMemberRole() {
        return requestedMemberRole;
    }
}

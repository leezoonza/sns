package com.zoonza.sns.member.internal.application.dto;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.MemberProfile;
import com.zoonza.sns.member.internal.domain.RawPassword;

public record RegisterMemberCommand(
        Email email,
        RawPassword rawPassword,
        MemberProfile profile
) {
}

package com.zoonza.sns.member.application.dto;

import com.zoonza.sns.member.domain.Email;
import com.zoonza.sns.member.domain.MemberProfile;
import com.zoonza.sns.member.domain.RawPassword;

public record RegisterMemberCommand(
        Email email,
        RawPassword rawPassword,
        MemberProfile profile
) {
}

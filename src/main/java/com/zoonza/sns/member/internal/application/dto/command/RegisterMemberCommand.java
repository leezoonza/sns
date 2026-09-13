package com.zoonza.sns.member.internal.application.dto.command;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.MemberProfile;
import com.zoonza.sns.member.internal.domain.NewPassword;

public record RegisterMemberCommand(
        Email email,
        NewPassword newPassword,
        MemberProfile profile
) {
}

package com.zoonza.sns.member.internal.application.dto.result;

import com.zoonza.sns.member.internal.application.dto.AccessToken;
import com.zoonza.sns.member.internal.application.dto.RefreshToken;


public record TokenResult(
        AccessToken accessToken,
        RefreshToken refreshToken
) {
}

package com.zoonza.sns.member.internal.application.dto;

public record IssuedToken(
        AccessToken accessToken,
        RefreshToken refreshToken
) {
}

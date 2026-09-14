package com.zoonza.sns.member.internal.domain;

import com.zoonza.sns.shared.error.ErrorCode;

public enum AuthErrorCode implements ErrorCode {
    INVALID_CREDENTIAL("AUTH-001", "이메일 또는 비밀번호를 확인해 주세요", 401),
    INVALID_REFRESH_TOKEN("AUTH-002", "인증이 만료되었습니다. 다시 로그인해 주세요.", 401),
    ;

    private final String code;
    private final String message;
    private final int status;

    AuthErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getStatus() {
        return this.status;
    }
}

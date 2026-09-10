package com.zoonza.sns.member.internal.domain;

import com.zoonza.sns.common.ErrorCode;

public enum MemberErrorCode implements ErrorCode {
    DUPLICATE_EMAIL("USER-001", "이미 사용 중인 이메일입니다.", 409),
    DUPLICATE_USERNAME("USER-002", "이미 사용 중인 사용자 이름입니다.", 409),
    ;

    private final String code;
    private final String message;
    private final int status;

    MemberErrorCode(String code, String message, int status) {
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

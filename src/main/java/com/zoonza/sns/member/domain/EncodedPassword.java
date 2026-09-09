package com.zoonza.sns.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record EncodedPassword(
        @Column(name = "password", nullable = false)
        String value
) {
        public EncodedPassword {
                if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException("비밀번호는 필수입니다.");
                }
        }
}

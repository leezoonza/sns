package com.zoonza.sns.member.internal.domain;

public interface MemberRepository {
    boolean existsByEmail(Email email);

    boolean existsByUsername(String username);

    Member save(Member member);
}

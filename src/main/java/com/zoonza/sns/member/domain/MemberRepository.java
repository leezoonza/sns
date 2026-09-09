package com.zoonza.sns.member.domain;

public interface MemberRepository {
    boolean existsByEmail(Email email);

    boolean existsByUsername(String username);

    Member save(Member member);
}

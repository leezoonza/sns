package com.zoonza.sns.member.internal.domain;

import java.util.Optional;

public interface MemberRepository {
    boolean existsByEmail(Email email);

    boolean existsByUsername(String username);

    Member save(Member member);

    Optional<Member> findByEmail(Email email);

    Optional<Member> findById(Long memberId);
}

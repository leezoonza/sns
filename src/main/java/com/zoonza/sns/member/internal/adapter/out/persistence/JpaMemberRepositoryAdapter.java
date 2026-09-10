package com.zoonza.sns.member.internal.adapter.out.persistence;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.Member;
import com.zoonza.sns.member.internal.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaMemberRepositoryAdapter implements MemberRepository {
    private final MemberJpaRepository jpaRepository;

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return  jpaRepository.existsByProfileUsername(username);
    }

    @Override
    public Member save(Member member) {
        return jpaRepository.save(member);
    }
}

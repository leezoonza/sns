package com.zoonza.sns.member.internal.adapter.out.persistence;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(Email email);

    boolean existsByProfileUsername(String username);
}

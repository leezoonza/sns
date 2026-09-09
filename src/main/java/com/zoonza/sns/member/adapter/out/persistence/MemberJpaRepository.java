package com.zoonza.sns.member.adapter.out.persistence;

import com.zoonza.sns.member.domain.Email;
import com.zoonza.sns.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(Email email);

    boolean existsByProfileUsername(String username);
}

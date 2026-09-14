package com.zoonza.sns.member.internal.fixture;

import com.zoonza.sns.member.internal.domain.Email;
import com.zoonza.sns.member.internal.domain.Member;
import com.zoonza.sns.member.internal.domain.MemberRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> members = new LinkedHashMap<>();
    private long nextId = 1L;

    @Override
    public boolean existsByEmail(Email email) {
        return members.values().stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public boolean existsByUsername(String username) {
        return members.values().stream()
                .anyMatch(member -> member.getProfile().username().equals(username));
    }

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            ReflectionTestUtils.setField(member, "id", nextId++);
        }

        members.put(member.getId(), member);
        nextId = Math.max(nextId, member.getId() + 1);

        return member;
    }

    @Override
    public Optional<Member> findByEmail(Email email) {
        return members.values().stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return Optional.ofNullable(members.get(memberId));
    }

    public List<Member> findAll() {
        return List.copyOf(members.values());
    }
}

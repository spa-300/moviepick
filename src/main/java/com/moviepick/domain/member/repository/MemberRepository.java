package com.moviepick.domain.member.repository;

import com.moviepick.domain.member.entity.AuthProvider;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 카카오·구글 회원 조회
    Optional<Member> findByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );

    // 관리자: 회원 상태별 목록 조회
    Page<Member> findByStatus(
            MemberStatus status,
            Pageable pageable
    );


    // 관리자 제재 처리 시 회원별 동시 승인 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :memberId")
    Optional<Member> findByIdForUpdate(
            @Param("memberId") Long memberId
    );

}


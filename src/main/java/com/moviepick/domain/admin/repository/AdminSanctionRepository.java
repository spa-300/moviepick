package com.moviepick.domain.admin.repository;

import com.moviepick.domain.admin.entity.AdminSanction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSanctionRepository
        extends JpaRepository<AdminSanction, Long> {

    // 특정 회원에게 적용된 누적 제재 횟수 조회
    long countByMemberId(Long memberId);

    // 동일 신고가 이미 제재 처리되었는지 확인
    boolean existsByReportId(Long reportId);
}

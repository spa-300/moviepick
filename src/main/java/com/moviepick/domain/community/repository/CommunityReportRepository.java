
package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.CommunityReport;
import com.moviepick.domain.community.entity.ReportTargetType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityReportRepository
        extends JpaRepository<CommunityReport, Long> {

    // 기존 기능: 동일 사용자의 중복 신고 확인
    boolean existsByReporterIdAndTargetTypeAndTargetId(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId
    );

    // 관리자: 신고 처리 시 중복 승인 방지를 위한 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM CommunityReport r WHERE r.id = :reportId")
    Optional<CommunityReport> findByIdForUpdate(
            @Param("reportId") Long reportId
    );
}

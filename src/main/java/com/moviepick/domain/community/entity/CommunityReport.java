
package com.moviepick.domain.community.entity;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "community_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_reporter_target",
                        columnNames = {"reporter_id", "target_type", "target_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_report_target_status",
                        columnList = "target_type, target_id, status"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 20)
    private String reason;

    @Column(length = 200)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    private CommunityReport(
            Member reporter,
            ReportTargetType targetType,
            Long targetId,
            String reason,
            String detail
    ) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
    }

    public static CommunityReport create(
            Member reporter,
            ReportTargetType targetType,
            Long targetId,
            String reason,
            String detail
    ) {
        if (reporter == null) {
            throw new IllegalArgumentException("신고자는 필수입니다.");
        }

        if (targetType == null) {
            throw new IllegalArgumentException("신고 대상 유형은 필수입니다.");
        }

        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("신고 대상 ID가 올바르지 않습니다.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("신고 유형은 필수입니다.");
        }

        if (detail != null && detail.length() > 200) {
            throw new IllegalArgumentException("상세 사유는 200자 이하여야 합니다.");
        }

        thisReasonValidation(reason);

        if ("ETC".equals(reason) && (detail == null || detail.isBlank())) {
            throw new IllegalArgumentException("기타 신고는 상세 사유가 필수입니다.");
        }

        return new CommunityReport(
                reporter,
                targetType,
                targetId,
                reason.trim(),
                detail == null ? null : detail.trim()
        );
    }

    private static void thisReasonValidation(String reason) {
        if (!java.util.Set.of("SPAM", "ABUSE", "SPOILER", "ADULT", "ETC")
                .contains(reason)) {
            throw new IllegalArgumentException("올바르지 않은 신고 유형입니다.");
        }
    }

    // 관리자가 신고를 승인하거나 반려
    public void process(ReportStatus newStatus) {

        // 이미 처리한 신고는 다시 처리할 수 없음
        if (this.status != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        // 승인 또는 반려만 허용
        if (newStatus != ReportStatus.ACCEPTED
                && newStatus != ReportStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "신고는 승인 또는 반려만 가능합니다."
            );
        }

        this.status = newStatus;
    }


}

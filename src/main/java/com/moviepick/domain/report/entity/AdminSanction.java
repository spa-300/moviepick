package com.moviepick.domain.report.entity;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_sanctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminSanction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제재 대상 회원
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 제재의 원인이 된 승인 신고
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private CommunityReport report;

    // 누적 승인 신고 횟수
    @Column(nullable = false)
    private int sanctionCount;

    // 제재 종료 시각 (null이면 영구 정지)
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    public static AdminSanction create(
            Member member,
            CommunityReport report,
            int sanctionCount,
            LocalDateTime suspendedUntil
    ) {
        AdminSanction sanction = new AdminSanction();
        sanction.member = member;
        sanction.report = report;
        sanction.sanctionCount = sanctionCount;
        sanction.suspendedUntil = suspendedUntil;
        return sanction;
    }
}

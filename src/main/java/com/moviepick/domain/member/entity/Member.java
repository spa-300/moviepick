package com.moviepick.domain.member.entity;

import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_members_email_provider",
                        columnNames = {"email", "provider"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    public static Member createLocal(
            String email,
            String passwordHash,
            String nickname
    ) {
        Member member = new Member();
        member.provider = AuthProvider.LOCAL;
        member.email = email;
        member.passwordHash = passwordHash;
        member.nickname = nickname;
        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public static Member createSocial(
            AuthProvider provider,
            String providerId,
            String email,
            String nickname
    ) {
        if (provider == AuthProvider.LOCAL) {
            throw new IllegalArgumentException(
                    "소셜 회원은 카카오 또는 구글 제공자를 사용해야 합니다."
            );
        }

        Member member = new Member();
        member.provider = provider;
        member.providerId = providerId;
        member.email = email;
        member.nickname = nickname.length() > 50
                ? nickname.substring(0, 50)
                : nickname;
        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void suspend(LocalDateTime until) {
        this.status = MemberStatus.SUSPENDED;
        this.suspendedUntil = until;
    }

    public void releaseSuspension() {
        this.status = MemberStatus.ACTIVE;
        this.suspendedUntil = null;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }


    public boolean releaseSuspensionIfExpired() {

        // 정지 상태가 아니면 아무 작업도 하지 않음
        if (this.status != MemberStatus.SUSPENDED) {
            return false;
        }

        // 종료일이 없으면 영구 정지이므로 해제하지 않음
        if (this.suspendedUntil == null) {
            return false;
        }

        // 정지 종료 시간이 지났다면 자동 해제
        if (!this.suspendedUntil.isAfter(LocalDateTime.now())) {
            releaseSuspension();
            return true;
        }

        return false;
    }

}
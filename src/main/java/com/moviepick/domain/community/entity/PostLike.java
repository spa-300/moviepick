package com.moviepick.domain.community.entity;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_like_member",
                        columnNames = {"post_id", "member_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private PostLike(Post post, Member member) {
        this.post = post;
        this.member = member;
    }

    public static PostLike create(Post post, Member member) {
        if (post == null) {
            throw new IllegalArgumentException("좋아요 대상 게시글은 필수입니다.");
        }
        if (member == null) {
            throw new IllegalArgumentException("좋아요를 누른 회원은 필수입니다.");
        }

        if (post.getStatus() != PostStatus.VISIBLE) {
            throw new IllegalArgumentException("삭제된 게시글에는 좋아요를 누를 수 없습니다.");
        }

        return new PostLike(post, member);
    }
}
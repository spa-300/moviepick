package com.moviepick.domain.community.entity;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글이 달린 게시글
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 댓글 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status = CommentStatus.VISIBLE;

    // 댓글 생성
    public static Comment create(Post post, Member member, String content) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다.");
        }
        if (member == null) {
            throw new IllegalArgumentException("댓글 작성자는 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        Comment comment = new Comment();
        comment.post = post;
        comment.member = member;
        comment.content = content;
        comment.status = CommentStatus.VISIBLE;

        return comment;
    }

    // 댓글 논리 삭제
    public void delete() {
        this.status = CommentStatus.DELETED;
    }

    // 댓글 수정
    public void update(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        if (this.status != CommentStatus.VISIBLE) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        this.content = content;
    }
}
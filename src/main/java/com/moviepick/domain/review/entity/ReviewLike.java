package com.moviepick.domain.review.entity;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "review_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_likes_member_id_review_id",
                columnNames = {"member_id", "review_id"}),
        indexes = @Index(name = "idx_review_likes_review_id", columnList = "review_id"))
public class ReviewLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static ReviewLike create(Review review, Member member) {
        ReviewLike like = new ReviewLike();
        like.review = review;
        like.member = member;
        return like;
    }
}
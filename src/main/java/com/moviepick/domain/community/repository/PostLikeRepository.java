package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 회원이 특정 게시글에 좋아요를 눌렀는지 확인
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    // 특정 회원이 특정 게시글에 누른 좋아요 조회
    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);

    // 게시글의 좋아요 수 조회
    long countByPostId(Long postId);
}
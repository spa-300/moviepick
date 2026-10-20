package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 목록 조회 (오래된 댓글부터)
    List<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId);
}
package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글 목록 조회
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    // 삭제되지 않은 게시글 상세 조회
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);
}
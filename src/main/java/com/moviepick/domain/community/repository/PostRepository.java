package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
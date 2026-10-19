package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.request.PostCreateRequest;
import com.moviepick.domain.community.dto.response.PostCreateResponse;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostCreateResponse create(PostCreateRequest request) {
        Post post = Post.create(
                request.title(),
                request.content()
        );

        Post savedPost = postRepository.save(post);

        return PostCreateResponse.from(savedPost);
    }
}
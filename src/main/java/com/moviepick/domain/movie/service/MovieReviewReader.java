package com.moviepick.domain.movie.service;

import java.util.Optional;

/**
 * 영화 상세가 로그인 회원의 리뷰 번호를 얻는 창구.
 * 리뷰 도메인이 구현하므로 영화 도메인은 리뷰 클래스를 직접 알지 않는다 (review → movie 한 방향).
 */
public interface MovieReviewReader {

    // 회원이 이 영화에 쓴 공개 리뷰 번호 (비로그인이거나 없으면 빈 값)
    Optional<Long> findMyReviewId(Long memberId, Long movieId);
}

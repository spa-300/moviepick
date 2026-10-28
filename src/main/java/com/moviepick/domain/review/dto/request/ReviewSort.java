package com.moviepick.domain.review.dto.request;

import com.moviepick.domain.review.exception.ReviewErrorCode;
import com.moviepick.global.exception.BusinessException;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum ReviewSort {

    LATEST("latest", Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    LIKES("likes", Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    // 평점순은 숨긴 리뷰(BLIND)를 맨 뒤로 보내 위치로 숨긴 평점이 드러나지 않게 한다.
    // 상태는 문자열로 저장되고 목록에는 VISIBLE·BLIND만 나오므로 status 내림차순이면 VISIBLE이 먼저다
    RATING_DESC("ratingDesc", Sort.by(Sort.Order.desc("status"), Sort.Order.desc("rating"),
            Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    RATING_ASC("ratingAsc", Sort.by(Sort.Order.desc("status"), Sort.Order.asc("rating"),
            Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

    private final String param;
    private final Sort sort;

    ReviewSort(String param, Sort sort) {
        this.param = param;
        this.sort = sort;
    }

    public static ReviewSort from(String value) {
        return Arrays.stream(values())
                .filter(s -> s.param.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.INVALID_SORT));
    }
}
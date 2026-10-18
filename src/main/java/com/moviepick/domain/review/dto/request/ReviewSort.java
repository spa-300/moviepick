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
    RATING_DESC("ratingDesc", Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    RATING_ASC("ratingAsc", Sort.by(Sort.Order.asc("rating"), Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

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
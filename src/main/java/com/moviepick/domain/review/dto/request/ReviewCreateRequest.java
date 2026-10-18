package com.moviepick.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ReviewCreateRequest(
        @NotNull(message = "평점을 입력해 주세요.")
        BigDecimal rating,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 1000, message = "내용은 1000자 이내로 입력해 주세요.")
        String content,

        Boolean spoiler
) {
    public boolean spoilerOrDefault() {
        return Boolean.TRUE.equals(spoiler);
    }
}
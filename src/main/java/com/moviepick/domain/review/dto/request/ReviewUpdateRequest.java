package com.moviepick.domain.review.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ReviewUpdateRequest(
        BigDecimal rating,

        @Size(min = 1, max = 1000, message = "내용은 1~1000자로 입력해 주세요.")
        String content,

        Boolean spoiler
) {
    @JsonIgnore
    @AssertTrue(message = "수정할 항목을 하나 이상 입력해 주세요.")
    public boolean isAnyFieldPresent() {
        return rating != null || content != null || spoiler != null;
    }
}
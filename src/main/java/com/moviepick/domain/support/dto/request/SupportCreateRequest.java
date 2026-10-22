package com.moviepick.domain.support.dto.request;

import jakarta.validation.constraints.NotNull;

public record SupportCreateRequest(
        @NotNull(message = "후원 금액은 필수입니다.")
        Long amount
) {
}

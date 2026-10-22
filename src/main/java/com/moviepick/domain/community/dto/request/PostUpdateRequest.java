
package com.moviepick.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(

        @NotBlank(message = "제목은 공백일 수 없습니다.")
        @Size(min = 1, max = 100, message = "제목은 1~100자여야 합니다.")
        String title,

        @NotBlank(message = "내용은 공백일 수 없습니다.")
        @Size(min = 1, max = 5000, message = "내용은 1~5000자여야 합니다.")
        String content

) {
}

package com.moviepick.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityReportRequest {

    @NotBlank(message = "신고 사유는 필수입니다.")
    @Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.")
    private String reason;
}
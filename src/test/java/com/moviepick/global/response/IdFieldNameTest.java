package com.moviepick.global.response;

import com.moviepick.domain.report.dto.response.AdminReportResponse;
import com.moviepick.domain.report.entity.CommunityReport;
import com.moviepick.domain.report.entity.ReportTargetType;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.movie.dto.response.MovieDetailResponse;
import com.moviepick.domain.movie.dto.response.MovieListResponse;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.entity.MovieGenre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// API-01: 응답 번호 필드는 이름을 붙인다 (memberId, reportId, movieId, genreId)
class IdFieldNameTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private void assertIdField(JsonNode json, String name, long value) {
        assertThat(json.has("id")).as("이름 없는 id 필드").isFalse();
        assertThat(json.get(name)).as(name).isNotNull();
        assertThat(json.get(name).asLong()).isEqualTo(value);
    }

    private Member member(Long id) {
        Member member = Member.createLocal("m" + id + "@test.com", "encoded", "m" + id);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Movie movie() {
        Movie movie = new Movie(100L, "movie");
        ReflectionTestUtils.setField(movie, "id", 7L);
        return movie;
    }

    @Test
    @DisplayName("회원 응답(가입, 내 정보, 관리자 회원 목록·해제): memberId")
    void memberResponse() {
        JsonNode json = objectMapper.valueToTree(MemberResponse.from(member(3L)));

        assertIdField(json, "memberId", 3L);
    }

    @Test
    @DisplayName("관리자 신고 응답(목록·처리): reportId")
    void adminReportResponse() {
        CommunityReport report = CommunityReport.create(member(1L), ReportTargetType.POST, 9L, "SPAM", null);
        ReflectionTestUtils.setField(report, "id", 5L);

        assertIdField(objectMapper.valueToTree(AdminReportResponse.from(report)), "reportId", 5L);
        assertIdField(objectMapper.valueToTree(AdminReportResponse.from(report, "t", "p", "n")), "reportId", 5L);
    }

    @Test
    @DisplayName("영화 목록 응답: movieId")
    void movieListResponse() {
        assertIdField(objectMapper.valueToTree(MovieListResponse.from(movie())), "movieId", 7L);
    }

    @Test
    @DisplayName("영화 상세 응답: movieId, genres[].genreId")
    void movieDetailResponse() {
        Movie movie = movie();
        JsonNode json = objectMapper.valueToTree(
                MovieDetailResponse.from(movie, List.of(new MovieGenre(movie, 28, "액션")), null));

        assertIdField(json, "movieId", 7L);
        assertIdField(json.get("genres").get(0), "genreId", 28L);
    }
}

package com.moviepick.domain.movie.controller;

import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.service.MovieCollectionService;
import com.moviepick.global.auth.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminMovieController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AdminMovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieCollectionService movieCollectionService;

    @Test
    @DisplayName("관리자 영화 동기화 API가 수집 결과를 반환한다")
    void syncMovies_success() throws Exception {

        // given
        MovieSyncResponse response =
                new MovieSyncResponse(1, 2, 1);

        given(movieCollectionService
                .collectPopularMoviesWithResult(1))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/movies/sync")
                        .param("pages", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestedPages").value(1))
                .andExpect(jsonPath("$.data.savedCount").value(2))
                .andExpect(jsonPath("$.data.failedCount").value(1));

        verify(movieCollectionService)
                .collectPopularMoviesWithResult(1);
    }
}

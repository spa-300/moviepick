package com.moviepick.domain.movie.batch.tasklet;

import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.service.MovieCollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieSyncTaskletTest {

    // 1. 영화 수집 성공 테스트
    @Test
    void movieSyncSuccess() {

        // 실제 서비스 대신 가짜 객체 생성
        MovieCollectionService collectionService =
                mock(MovieCollectionService.class);

        MovieSyncTasklet tasklet =
                new MovieSyncTasklet(collectionService);

        // 20편 성공, 0편 실패했다고 가정
        when(collectionService.collectPopularMoviesWithResult(1))
                .thenReturn(new MovieSyncResponse(1, 20, 0));

        // Tasklet 실행
        RepeatStatus result = tasklet.execute(null, null);

        // 정상적으로 종료되었는지 검증
        assertEquals(RepeatStatus.FINISHED, result);

        verify(collectionService)
                .collectPopularMoviesWithResult(1);
    }

    // 2. 영화 수집 실패 테스트
    @Test
    void movieSyncFail() {

        MovieCollectionService collectionService =
                mock(MovieCollectionService.class);

        MovieSyncTasklet tasklet =
                new MovieSyncTasklet(collectionService);

        // 18편 성공, 2편 실패했다고 가정
        when(collectionService.collectPopularMoviesWithResult(1))
                .thenReturn(new MovieSyncResponse(1, 18, 2));

        // 실패 결과면 예외가 발생해야 함
        assertThrows(
                IllegalStateException.class,
                () -> tasklet.execute(null, null)
        );

        verify(collectionService)
                .collectPopularMoviesWithResult(1);
    }
}
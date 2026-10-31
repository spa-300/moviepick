package com.moviepick.domain.movie.batch;

import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.service.MovieCollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "tmdb.access-token=test-dummy-token",
        "movie.batch.collect-pages=3",
        "movie.batch.scheduler.enabled=false"
})
class MovieBatchPagesTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    @Qualifier("movieSyncJob")
    private Job movieSyncJob;

    @MockitoBean
    private MovieCollectionService movieCollectionService;

    @Test
    void movieSyncJobUsesConfiguredPages() throws Exception {

        // given: 3페이지 수집에 성공했다고 가정
        when(movieCollectionService.collectPopularMoviesWithResult(3))
                .thenReturn(new MovieSyncResponse(3, 60, 0));

        var parameters = new JobParametersBuilder()
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();

        // when: 실제 Spring Batch Job 실행
        JobExecution execution =
                jobOperator.start(movieSyncJob, parameters);

        // then: 성공 여부와 3페이지 호출 검증
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        verify(movieCollectionService, times(1))
                .collectPopularMoviesWithResult(3);
    }
}
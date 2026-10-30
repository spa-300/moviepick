
package com.moviepick.domain.movie.batch;

import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.service.MovieCollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class MovieBatchRunTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    @Qualifier("movieSyncJob")
    private Job movieSyncJob;

    // 실제 TMDB 수집 서비스 대신 Mock 객체 사용
    @MockitoBean
    private MovieCollectionService movieCollectionService;

    // 매 테스트마다 고유한 실행 파라미터 생성
    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();
    }

    @Test
    void movieSyncJobSuccess() throws Exception {

        // 1. 영화 20편 수집 성공 상황 가정
        when(movieCollectionService.collectPopularMoviesWithResult(1))
                .thenReturn(new MovieSyncResponse(1, 20, 0));

        // 2. 실제 Spring Batch Job 실행
        JobExecution execution =
                jobOperator.start(movieSyncJob, createJobParameters());

        // 3. Job이 성공했는지 확인
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        verify(movieCollectionService)
                .collectPopularMoviesWithResult(1);
    }

    @Test
    void movieSyncJobFail() throws Exception {

        // 1. 영화 18편 성공, 2편 실패 상황 가정
        when(movieCollectionService.collectPopularMoviesWithResult(1))
                .thenReturn(new MovieSyncResponse(1, 18, 2));

        // 2. 실제 Spring Batch Job 실행
        JobExecution execution =
                jobOperator.start(movieSyncJob, createJobParameters());

        // 3. Job이 실패로 기록됐는지 확인
        assertEquals(BatchStatus.FAILED, execution.getStatus());

        verify(movieCollectionService)
                .collectPopularMoviesWithResult(1);
    }
}

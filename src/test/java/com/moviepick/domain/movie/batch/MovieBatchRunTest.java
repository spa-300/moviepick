package com.moviepick.domain.movie.batch;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MovieBatchRunTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    @Qualifier("movieSyncJob")
    private Job movieSyncJob;

    @Test
    void movieSyncJobTest() throws Exception {

        // 1. 실행할 작업에 고유한 파라미터 부여
        JobParameters parameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        // 2. 배치 작업 실행
        JobExecution execution =
                jobOperator.start(movieSyncJob, parameters);

        // 3. 정상 종료 여부 검증
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        System.out.println("JobRepository = "
                + jobRepository.getClass().getName());

        try (var connection = dataSource.getConnection()) {
            System.out.println("DB URL = "
                    + connection.getMetaData().getURL());

            System.out.println("DB NAME = "
                    + connection.getCatalog());
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION",
                Integer.class
        );

        System.out.println("Batch 실행 기록 수 = " + count);
    }
}
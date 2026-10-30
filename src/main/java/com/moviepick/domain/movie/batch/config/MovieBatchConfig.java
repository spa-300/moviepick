package com.moviepick.domain.movie.batch.config;

import com.moviepick.domain.movie.batch.tasklet.MovieSyncTasklet;
import com.moviepick.domain.movie.service.MovieCollectionService;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MovieBatchConfig {

    // 1. TMDB 영화 수집 Tasklet 등록
    @Bean
    public MovieSyncTasklet movieSyncTasklet(
            MovieCollectionService movieCollectionService,
            @Value("${movie.batch.collect-pages:1}") int collectPages
    ) {
        return new MovieSyncTasklet(movieCollectionService, collectPages);
    }

    // 2. 영화 수집 Step 등록
    @Bean
    public Step movieSyncStep(
            JobRepository jobRepository,
            @Qualifier("movieSyncTasklet") MovieSyncTasklet tasklet
    ) {
        return new StepBuilder("movieSyncStep", jobRepository)
                .tasklet(tasklet)
                .build();
    }

    // 3. 영화 수집 Job 등록
    @Bean
    public Job movieSyncJob(
            JobRepository jobRepository,
            @Qualifier("movieSyncStep") Step movieSyncStep
    ) {
        return new JobBuilder("movieSyncJob", jobRepository)
                .start(movieSyncStep)
                .build();
    }
}

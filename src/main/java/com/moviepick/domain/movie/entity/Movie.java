package com.moviepick.domain.movie.entity;

import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movies")
public class Movie extends BaseEntity {

    // 내부 영화 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TMDB 영화 고유 ID
    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Long tmdbId;

    // 영화 제목
    @Column(nullable = false, length = 500)
    private String title;

    // 영화 원제
    @Column(name = "original_title", length = 500)
    private String originalTitle;

    // 원어 코드
    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    // 영화 줄거리
    @Column(columnDefinition = "TEXT")
    private String overview;

    // 개봉일
    @Column(name = "release_date")
    private LocalDate releaseDate;

    // 상영 시간 (분)
    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    // 영화 상태
    @Column(length = 50)
    private String status;

    // 성인 영화 여부
    @Column(nullable = false)
    private boolean adult;

    // TMDB 평균 평점
    @Column(name = "vote_average")
    private Double voteAverage;

    // TMDB 평가 참여자 수
    @Column(name = "vote_count")
    private Integer voteCount;

    // TMDB 인기도
    private Double popularity;

    // 포스터 이미지 경로
    @Column(name = "poster_path")
    private String posterPath;

    // 배경 이미지 경로
    @Column(name = "backdrop_path")
    private String backdropPath;

    // 서비스 노출 여부
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // 마지막 TMDB 동기화 성공 시각
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    // 최초 영화 생성
    public Movie(Long tmdbId, String title) {
        this.tmdbId = tmdbId;
        this.title = title;
    }

    // TMDB 동기화 완료 시각 기록
    public void markSynced(LocalDateTime syncedAt) {
        this.lastSyncedAt = syncedAt;
    }
}

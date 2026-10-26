package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.client.dto.TmdbMovieResponse;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.entity.MovieGenre;
import com.moviepick.domain.movie.model.MovieInfo;
import com.moviepick.domain.movie.repository.MovieGenreRepository;
import com.moviepick.domain.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
// 수집한 영화·장르 저장 및 동기화
public class MovieSaveService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;

    // 영화 1편 단위 저장 및 갱신
    @Transactional
    public Long saveOne(TmdbMovieResponse response) {

        // 1. 필수 데이터 검증
        if (response == null
                || response.id() == null
                || response.title() == null
                || response.title().isBlank()
                || response.adult() == null
                || response.genres() == null) {
            throw new IllegalArgumentException(
                    "TMDB 영화 필수 정보가 없습니다."
            );
        }

        // 2. 기존 영화 조회
        Optional<Movie> existing =
                movieRepository.findByTmdbId(response.id());

        // 3. 기존 영화가 없다면 새 엔티티 생성
        Movie movie = existing.orElseGet(() ->
                new Movie(response.id(), response.title())
        );

        // 4. TMDB 응답을 MovieInfo로 변환
        LocalDate releaseDate =
                response.releaseDate() == null
                        || response.releaseDate().isBlank()
                        ? null
                        : LocalDate.parse(response.releaseDate());

        MovieInfo info = new MovieInfo(
                response.title(),
                response.originalTitle(),
                response.originalLanguage(),
                response.overview(),
                releaseDate,
                response.runtime(),
                response.status(),
                response.adult(),
                response.voteAverage(),
                response.voteCount(),
                response.popularity(),
                response.posterPath(),
                response.backdropPath()
        );

        // 5. 영화 정보 갱신
        movie.updateDetails(info);

        // 6. 신규 영화라면 DB에 저장
        if (existing.isEmpty()) {
            movieRepository.save(movie);
        }

        // 7. 영화 장르 동기화
        syncGenres(movie, response.genres());

        // 8. 동기화 성공 시각 기록
        movie.markSynced(LocalDateTime.now());

        return movie.getId();
    }

    // 영화 장르 동기화
    private void syncGenres(
            Movie movie,
            List<TmdbMovieResponse.GenreResponse> genres
    ) {

        // 1. 기존 장르 목록 조회
        List<MovieGenre> existingGenres =
                movieGenreRepository.findAllByMovie_Id(movie.getId());

        Map<Integer, MovieGenre> existingMap = new HashMap<>();

        for (MovieGenre genre : existingGenres) {
            existingMap.put(genre.getTmdbGenreId(), genre);
        }

        Set<Integer> receivedIds = new HashSet<>();
        List<MovieGenre> newGenres = new ArrayList<>();

        // 2. TMDB에서 받은 장르 처리
        for (TmdbMovieResponse.GenreResponse genre : genres) {

            if (genre == null || genre.id() == null
                    || genre.name() == null) {
                throw new IllegalArgumentException(
                        "TMDB 장르 정보가 올바르지 않습니다."
                );
            }

            // 응답 내부 중복 장르 방지
            if (!receivedIds.add(genre.id())) {
                continue;
            }

            MovieGenre existingGenre =
                    existingMap.get(genre.id());

            if (existingGenre != null) {
                // 기존 장르 이름 갱신
                existingGenre.updateGenreName(genre.name());
            } else {
                // 신규 장르 생성
                newGenres.add(
                        new MovieGenre(
                                movie,
                                genre.id(),
                                genre.name()
                        )
                );
            }
        }

        // 3. TMDB에서 제거된 장르 삭제
        List<MovieGenre> removedGenres =
                existingGenres.stream()
                        .filter(genre ->
                                !receivedIds.contains(
                                        genre.getTmdbGenreId()
                                )
                        )
                        .toList();

        movieGenreRepository.deleteAll(removedGenres);

        // 4. 새 장르 저장
        movieGenreRepository.saveAll(newGenres);
    }
}

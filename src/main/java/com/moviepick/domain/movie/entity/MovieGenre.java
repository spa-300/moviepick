package com.moviepick.domain.movie.entity;

import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "movie_genres",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_movie_genre",
                columnNames = {"movie_id", "tmdb_genre_id"}
        )
)
public class MovieGenre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 영화와 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    // TMDB 장르 ID
    @Column(name = "tmdb_genre_id", nullable = false)
    private Integer tmdbGenreId;

    // 장르 이름
    @Column(name = "genre_name", nullable = false)
    private String genreName;

    public MovieGenre(
            Movie movie,
            Integer tmdbGenreId,
            String genreName
    ) {
        this.movie = movie;
        this.tmdbGenreId = tmdbGenreId;
        this.genreName = genreName;
    }

    // 장르 이름 수정
    public void updateGenreName(String genreName) {
        this.genreName = genreName;
    }
}

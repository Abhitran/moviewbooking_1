package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {

    List<Show> findByScreen_ScreenId(UUID screenId);

    boolean existsByScreen_ScreenIdAndShowDateAndShowTime(
        UUID screenId, LocalDate showDate, java.time.LocalTime showTime);

    @Query("""
        SELECT sh FROM Show sh
        JOIN sh.screen s
        JOIN s.theatre t
        WHERE t.theatreId = :theatreId
          AND (:movieName IS NULL OR LOWER(sh.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')))
          AND (:showDate IS NULL OR sh.showDate = :showDate)
          AND (:language IS NULL OR LOWER(sh.language) = LOWER(:language))
          AND (:genre IS NULL OR LOWER(sh.genre) = LOWER(:genre))
          AND t.status = 'APPROVED'
        """)
    List<Show> findShowsByFilters(
        @Param("theatreId") UUID theatreId,
        @Param("movieName") String movieName,
        @Param("showDate") LocalDate showDate,
        @Param("language") String language,
        @Param("genre") String genre
    );

    @Query("""
        SELECT sh FROM Show sh
        JOIN sh.screen s
        JOIN s.theatre t
        WHERE (:city IS NULL OR LOWER(t.city) = LOWER(:city))
          AND (:movieName IS NULL OR LOWER(sh.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')))
          AND (:showDate IS NULL OR sh.showDate = :showDate)
          AND (:language IS NULL OR LOWER(sh.language) = LOWER(:language))
          AND (:genre IS NULL OR LOWER(sh.genre) = LOWER(:genre))
          AND t.status = 'APPROVED'
        """)
    List<Show> searchShows(
        @Param("city") String city,
        @Param("movieName") String movieName,
        @Param("showDate") LocalDate showDate,
        @Param("language") String language,
        @Param("genre") String genre
    );

    Optional<Show> findByShowIdAndScreen_Theatre_PartnerId(UUID showId, UUID partnerId);
}

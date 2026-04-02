package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowRepository extends JpaRepository<Show, UUID> {
    
    List<Show> findByScreenId(UUID screenId);
    
    List<Show> findByMovieNameAndShowDate(String movieName, LocalDate showDate);
    
    @Query("SELECT s FROM Show s WHERE s.screen.id = :screenId " +
           "AND s.showDate = :showDate AND s.showTime = :showTime")
    Optional<Show> findByScreenIdAndShowDateAndShowTime(
        @Param("screenId") UUID screenId,
        @Param("showDate") LocalDate showDate,
        @Param("showTime") LocalTime showTime
    );
    
    @Query("SELECT s FROM Show s " +
           "WHERE s.screen.theatre.id = :theatreId " +
           "AND s.movieName = :movieName " +
           "AND s.showDate = :date")
    List<Show> findByTheatreAndMovieAndDate(
        @Param("theatreId") UUID theatreId,
        @Param("movieName") String movieName,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.seats " +
           "WHERE s.id = :showId")
    Optional<Show> findByIdWithSeats(@Param("showId") UUID showId);
}

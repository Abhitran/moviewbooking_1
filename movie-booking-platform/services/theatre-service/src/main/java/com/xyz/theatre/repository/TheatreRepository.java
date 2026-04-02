package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Theatre;
import com.xyz.theatre.entity.TheatreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
=======

>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
import java.util.List;
import java.util.Optional;
import java.util.UUID;

<<<<<<< HEAD
@Repository
public interface TheatreRepository extends JpaRepository<Theatre, UUID> {
    
    List<Theatre> findByPartnerId(UUID partnerId);
    
    List<Theatre> findByCity(String city);
    
    List<Theatre> findByStatus(TheatreStatus status);
    
    Optional<Theatre> findByIdAndPartnerId(UUID id, UUID partnerId);
    
    @Query("SELECT DISTINCT t FROM Theatre t " +
           "JOIN t.screens s " +
           "JOIN s.shows sh " +
           "WHERE t.city = :city " +
           "AND t.status = 'APPROVED' " +
           "AND sh.movieName = :movieName " +
           "AND sh.showDate = :date")
    List<Theatre> findTheatresShowingMovie(
        @Param("city") String city,
        @Param("movieName") String movieName,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT DISTINCT t FROM Theatre t " +
           "JOIN FETCH t.screens s " +
           "JOIN FETCH s.shows sh " +
           "WHERE t.city = :city " +
           "AND t.status = 'APPROVED' " +
           "AND sh.movieName = :movieName " +
           "AND sh.showDate = :date " +
           "AND (:language IS NULL OR sh.language = :language) " +
           "AND (:genre IS NULL OR sh.genre = :genre)")
    List<Theatre> searchTheatres(
        @Param("city") String city,
        @Param("movieName") String movieName,
        @Param("date") LocalDate date,
        @Param("language") String language,
        @Param("genre") String genre
=======
public interface TheatreRepository extends JpaRepository<Theatre, UUID> {

    List<Theatre> findByPartnerId(UUID partnerId);

    List<Theatre> findByCityIgnoreCase(String city);

    Optional<Theatre> findByTheatreIdAndPartnerId(UUID theatreId, UUID partnerId);

    @Query("""
        SELECT DISTINCT t FROM Theatre t
        JOIN t.screens s
        JOIN s.shows sh
        WHERE (:city IS NULL OR LOWER(t.city) = LOWER(:city))
          AND (:movieName IS NULL OR LOWER(sh.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')))
          AND t.status = 'APPROVED'
        """)
    List<Theatre> searchTheatres(
        @Param("city") String city,
        @Param("movieName") String movieName
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    );
}

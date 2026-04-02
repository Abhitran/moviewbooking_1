package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Theatre;
import com.xyz.theatre.entity.TheatreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    );
}

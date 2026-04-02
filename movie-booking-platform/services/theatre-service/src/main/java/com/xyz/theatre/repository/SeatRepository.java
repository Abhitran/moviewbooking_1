package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Seat;
import com.xyz.theatre.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByShow_ShowId(UUID showId);

    List<Seat> findByShow_ShowIdAndStatus(UUID showId, SeatStatus status);

    Optional<Seat> findByShow_ShowIdAndSeatNumber(UUID showId, String seatNumber);

    boolean existsByShow_ShowIdAndSeatNumber(UUID showId, String seatNumber);

    long countByShow_ShowId(UUID showId);

    long countByShow_ShowIdAndStatus(UUID showId, SeatStatus status);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :status WHERE s.show.showId = :showId AND s.seatNumber = :seatNumber")
    int updateSeatStatus(@Param("showId") UUID showId,
                         @Param("seatNumber") String seatNumber,
                         @Param("status") SeatStatus status);
}

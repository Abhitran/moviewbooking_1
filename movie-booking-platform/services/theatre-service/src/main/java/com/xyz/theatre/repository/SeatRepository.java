package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Seat;
import com.xyz.theatre.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;
=======
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3

import java.util.List;
import java.util.Optional;
import java.util.UUID;

<<<<<<< HEAD
@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    
    List<Seat> findByShowId(UUID showId);
    
    List<Seat> findByShowIdAndStatus(UUID showId, SeatStatus status);
    
    Optional<Seat> findByShowIdAndSeatNumber(UUID showId, String seatNumber);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.show.id = :showId AND s.status = :status")
    long countByShowIdAndStatus(
        @Param("showId") UUID showId,
        @Param("status") SeatStatus status
    );
    
    @Modifying
    @Query("UPDATE Seat s SET s.status = :status " +
           "WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers")
    int updateSeatStatus(
        @Param("showId") UUID showId,
        @Param("seatNumbers") List<String> seatNumbers,
        @Param("status") SeatStatus status
    );
    
    boolean existsByShowIdAndSeatNumber(UUID showId, String seatNumber);
=======
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
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
}

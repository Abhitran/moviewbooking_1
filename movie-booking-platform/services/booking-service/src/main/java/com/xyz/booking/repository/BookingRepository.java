package com.xyz.booking.repository;

import com.xyz.booking.entity.Booking;
import com.xyz.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    List<Booking> findByShowId(UUID showId);

    List<Booking> findByShowIdAndStatus(UUID showId, BookingStatus status);

    Optional<Booking> findByBookingIdAndUserId(UUID bookingId, UUID userId);

    List<Booking> findByBookingIdInAndUserId(List<UUID> bookingIds, UUID userId);
}

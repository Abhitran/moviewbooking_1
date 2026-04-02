package com.xyz.booking.repository;

import com.xyz.booking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {

    List<BookingSeat> findByBooking_BookingId(UUID bookingId);

    List<BookingSeat> findByBooking_ShowIdAndBooking_StatusIn(
        UUID showId, List<com.xyz.booking.entity.BookingStatus> statuses);
}

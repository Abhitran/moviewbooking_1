package com.xyz.theatre.entity;

import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"show_id", "seat_number"})
})
=======
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seats",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_show_seat_number",
        columnNames = {"show_id", "seat_number"}
    ))
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@EntityListeners(AuditingEntityListener.class)
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seat_id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
    
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
=======
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seat_id", updatable = false, nullable = false)
    private UUID seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Show show;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
}

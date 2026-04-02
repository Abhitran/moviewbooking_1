package com.xyz.theatre.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seats",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_show_seat_number",
        columnNames = {"show_id", "seat_number"}
    ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

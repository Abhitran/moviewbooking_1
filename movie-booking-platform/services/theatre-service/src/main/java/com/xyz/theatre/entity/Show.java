package com.xyz.theatre.entity;

import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
=======
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
<<<<<<< HEAD
@Table(name = "shows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "show_date", "show_time"})
})
=======
@Table(name = "shows",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_screen_date_time",
        columnNames = {"screen_id", "show_date", "show_time"}
    ))
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@EntityListeners(AuditingEntityListener.class)
public class Show {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "show_id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;
    
    @Column(name = "movie_name", nullable = false)
    private String movieName;
    
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;
    
    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @Column(length = 50)
    private String language;
    
    @Column(length = 50)
    private String genre;
    
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
=======
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "show_id", updatable = false, nullable = false)
    private UUID showId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Screen screen;

    @Column(name = "movie_name", nullable = false)
    private String movieName;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "language")
    private String language;

    @Column(name = "genre")
    private String genre;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Seat> seats = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
}

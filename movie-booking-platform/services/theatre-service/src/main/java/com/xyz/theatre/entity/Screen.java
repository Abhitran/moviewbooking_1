package com.xyz.theatre.entity;

import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
=======
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "screens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@EntityListeners(AuditingEntityListener.class)
public class Screen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "screen_id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "seat_layout", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> seatLayout;
    
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Show> shows = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
=======
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "screen_id", updatable = false, nullable = false)
    private UUID screenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Theatre theatre;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "seat_layout", columnDefinition = "jsonb")
    private Map<String, Object> seatLayout;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Show> shows = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    private LocalDateTime createdAt;
}

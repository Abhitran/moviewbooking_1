package com.xyz.theatre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "theatres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Theatre {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "theatre_id")
    private UUID id;
    
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private TheatreStatus status = TheatreStatus.PENDING_APPROVAL;
    
    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Screen> screens = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public void addScreen(Screen screen) {
        screens.add(screen);
        screen.setTheatre(this);
    }
    
    public void removeScreen(Screen screen) {
        screens.remove(screen);
        screen.setTheatre(null);
    }
}

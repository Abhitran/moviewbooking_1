package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, UUID> {
    
    List<Screen> findByTheatreId(UUID theatreId);
    
    @Query("SELECT s FROM Screen s WHERE s.id = :screenId AND s.theatre.id = :theatreId")
    Optional<Screen> findByIdAndTheatreId(
        @Param("screenId") UUID screenId,
        @Param("theatreId") UUID theatreId
    );
    
    @Query("SELECT COUNT(s) FROM Screen s WHERE s.theatre.id = :theatreId")
    long countByTheatreId(@Param("theatreId") UUID theatreId);
}

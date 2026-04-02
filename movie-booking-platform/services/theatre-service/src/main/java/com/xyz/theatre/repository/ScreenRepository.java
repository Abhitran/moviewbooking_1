package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
=======
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3

import java.util.List;
import java.util.Optional;
import java.util.UUID;

<<<<<<< HEAD
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
=======
public interface ScreenRepository extends JpaRepository<Screen, UUID> {

    List<Screen> findByTheatre_TheatreId(UUID theatreId);

    Optional<Screen> findByScreenIdAndTheatre_TheatreId(UUID screenId, UUID theatreId);
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
}

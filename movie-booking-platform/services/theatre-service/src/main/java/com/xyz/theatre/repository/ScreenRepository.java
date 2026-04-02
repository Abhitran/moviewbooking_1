package com.xyz.theatre.repository;

import com.xyz.theatre.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScreenRepository extends JpaRepository<Screen, UUID> {

    List<Screen> findByTheatre_TheatreId(UUID theatreId);

    Optional<Screen> findByScreenIdAndTheatre_TheatreId(UUID screenId, UUID theatreId);
}

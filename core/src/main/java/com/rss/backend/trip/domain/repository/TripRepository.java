package com.rss.backend.trip.domain.repository;

import com.rss.backend.trip.domain.entity.Trip;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByIdAndDriverId(Long id, Long driverId);
}

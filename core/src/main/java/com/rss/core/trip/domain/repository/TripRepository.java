package com.rss.core.trip.domain.repository;

import com.rss.core.trip.domain.entity.Trip;
import com.rss.core.trip.domain.entity.Trip.TripStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByIdAndDriverId(Long id, Long driverId);

    List<Trip> findAllByRiderId(Long riderId);

    boolean existsByDriverIdAndStatusNotIn(Long driverId, List<TripStatus> tripStatus);

    boolean existsByRiderIdAndStatusNotIn(Long riderId, List<TripStatus> tripStatus);
}

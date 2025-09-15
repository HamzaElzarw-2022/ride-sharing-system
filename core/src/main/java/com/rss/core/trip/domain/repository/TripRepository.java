package com.rss.core.trip.domain.repository;

import com.rss.core.trip.domain.entity.Trip;
import com.rss.core.trip.domain.entity.Trip.TripStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByIdAndDriverId(Long id, Long driverId);

    List<Trip> findAllByRiderId(Long riderId);

    List<Trip> findAllByDriverId(Long driverId);

    boolean existsByDriverIdAndStatusNotIn(Long driverId, List<TripStatus> tripStatus);

    boolean existsByRiderIdAndStatusNotIn(Long riderId, List<TripStatus> tripStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Trip t SET t.driverId = :driverId, t.status = com.rss.core.trip.domain.entity.Trip$TripStatus.PICKING_UP WHERE t.id = :tripId AND t.status = com.rss.core.trip.domain.entity.Trip$TripStatus.MATCHING AND t.driverId IS NULL")
    int acceptTripIfMatching(@Param("tripId") Long tripId, @Param("driverId") Long driverId);
}

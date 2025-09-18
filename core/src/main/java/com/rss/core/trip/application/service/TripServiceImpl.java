package com.rss.core.trip.application.service;

import com.rss.core.location.LocationInternalApi;
import com.rss.core.map.MapInternalApi;
import com.rss.core.trip.api.internal.TripInternalApi;
import com.rss.core.trip.application.dto.TripDto;
import com.rss.core.trip.application.port.in.RequestDriverService;
import com.rss.core.trip.application.port.in.TripService;
import com.rss.core.trip.application.port.out.NotificationService;
import com.rss.core.trip.domain.entity.Trip;
import com.rss.core.trip.domain.entity.Trip.TripStatus;
import com.rss.core.trip.domain.event.TripCreatedEvent;
import com.rss.core.trip.domain.event.TripEndedEvent;
import com.rss.core.trip.domain.event.TripMatchedEvent;
import com.rss.core.trip.domain.event.TripStartedEvent;
import com.rss.core.trip.domain.repository.TripRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService, TripInternalApi {
    private final TripRepository tripRepository;
    private final LocationInternalApi locationInternalApi;
    private final MapInternalApi mapInternalApi;
    private final RequestDriverService requestDriverService;
    private final TripMatchingTracker tripMatchingTracker;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TripDto createTrip(Long riderId, Point start, Point end) {
        if(tripRepository.existsByRiderIdAndStatusNotIn(riderId,
                List.of(TripStatus.COMPLETED, TripStatus.CANCELLED, TripStatus.NO_DRIVERS_MATCHED)))
            throw new IllegalStateException("Driver is already in a trip");

        Trip trip = Trip.builder()
                .riderId(riderId)
                .status(TripStatus.MATCHING)
                .startPoint(start)
                .endPoint(end)
                .createdAt(LocalDateTime.now())
                .build();

        tripRepository.save(trip);
        // Track trip in MATCHING state for timeout handling
        try {
            long createdAtMillis = java.time.ZonedDateTime.now().toInstant().toEpochMilli();
            tripMatchingTracker.addMatchingTrip(trip.getId(), createdAtMillis);
        } catch (Exception ignored) {}
        requestDriverService.requestDriver(trip);
        //System.out.println("Rider=" + riderId + ", CREATED trip=" + trip.getId());
        eventPublisher.publishEvent(new TripCreatedEvent(trip.getId(), trip.getRiderId(),
                trip.getCreatedAt(), trip.getStartPoint(), trip.getEndPoint()));
        return toDto(trip);
    }

    @Override
    @Transactional
    public TripDto acceptTrip(Long driverId, Long tripId) {
        if(tripRepository.existsByDriverIdAndStatusNotIn(driverId,
                List.of(TripStatus.COMPLETED, TripStatus.CANCELLED, TripStatus.NO_DRIVERS_MATCHED)))
            throw new IllegalStateException("Driver is already in a trip");

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if(trip.getStatus() != TripStatus.MATCHING)
            throw new IllegalStateException("Trip is not in MATCHING status");
        else if(!requestDriverService.isDriverRequestedForTrip(driverId, tripId))
            throw new IllegalStateException("Driver was not requested for this trip");

        int updated = tripRepository.acceptTripIfMatching(tripId, driverId);
        if (updated == 0) {
            //System.out.println("About the same time acceptance resolved!!");
            throw new IllegalStateException("Trip was already accepted or is no longer in MATCHING state");
        }
        // reload latest state
        Trip updatedTrip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found after update"));
        // remove from matching tracking since it is accepted
        try { tripMatchingTracker.removeMatchingTrip(tripId); } catch (Exception ignored) {}

        //System.out.println("Driver=" + driverId + ", ACCEPTED trip=" + tripId);
        eventPublisher.publishEvent(new TripMatchedEvent(updatedTrip.getId(), updatedTrip.getDriverId()));
        return toDto(updatedTrip);
    }

    @Override
    public TripDto startTrip(Long driverId, Long tripId) {
        Trip trip = tripRepository.findByIdAndDriverId(tripId, driverId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if(trip.getStatus() != TripStatus.PICKING_UP)
            throw new IllegalStateException("Trip is not in PICKING_UP state");

        // Validate driver is at expected location.
        Point driverLocation = locationInternalApi.getDriverLocation(trip.getDriverId());
        if(driverLocation == null) {
            log.error("driver {} location is null, cant start trip {}", trip.getDriverId(), trip.getId());
            throw new IllegalStateException("Driver location not found");
        }
        if(!mapInternalApi.isAtExpectedOrProjection(driverLocation, trip.getStartPoint()))
            log.warn("driver {} is NOT at user location, started trip {}, driverLocation={}, expectedLocation={}",
                    trip.getDriverId(), trip.getId(), driverLocation, trip.getStartPoint());

        trip.setStatus(TripStatus.STARTED);
        trip.setStartTime(LocalDateTime.now());
        tripRepository.save(trip);

        //System.out.println("Driver=" + driverId + ", STARTED trip=" + tripId + ", actual=" + driverLocation + ", expected=" + trip.getStartPoint());
        eventPublisher.publishEvent(new TripStartedEvent(trip.getId(), trip.getStartTime()));
        return toDto(trip);
    }

    @Override
    public void endTrip(Long driverId, Long tripId) {
        Trip trip = tripRepository.findByIdAndDriverId(tripId, driverId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if(trip.getStatus() != TripStatus.STARTED)
            throw new IllegalStateException("Trip is not in PICKING_UP state");

        // Validate driver is at destination.
        Point driverLocation = locationInternalApi.getDriverLocation(trip.getDriverId());
        if(driverLocation == null) {
            log.error("driver {} location is null, cant end trip {}", trip.getDriverId(), trip.getId());
            throw new IllegalStateException("Driver location not found");
        }
        if(!mapInternalApi.isAtExpectedOrProjection(driverLocation, trip.getEndPoint()))
            log.warn("driver {} is NOT at destination, ending trip {}, actual={}, expected={}",
                    trip.getDriverId(), trip.getId(), driverLocation, trip.getEndPoint());

        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        tripRepository.save(trip);

        //System.out.println("Driver=" + driverId + ", ENDED trip=" + tripId + ", actual=" + driverLocation + ", expected=" + trip.getEndPoint());
        eventPublisher.publishEvent(new TripEndedEvent(trip.getId(), trip.getEndTime()));
        notificationService.NotifyRiderTripEnded(trip.getRiderId(), trip.getId());
    }

    @Override
    public List<TripDto> getAllActiveTrips() {
        return tripRepository.findAllByStatusIn(List.of(
                        TripStatus.MATCHING,
                        TripStatus.PICKING_UP,
                        TripStatus.STARTED))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public TripDto getTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        return toDto(trip);
    }

    @Override
    public List<TripDto> getRiderTrips(Long riderId) {
        return tripRepository.findAllByRiderId(riderId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<TripDto> getDriverTrips(Long driverId) {
        return tripRepository.findAllByDriverId(driverId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private TripDto toDto(Trip trip) {
        if (trip == null) return null;

        return TripDto.builder()
                .id(trip.getId())
                .status(trip.getStatus())
                .riderId(trip.getRiderId())
                .driverId(trip.getDriverId())
                .startX(trip.getStartPoint() != null ? trip.getStartPoint().getX() : null)
                .startY(trip.getStartPoint() != null ? trip.getStartPoint().getY() : null)
                .destX(trip.getEndPoint() != null ? trip.getEndPoint().getX() : null)
                .destY(trip.getEndPoint() != null ? trip.getEndPoint().getY() : null)
                .createdAt(trip.getCreatedAt())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .fare(trip.getFare())
                .build();
    }
}

package com.rss.core.trip.application.service;

import com.rss.core.location.LocationInternalApi;
import com.rss.core.map.MapInternalApi;
import com.rss.core.trip.application.dto.TripDto;
import com.rss.core.trip.application.port.in.RequestDriverService;
import com.rss.core.trip.application.port.in.TripService;
import com.rss.core.trip.domain.entity.Trip;
import com.rss.core.trip.domain.entity.Trip.TripStatus;
import com.rss.core.trip.domain.repository.TripRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    private final TripRepository tripRepository;
    private final LocationInternalApi locationInternalApi;
    private final MapInternalApi mapInternalApi;
    private final RequestDriverService requestDriverService;

    @Override
    public TripDto createTrip(Long riderId, Point start, Point end) {
        if(tripRepository.existsByRiderIdAndStatusNotIn(riderId, List.of(TripStatus.COMPLETED, TripStatus.CANCELLED)))
            throw new IllegalStateException("Driver is already in a trip");

        Trip trip = Trip.builder()
                .riderId(riderId)
                .status(TripStatus.MATCHING)
                .startPoint(start)
                .endPoint(end)
                .createdAt(LocalDateTime.now())
                .build();

        tripRepository.save(trip);
        requestDriverService.requestDriver(trip);
        return toDto(trip);
    }

    @Override
    public TripDto acceptTrip(Long driverId, Long tripId) {
        if(tripRepository.existsByDriverIdAndStatusNotIn(driverId, List.of(TripStatus.COMPLETED, TripStatus.CANCELLED)))
            throw new IllegalStateException("Driver is already in a trip");

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if(trip.getStatus() != TripStatus.MATCHING)
            throw new IllegalStateException("Trip is not in MATCHING status");
        else if(!requestDriverService.isDriverRequestedForTrip(driverId, tripId))
            throw new IllegalStateException("Driver was not requested for this trip");

        trip.setDriverId(driverId);
        trip.setStatus(TripStatus.PICKING_UP);
        tripRepository.save(trip);

        return toDto(trip);
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
    }

    @Override
    public TripDto getTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        return toDto(trip);
    }

    @Override
    public List<TripDto> getTripHistory(Long riderId) {
        return tripRepository.findAllByRiderId(riderId)
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
                .startLatitude(trip.getStartPoint() != null ? trip.getStartPoint().getY() : null)
                .startLongitude(trip.getStartPoint() != null ? trip.getStartPoint().getX() : null)
                .endLatitude(trip.getEndPoint() != null ? trip.getEndPoint().getY() : null)
                .endLongitude(trip.getEndPoint() != null ? trip.getEndPoint().getX() : null)
                .createdAt(trip.getCreatedAt())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .fare(trip.getFare())
                .build();
    }
}

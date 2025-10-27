package com.rss.core.trip.application.service;

import com.rss.core.location.DriverLocation;
import com.rss.core.location.LocationInternalApi;
import com.rss.core.trip.api.internal.TripInternalApi;
import com.rss.core.trip.application.dto.TripDto;
import com.rss.core.trip.application.ws.TripWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DriverLocationScheduler {

    private final TripInternalApi tripInternalApi;
    private final LocationInternalApi locationInternalApi;
    private final TripWebSocketHandler tripWebSocketHandler;

    @Scheduled(fixedRate = 1000)
    public void broadcastDriverLocationsToRiders() {
        List<TripDto> activeTrips = tripInternalApi.getAllActiveTrips();
        for (TripDto trip : activeTrips) {
            if (trip.getDriverId() != null) {
                DriverLocation driverLocation = locationInternalApi.getDriverLocation(trip.getDriverId());
                tripWebSocketHandler.sendDriverLocation(trip.getId(), trip.getDriverId(), driverLocation);
            }
        }
    }
}


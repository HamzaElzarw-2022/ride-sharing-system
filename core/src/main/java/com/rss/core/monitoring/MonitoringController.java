package com.rss.core.monitoring;

import com.rss.core.location.LocationInternalApi;
import com.rss.core.trip.api.internal.TripInternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final TripInternalApi tripInternalApi;
    private final LocationInternalApi locationInternalApi;

    @GetMapping("/snapshot")
    public MonitoringSnapshot snapshot() {
        return new MonitoringSnapshot(
                tripInternalApi.getAllActiveTrips(),
                locationInternalApi.getAllDriverLocations()
        );
    }
}

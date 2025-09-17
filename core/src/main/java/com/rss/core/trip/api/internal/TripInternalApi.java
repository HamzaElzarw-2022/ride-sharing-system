package com.rss.core.trip.api.internal;

import com.rss.core.trip.application.dto.TripDto;
import java.util.List;

public interface TripInternalApi {
    /**
     * Get all active trips (status: REQUESTED, MATCHED, IN_PROGRESS)
     */
    List<TripDto> getAllActiveTrips();
}

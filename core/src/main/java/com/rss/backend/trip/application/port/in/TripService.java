package com.rss.backend.trip.application.port.in;

import com.rss.backend.trip.application.dto.TripDto;
import java.util.List;
import org.springframework.data.geo.Point;

public interface TripService {

    /*
    * user  a trip, after that a driver is requested based on the user's location, trip status is marked as MATCHING,
    * this is the only method that the user call in trip lifecycle.
    *
    * notes:
    * validate user is not in a trip.
    * */
    TripDto createTrip(Long userId, Point startNodeId, Point endNodeId);

    /*
    * driver accepts the trip, trip status is marked as PICKING_UP,
    * driver simulation start moving towards the user's location.
    *
    * notes:
    * validate driver is not in a trip.
    * validate trip is in MATCHING status.
    * validate driver was requested for the trip.
    * */
    TripDto acceptTrip(Long driverId, Long tripId);

    /*
    * driver starts the trip after arriving to user location, trip status is marked as STARTED,
    * service should validate that driver is at user location.
    * */
    TripDto startTrip(Long driverId, Long tripId);

    /*
    * driver ends the trip after arriving to destination, trip status is marked as COMPLETED,
    * service should validate that driver is at destination location,
    * trigger payment process.
    * */
    void endTrip(Long driverId, Long tripId);

    TripDto getTrip(Long tripId);
    List<TripDto> getTripHistory(Long riderId);
}

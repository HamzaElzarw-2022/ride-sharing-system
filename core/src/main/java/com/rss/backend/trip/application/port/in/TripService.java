package com.rss.backend.trip.application.port.in;

import org.springframework.data.geo.Point;

public interface TripService {

    /*
    * user  a trip, after that a driver is requested based on the user's location, trip status is marked as MATCHING,
    * this is the only method that the user call in trip lifecycle.
    *
    * notes:
    * validate user is not in a trip.
    * */
    void createTrip(Long userId, Point startNodeId, Point endNodeId);

    /*
    * driver accepts the trip, trip status is marked as PICKING_UP,
    * driver simulation start moving towards the user's location.
    *
    * notes:
    * validate driver is not in a trip.
    * validate trip is in MATCHING status.
    * validate driver was requested for the trip.
    * */
    void acceptTrip(Long driverId, Long tripId);

    /*
    * driver starts the trip after arriving to user location, trip status is marked as STARTED,
    * service should validate that driver is at user location.
    * */
    void startTrip(Long driverId, Long tripId);

    /*
    * driver ends the trip after arriving to destination, trip status is marked as COMPLETED,
    * service should validate that driver is at destination location,
    * trigger payment process.
    * */
    void endTrip(Long driverId, Long tripId);

    void getTripStatus(Long tripId);
    void getTripHistory(Long riderId);
}

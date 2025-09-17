package com.rss.core.location;

import java.util.Map;
import java.util.Set;
import org.springframework.data.geo.Point;

public interface LocationInternalApi {

    /**
     * Retrieve driver location in map unit; returns null if absent.
     */
    Point getDriverLocation(Long driverId);

    /**
     * Find nearby drivers.
     * @param x X coordinate
     * @param y Y coordinate
     * @param radiusUnits radius in MAP UNITS (not meters). Must be > 0.
     * @return set of driver usernames within the radius (excluding none)
     * Validation: radiusUnits must be positive and not NaN/Infinite; otherwise empty set is returned.
     */
    Set<Long> findDriversWithinRadius(double x, double y, double radiusUnits);

    /**
     * Find nearby drivers by starting with a default radius and progressively increasing it
     * until drivers are found or a maximum threshold is reached.
     * Implementation should return an empty set if none are found within the threshold.
     */
    Set<Long> findNearbyDrivers(double x, double y);

   /**
    * Get the locations of all available drivers
    */
    Map<Long, Point> getAllDriverLocations();
}

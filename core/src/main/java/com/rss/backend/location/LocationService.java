package com.rss.backend.location;

import java.util.Set;
import org.springframework.data.geo.Point;

public interface LocationService {

    /**
     * Store driver position in map unit coordinates (x,y)
     */
    void updateDriverLocation(Long driverId, double x, double y);

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
}

package com.rss.core.location;

import java.util.Set;

public interface LocationService {

    /**
     * Store driver position in map unit coordinates (x,y) with orientation degree
     */
    void updateDriverLocation(Long driverId, double x, double y, double degree);

    /**
     * Retrieve driver location in map unit with degree; returns null if absent.
     */
    DriverLocation getDriverLocation(Long driverId);

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

package com.rss.backend.location;

import java.util.Set;
import org.springframework.data.geo.Point;

public interface LocationInternalApi {
    Set<Long> findDriversWithinRadius(double x, double y, double radiusUnits);
    Point getDriverLocation(Long driverId);
}

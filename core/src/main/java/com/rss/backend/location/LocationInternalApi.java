package com.rss.backend.location;

import java.util.Set;

public interface LocationInternalApi {
    Set<Long> findDriversWithinRadius(double x, double y, double radiusUnits);
}

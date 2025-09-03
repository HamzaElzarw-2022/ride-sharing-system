package com.rss.backend.location;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Removed unused imports
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.geo.GeoResults;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final StringRedisTemplate stringRedisTemplate;
    private final GeoCoordinateMapper geoCoordinateMapper;

    private static final String KEY_DRIVERS = "drivers";

    /**
     * Store driver position using internal normalized map coordinates (x,y)
     */
    public void updateDriverLocationInternal(double x, double y) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Point redisPoint = geoCoordinateMapper.toRedisPoint(x, y);
        stringRedisTemplate.opsForGeo().add(KEY_DRIVERS, redisPoint, username);
    }

    /**
     * Retrieve driver location as raw Redis GEO point (lon,lat)
     */
    private Point getDriverLocation(String username) {
        List<Point> pts = stringRedisTemplate.opsForGeo().position(KEY_DRIVERS, username);
        return (pts == null || pts.isEmpty()) ? null : pts.getFirst();
    }

    /**
     * Retrieve driver location mapped back to internal normalized coordinates; returns null if absent.
     */
    public double[] getDriverLocationInternal(String username) {
        Point p = getDriverLocation(username);
        if (p == null) return null;
        return geoCoordinateMapper.fromRedisPoint(p);
    }

    /**
     * Find nearby drivers.
     * @param x internal normalized X coordinate
     * @param y internal normalized Y coordinate
     * @param radiusUnits radius in INTERNAL MAP UNITS (not meters). Must be > 0 and finite.
     * @return set of driver usernames within the radius (excluding none)
     * Validation: radiusUnits must be positive and not NaN/Infinite; otherwise empty set is returned.
     */
    public Set<String> findDriversWithinRadiusInternal(double x, double y, double radiusUnits) {
        if (radiusUnits <= 0 || Double.isNaN(radiusUnits) || Double.isInfinite(radiusUnits)) {
            return Set.of();
        }
        Point center = geoCoordinateMapper.toRedisPoint(x, y);
        double radiusMeters = geoCoordinateMapper.unitsToMeters(radiusUnits);
        Distance distance = new Distance(radiusMeters, Metrics.METERS);

        GeoResults<GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                KEY_DRIVERS,
                GeoReference.fromCoordinate(center),
                distance
        );

        if (results == null || results.getContent().isEmpty()) return Set.of();
        return results.getContent().stream()
                .map(r -> r.getContent().getName())
                .collect(Collectors.toSet());
    }
}
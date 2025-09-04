package com.rss.backend.location;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.geo.GeoResults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService implements LocationInternalApi {
    private final StringRedisTemplate stringRedisTemplate;
    private final GeoCoordinateMapper geoCoordinateMapper;

    private static final String KEY_DRIVERS = "drivers";

    /**
     * Store driver position in map unit coordinates (x,y)
     */
    public void updateDriverLocation(Long driverId, double x, double y) {
        Point redisPoint = geoCoordinateMapper.toRedisPoint(x, y);
        stringRedisTemplate.opsForGeo().add(KEY_DRIVERS, redisPoint, driverId.toString());
    }

    /**
     * Retrieve driver location as raw Redis GEO point (lon,lat)
     */
    private Point getDriverLocationRaw(Long driverId) {
        List<Point> pts = stringRedisTemplate.opsForGeo().position(KEY_DRIVERS, driverId.toString());
        return (pts == null || pts.isEmpty()) ? null : pts.getFirst();
    }

    /**
     * Retrieve driver location in map unit; returns null if absent.
     */
    public double[] getDriverLocation(Long driverId) {
        Point p = getDriverLocationRaw(driverId);
        if (p == null) return null;
        return geoCoordinateMapper.fromRedisPoint(p);
    }

    /**
     * Find nearby drivers.
     * @param x X coordinate
     * @param y Y coordinate
     * @param radiusUnits radius in MAP UNITS (not meters). Must be > 0.
     * @return set of driver usernames within the radius (excluding none)
     * Validation: radiusUnits must be positive and not NaN/Infinite; otherwise empty set is returned.
     */
    public Set<Long> findDriversWithinRadius(double x, double y, double radiusUnits) {
        if (radiusUnits <= 0 || Double.isNaN(radiusUnits) || Double.isInfinite(radiusUnits))
            return Set.of();

        Point center = geoCoordinateMapper.toRedisPoint(x, y);
        double radiusMeters = geoCoordinateMapper.unitsToMeters(radiusUnits);
        Distance distance = new Distance(radiusMeters, Metrics.METERS);

        GeoResults<GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                KEY_DRIVERS,
                GeoReference.fromCoordinate(center),
                distance
        );

        if (results == null || results.getContent().isEmpty())
            return Set.of();

        return results.getContent().stream()
                .map(r -> Long.valueOf(r.getContent().getName()))
                .collect(Collectors.toSet());
    }
}
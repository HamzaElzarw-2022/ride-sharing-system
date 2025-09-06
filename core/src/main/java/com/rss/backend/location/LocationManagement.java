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
public class LocationManagement implements LocationService, LocationInternalApi {
    private final StringRedisTemplate stringRedisTemplate;
    private final GeoCoordinateMapper geoCoordinateMapper;

    private static final String DRIVER_LOCATION_KEY = "driver:location";
    private static final String DRIVER_DEGREE_KEY = "driver:degree";

    @Override
    public void updateDriverLocation(Long driverId, double x, double y, double degree) {
        Point redisPoint = geoCoordinateMapper.toRedisPoint(x, y);
        stringRedisTemplate.opsForGeo().add(DRIVER_LOCATION_KEY, redisPoint, driverId.toString());
        stringRedisTemplate.opsForValue().set(DEGREE_KEY(driverId), Double.toString(normalizeDegree(degree)));
    }

    @Override
    public DriverLocation getDriverLocation(Long driverId) {
        Point geoPoint = getDriverLocationRaw(driverId);
        if (geoPoint == null) return null;
        Point mapPoint = geoCoordinateMapper.fromRedisPoint(geoPoint);

        String d = stringRedisTemplate.opsForValue().get(DEGREE_KEY(driverId));
        double degree = d == null ? 0.0 : parseDegree(d);

        return new DriverLocation(mapPoint.getX(), mapPoint.getY(), degree);
    }

    @Override
    public Set<Long> findDriversWithinRadius(double x, double y, double radiusUnits) {
        if (radiusUnits <= 0 || Double.isNaN(radiusUnits) || Double.isInfinite(radiusUnits))
            return Set.of();

        Point center = geoCoordinateMapper.toRedisPoint(x, y);
        double radiusMeters = geoCoordinateMapper.unitsToMeters(radiusUnits);
        Distance distance = new Distance(radiusMeters, Metrics.METERS);

        GeoResults<GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                DRIVER_LOCATION_KEY,
                GeoReference.fromCoordinate(center),
                distance
        );

        if (results == null || results.getContent().isEmpty())
            return Set.of();

        return results.getContent().stream()
                .map(r -> Long.valueOf(r.getContent().getName()))
                .collect(Collectors.toSet());
    }

    private static String DEGREE_KEY(Long driverId) {
        return DRIVER_DEGREE_KEY + ":" + driverId;
    }

    private static double normalizeDegree(double deg) {
        double d = deg % 360.0;
        if (d < 0) d += 360.0;
        return d;
    }

    private static double parseDegree(String s) {
        try {
            return normalizeDegree(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Retrieve driver location as raw Redis GEO point (lon,lat)
     */
    private Point getDriverLocationRaw(Long driverId) {
        List<Point> pts = stringRedisTemplate.opsForGeo().position(DRIVER_LOCATION_KEY, driverId.toString());
        return (pts == null || pts.isEmpty()) ? null : pts.getFirst();
    }
}
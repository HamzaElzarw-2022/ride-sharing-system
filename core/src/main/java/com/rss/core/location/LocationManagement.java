package com.rss.core.location;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    @Override
    public Set<Long> findNearbyDrivers(double x, double y) {
        double startRadiusUnits = 50;
        double maxRadiusUnits = 5000;
        double growthFactor = 2.0;

        if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y))
            return Set.of();

        double radius = startRadiusUnits;
        while (radius > 0 && radius <= maxRadiusUnits) {
            Set<Long> found = findDriversWithinRadius(x, y, radius);
            if (!found.isEmpty()) {
                return found;
            }
            // Increase radius
            double next = radius * growthFactor;
            if (next == radius) { // guard against no change due to precision
                next += 1.0;
            }
            radius = next;
        }
        return Set.of();
    }

    @Override
    public Map<Long, Point> getAllDriverLocations() {
        // Get all driver IDs
        Set<String> memberIds = stringRedisTemplate.opsForZSet()
                .range(DRIVER_LOCATION_KEY, 0, -1);

        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        // Get all positions in one call
        List<Point> positions = stringRedisTemplate.opsForGeo()
                .position(DRIVER_LOCATION_KEY, memberIds.toArray(String[]::new));

        if (positions == null || positions.isEmpty()) {
            return Map.of();
        }

        // Zip IDs + positions
        Iterator<String> idIterator = memberIds.iterator();
        Iterator<Point> posIterator = positions.iterator();

        Map<Long, Point> result = new HashMap<>(memberIds.size());

        while (idIterator.hasNext() && posIterator.hasNext()) {
            String id = idIterator.next();
            Point geoPoint = posIterator.next();
            if (geoPoint == null) continue;

            try {
                result.put(Long.parseLong(id), geoCoordinateMapper.fromRedisPoint(geoPoint));
            } catch (NumberFormatException ignored) {
                // skip invalid IDs
            }
        }

        return result;
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
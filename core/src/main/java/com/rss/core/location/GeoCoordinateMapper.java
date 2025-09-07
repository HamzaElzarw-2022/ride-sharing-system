package com.rss.core.location;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

/**
 * Maps normalized internal map coordinates (x,y) in range [0..maxX],[0..maxY]
 * to a small WGS84 sub-rectangle suitable for Redis GEO (lon -180..180, lat -85..85)
 * using an affine, invertible transformation centered at configurable center.
 */
@Component
public class GeoCoordinateMapper {

    // Max internal extents (inclusive upper bounds from map normalization)
    @Value("${map.longitude}")
    private int maxX;

    @Value("${map.latitude}")
    private int maxY;

    // Center of the small rectangle we project onto (degrees)
    @Value("${map.geo.center.lon:0.0}")
    private double centerLon;

    @Value("${map.geo.center.lat:0.0}")
    private double centerLat;

    // Conceptual meters represented by one internal map unit
    @Value("${map.geo.meters-per-unit:5.0}")
    private double metersPerUnit;

    // Approximate degree per meter factors
    private static final double DEG_PER_METER_LON = 1.0 / 111320.0;  // varies with latitude; acceptable approximation for small window
    private static final double DEG_PER_METER_LAT = 1.0 / 110574.0;  // meridional

    /**
     * Convert internal map coordinates to a Redis GEO point (lon, lat)
     */
    public Point toRedisPoint(double x, double y) {
        double normX = clamp(x, 0, maxX);
        double normY = clamp(y, 0, maxY);
        double dx = normX - (maxX / 2.0);
        double dy = normY - (maxY / 2.0);
        double lon = centerLon + dx * metersPerUnit * DEG_PER_METER_LON;
        double lat = centerLat + dy * metersPerUnit * DEG_PER_METER_LAT;
        lon = clamp(lon, -180, 180);
        lat = clamp(lat, -85, 85);
        System.out.println("Mapped (" + x + "," + y + ") to Redis Point(" + lon + "," + lat + ")");
        return new Point(lon, lat);
    }

    /**
     * Inverse: convert Redis GEO point back to internal map coordinates (x,y)
     */
    public Point fromRedisPoint(Point p) {
        double dxMeters = (p.getX() - centerLon) / DEG_PER_METER_LON; // meters in longitudinal direction
        double dyMeters = (p.getY() - centerLat) / DEG_PER_METER_LAT; // meters in latitudinal direction
        double x = (dxMeters / metersPerUnit) + (maxX / 2.0);
        double y = (dyMeters / metersPerUnit) + (maxY / 2.0);
        x = clamp(x, 0, maxX);
        y = clamp(y, 0, maxY);
        return new Point(x, y);
    }

    /**
     * Convert a search radius in internal units to meters for Redis GEO radius queries.
     */
    public double unitsToMeters(double units) {
        return units * metersPerUnit;
    }

    private static double clamp(double v, double min, double max) {
        return v < min ? min : (Math.min(v, max));
    }
}


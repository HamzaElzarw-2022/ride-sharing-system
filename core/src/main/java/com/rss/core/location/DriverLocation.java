package com.rss.core.location;

import lombok.Getter;
import org.springframework.data.geo.Point;

/**
 * Location with orientation (degree) in internal map units.
 */
public class DriverLocation extends Point {
    @Getter
    private final double degree; // orientation in degrees [0,360)

    public DriverLocation(double x, double y, double degree) {
        super(x, y);
        this.degree = degree;
    }
}

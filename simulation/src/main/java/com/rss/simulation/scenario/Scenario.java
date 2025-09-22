package com.rss.simulation.scenario;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sim")
public class Scenario {
    private int driverCount = 10;
    private int riderCount = 7;
    private long durationSeconds = 30; // demo length
    private double timeFactor = 1.0; // 1 = real-time, 10 = 10x faster sleeps
    private long seed = 42L;

    private MapProperties map = new MapProperties();
    private TripProperties trip = new TripProperties();

    public int getDriverCount() { return driverCount; }
    public void setDriverCount(int driverCount) { this.driverCount = driverCount; }

    public int getRiderCount() { return riderCount; }
    public void setRiderCount(int riderCount) { this.riderCount = riderCount; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public double getTimeFactor() { return timeFactor; }
    public void setTimeFactor(double timeFactor) { this.timeFactor = timeFactor; }

    public long getSeed() { return seed; }
    public void setSeed(long seed) { this.seed = seed; }

    public MapProperties getMap() { return map; }
    public void setMap(MapProperties map) { this.map = map; }

    public TripProperties getTrip() { return trip; }
    public void setTrip(TripProperties trip) { this.trip = trip; }

    public static class MapProperties {
        private int maxX;
        private int maxY;

        public int getMaxX() { return maxX; }
        public void setMaxX(int maxX) { this.maxX = maxX; }

        public int getMaxY() { return maxY; }
        public void setMaxY(int maxY) { this.maxY = maxY; }
    }

    public static class TripProperties {
        private int minDistance;

        public int getMinDistance() { return minDistance; }
        public void setMinDistance(int minDistance) { this.minDistance = minDistance; }
    }
}

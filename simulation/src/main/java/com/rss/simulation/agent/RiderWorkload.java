package com.rss.simulation.agent;

import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.client.dto.Point;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.trip.RiderAvailabilityInbox;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class RiderWorkload implements Runnable {
    private static final double COORD_MIN = 0.0;
    private static final double COORD_MAX = 500.0;
    private static final double MIN_TRIP_DISTANCE = 150.0;

    private static final long MIN_INTERVAL_MS = 1000;   // fastest when many riders are available
    private static final long MAX_INTERVAL_MS = 10000;  // slowest when few/no riders are available

    private final SimClock clock;
    private final CoreApiClient coreApiClient;
    private final Random rng;
    private final Map<Long, Identity> identities;
    private final RiderAvailabilityInbox availabilityInbox;
    private volatile boolean running = true;

    public RiderWorkload(SimClock clock, CoreApiClient coreApiClient, List<Identity> identities, Random rng, RiderAvailabilityInbox availabilityInbox) {
        this.clock = clock;
        this.coreApiClient = coreApiClient;
        this.rng = rng;
        this.identities = identities.stream().collect(Collectors.toMap(Identity::getRiderId, identity -> identity));
        this.availabilityInbox = availabilityInbox;
        this.availabilityInbox.initialize(this.identities.keySet());
        System.out.println("[RiderWorkload] created with " + identities.size() + " identities");
    }

    @Override
    public void run() {
        try {
            clock.sleep(Duration.ofMillis(10000));
            while (running) {
                long intervalMillis = computeDynamicIntervalMillis();

                availabilityInbox.poll().ifPresent(riderId -> {
                    var identity = identities.get(riderId);
                    if (identity != null) {
                        var route = randomStartEndWithMinDistance(MIN_TRIP_DISTANCE);
                        coreApiClient.requestTrip(route[0], route[1], identity.getJwt())
                            .doOnError(err -> {
                                System.err.println("[RiderWorkload] requestTrip error: " + err.getMessage());
                                availabilityInbox.markAvailable(riderId); // re-add rider on failure
                            })
                            .subscribe();
                        System.out.println("[RiderWorkload] rider " + riderId + " requested trip");
                    }
                });

                clock.sleep(Duration.ofMillis(intervalMillis));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long computeDynamicIntervalMillis() {
        int total = identities.size();
        int available = availabilityInbox.size();
        if (total <= 0) return MAX_INTERVAL_MS;
        double ratio = Math.max(0.0, Math.min(1.0, available / (double) total));
        double interval = MAX_INTERVAL_MS - ratio * (MAX_INTERVAL_MS - MIN_INTERVAL_MS);
        return Math.max(MIN_INTERVAL_MS, Math.min(MAX_INTERVAL_MS, Math.round(interval)));
    }

    private Point[] randomStartEndWithMinDistance(double minDist) {
        Point start = randomPoint(COORD_MIN, COORD_MAX);
        Point end = randomPoint(COORD_MIN, COORD_MAX);

        int attempts = 0, maxAttempts = 50;
        while (distance(start, end) < minDist && attempts++ < maxAttempts) {
            end = randomPoint(COORD_MIN, COORD_MAX);
        }
        if (distance(start, end) < minDist) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double ex = clamp(start.x() + minDist * Math.cos(angle), COORD_MIN, COORD_MAX);
            double ey = clamp(start.y() + minDist * Math.sin(angle), COORD_MIN, COORD_MAX);
            end = new Point(ex, ey);
        }
        return new Point[] { start, end };
    }

    private Point randomPoint(double min, double max) {
        double x = min + rng.nextDouble() * (max - min);
        double y = min + rng.nextDouble() * (max - min);
        return new Point(x, y);
    }

    private double distance(Point a, Point b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return Math.hypot(dx, dy);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public void stop() { this.running = false; }
}

package com.rss.simulation.agent;

import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.client.dto.Point;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.scenario.Scenario;
import com.rss.simulation.trip.RiderAvailabilityInbox;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class RiderWorkload implements Runnable {

    private final double X_COORD_MAX;
    private final double Y_COORD_MAX;
    private final double MIN_TRIP_DISTANCE;

    private static final long MIN_INTERVAL_MS = 1000;   // fastest when many riders are available
    private static final long MAX_INTERVAL_MS = 5000;  // slowest when few/no riders are available

    private final SimClock clock;
    private final CoreApiClient coreApiClient;
    private final Random rng;
    private final Map<Long, Identity> identities;
    private final RiderAvailabilityInbox availabilityInbox;
    private volatile boolean running = true;

    public RiderWorkload(SimClock clock,
                         CoreApiClient coreApiClient,
                         List<Identity> identities,
                         Random rng,
                         RiderAvailabilityInbox availabilityInbox,
                         Scenario scenario) {
        this.clock = clock;
        this.coreApiClient = coreApiClient;
        this.rng = rng;
        this.identities = identities.stream().collect(Collectors.toMap(Identity::getRiderId, identity -> identity));
        this.availabilityInbox = availabilityInbox;
        this.availabilityInbox.initialize(this.identities.keySet());
        this.X_COORD_MAX = scenario.getMap().getMaxX();
        this.Y_COORD_MAX = scenario.getMap().getMaxY();
        this.MIN_TRIP_DISTANCE = scenario.getTrip().getMinDistance();
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
                                System.err.println("[RiderWorkload] rider=" + riderId + " requestTrip error: " + err.getMessage());
                                availabilityInbox.markAvailable(riderId); // re-add rider on failure
                            })
                            .subscribe();
                        System.out.println("[RiderWorkload] rider=" + riderId + " requested trip");
                    }
                });
                System.out.println("[RiderWorkload] availableRiders=" + availabilityInbox.size() + "; intervalMillis=" + intervalMillis);
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
        Point start = randomPoint();
        Point end = randomPoint();

        int attempts = 0, maxAttempts = 50;
        while (distance(start, end) < minDist && attempts++ < maxAttempts) {
            end = randomPoint();
        }
        if (distance(start, end) < minDist) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double ex = clamp(start.x() + minDist * Math.cos(angle), X_COORD_MAX);
            double ey = clamp(start.y() + minDist * Math.sin(angle), Y_COORD_MAX);
            end = new Point(ex, ey);
        }
        return new Point[] { start, end };
    }

    private Point randomPoint() {
        double x = rng.nextDouble() * X_COORD_MAX;
        double y = rng.nextDouble() * Y_COORD_MAX;
        return new Point(x, y);
    }

    private double distance(Point a, Point b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return Math.hypot(dx, dy);
    }

    private double clamp(double v, double max) {
        return Math.max(0, Math.min(max, v));
    }

    public void stop() { this.running = false; }
}

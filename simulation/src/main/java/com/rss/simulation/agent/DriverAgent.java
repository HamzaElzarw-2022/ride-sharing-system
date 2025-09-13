package com.rss.simulation.agent;

import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.client.dto.Direction;
import com.rss.simulation.client.dto.Point;
import com.rss.simulation.client.dto.TripDto;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.trip.TripRequestInbox;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DriverAgent implements Agent {
    private final int id;
    private final SimClock clock;
    private final Random rng;
    private final Identity identity;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final CoreApiClient coreApiClient;
    private final TripRequestInbox tripRequestInbox;

    private final Integer tickTime = 3000; // milliseconds
    private final double idleSpeedFactor = 0.3; // move slower when idle
    private Integer remainingTime = 0;

    private State state = State.IDLE;
    private TripDto trip;

    private List<Direction> directions;
    private int directionIndex;

    private Point location;
    private double degree;

    public DriverAgent(int id, SimClock clock, Random rng, Identity identity, CoreApiClient coreApiClient, TripRequestInbox tripRequestInbox) {
        this.id = id;
        this.clock = clock;
        this.rng = rng;
        this.identity = identity;
        this.coreApiClient = coreApiClient;
        this.tripRequestInbox = tripRequestInbox;
    }

    @Override
    public void run() {
        location = new Point(rng.nextInt(0, 500), rng.nextInt(0, 500));
        getDirections(new Point(rng.nextInt(0, 500), rng.nextInt(0, 500)));
        try {
            while (running.get()) {
                if(state == State.IDLE)
                    acceptTrip();
                if(directions != null) {
                    remainingTime = move((double) tickTime /1000 + remainingTime);
                    updateLocation();
                }

                System.out.println("[DriverAgent] id=" + id + " driverId=" + identity.getDriverId() + "; state=" + state + "; loc=" + location + "; deg=" + degree + (trip != null ? "; tripId=" + trip.id() : ""));
                clock.sleep(Duration.ofMillis(tickTime));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() { running.set(false); }

    @Override
    public String name() { return identity.getUsername(); }

    private int move(double time) {
        Direction target = directions.get(directionIndex);
        double targetX = target.x();
        double targetY = target.y();

        double dx = targetX - location.x();
        double dy = targetY - location.y();
        double distanceToTarget = Math.hypot(dx, dy);

        degree = Math.toDegrees(Math.atan2(dy, dx));
        double speed = target.speed();
        if (state == State.IDLE) {
            speed = speed * idleSpeedFactor;
        }

        double maxMove = speed * time;
        if (maxMove < distanceToTarget) {
            // We cannot reach the target this tick: move proportionally and consume all time
            double ratio = maxMove / distanceToTarget;
            double newX = location.x() + dx * ratio;
            double newY = location.y() + dy * ratio;
            location = new Point(newX, newY);
            return 0;
        } else {
            // We can reach the target: snap to it and save leftover time
            double timeUsed = distanceToTarget / speed;
            location = new Point(targetX, targetY);
            int leftover = (int) Math.round(time - timeUsed);
            directionIndex++;

            // Check for completion
            if (directionIndex >= directions.size()) {
                if (state == State.ON_PICKUP) {
                    startTrip();
                } else if (state == State.ON_TRIP) {
                    endTrip();
                } else if (state == State.IDLE) {
                    getDirections(new Point(rng.nextInt(0, 500), rng.nextInt(0, 500)));
                }
            }
            return leftover;
        }
    }

    private void acceptTrip() {
        Long driverId = identity.getDriverId();
        if (driverId == null) {
            System.err.println("[DriverAgent] id=" + id + " has no driverId, cannot accept trips");
            return;
        }

        Optional<Long> MaybeTripId = tripRequestInbox.pollNext(driverId);
        while (MaybeTripId.isPresent()) {
            Long tripId = MaybeTripId.get();
            try {
                trip = coreApiClient.acceptTrip(tripId, identity.getJwt()).block();
            } catch (Exception e) {
                trip = null;
            }
            if (trip != null) {
                getDirections(new Point(trip.startLatitude(), trip.startLongitude()));
                state = State.ON_PICKUP;
                System.out.println("[DriverAgent] id=" + id + " accepted tripId=" + tripId);
                break;
            } else {
                System.out.println("[DriverAgent] id=" + id + " failed to accept tripId=" + tripId + " (maybe already taken)");
            }
            MaybeTripId = tripRequestInbox.pollNext(driverId);
        }
    }

    private void startTrip() {
        trip = coreApiClient.startTrip(trip.id(), identity.getJwt()).block();
        if(trip != null) {
            getDirections(new Point(trip.endLatitude(), trip.endLongitude()));
            System.out.println("[DriverAgent] id=" + id + " started tripId=" + trip.id());
            state = State.ON_TRIP;
        } else {
            System.out.println("[DriverAgent] id=" + id + " failed to start trip");
            state = State.IDLE;
        }
    }

    private void endTrip() {
        coreApiClient.endTrip(trip.id(), identity.getJwt()).subscribe();
        state = State.IDLE;
        trip = null;
        System.out.println("[DriverAgent] id=" + id + " ended trip and is now IDLE");
        getDirections(new Point(rng.nextInt(0, 500), rng.nextInt(0, 500)));
    }

    private void getDirections(Point point) {
        directions = null;
        var res = coreApiClient.getRoute(location, point, identity.getJwt());
        res.subscribe(routeRes -> {
            var newDirections = new ArrayList<Direction>();
            if(routeRes.startPointProjection() != null) {
                var start = new Direction(
                        (long) routeRes.startPointProjection().projectionPoint().x(),
                        (long) routeRes.startPointProjection().projectionPoint().y(),
                        60
                );
                newDirections.add(start);
            }

            newDirections.addAll(routeRes.route());
            if(routeRes.destinationPointProjection() != null) {
                var end = new Direction(
                        (long) routeRes.destinationPointProjection().projectionPoint().x(),
                        (long) routeRes.destinationPointProjection().projectionPoint().y(),
                        60
                );
                newDirections.add(end);
            }

            directions = newDirections;
            directionIndex = 0;
        });
    }

    private void updateLocation() {
        coreApiClient.updateLocation(location, degree, identity.getJwt());
    }

    public enum State {
        IDLE,
        ON_PICKUP,
        ON_TRIP
    }
}

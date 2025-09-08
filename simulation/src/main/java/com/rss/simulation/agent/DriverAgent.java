package com.rss.simulation.agent;

import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.client.dto.Direction;
import com.rss.simulation.client.dto.Point;
import com.rss.simulation.client.dto.TripDto;
import com.rss.simulation.clock.SimClock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DriverAgent implements Agent {
    private final int id;
    private final SimClock clock;
    private final Random rng;
    private final DriverIdentity identity;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Integer timeToNextMove = 3000; // milliseconds
    private final double idleSpeedFactor = 0.3; // move slower when idle
    private final CoreApiClient coreApiClient;

    private State state = State.IDLE;
    private TripDto trip;

    private List<Direction> directions;
    private int nextDirectionIndex;

    private Point location;
    private double degree;

    public DriverAgent(int id, SimClock clock, Random rng, DriverIdentity identity, CoreApiClient coreApiClient) {
        this.id = id;
        this.clock = clock;
        this.rng = rng;
        this.identity = identity;
        this.coreApiClient = coreApiClient;
    }

    @Override
    public void run() {
        location = new Point(rng.nextInt(0, 500), rng.nextInt(0, 500));
        try {
            while (running.get()) {
                if(state == State.IDLE) acceptTrip();

                if(directions != null && nextDirectionIndex < directions.size()) move(timeToNextMove/1000);
                else if(state == State.IDLE) getDirections(new Point(rng.nextInt(0, 500), rng.nextInt(0, 500)));

                System.out.println("[DriverAgent] id=" + id + " tick; clock x" + clock.factor());
                clock.sleep(Duration.ofMillis(timeToNextMove));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() { running.set(false); }

    @Override
    public String name() { return identity.getUsername(); }

    private void move(Integer timeElapsed) {
        // simulate movement from location to next direction based on speed
        // if on pickup reached startTrip
        // if destination reached endTrip
        // if idle get route to random point
        // if idle move slower

        updateLocation(); // send location update
    }

    private void acceptTrip() {
        List<Long> tripIds = List.of(1L); // TODO: replace with actual nearby trips

        for(Long tripId : tripIds) {
            trip = coreApiClient.acceptTrip(tripId, identity.getJwt()).block();
            if(trip != null) {
                getDirections(new Point(trip.startLatitude(), trip.startLongitude()));
                state = State.ON_PICKUP;
                break;
            }
        }
    }

    private void startTrip() {
        trip = coreApiClient.startTrip(trip.id(), identity.getJwt()).block();
        if(trip != null) {
            getDirections(new Point(trip.endLatitude(), trip.endLongitude()));
            state = State.ON_TRIP;
        } else {
            System.out.println("[DriverAgent] id=" + id + " failed to start trip");
            state = State.IDLE;
        }
    }

    private void endTrip() {
        coreApiClient.endTrip(trip.id(), identity.getJwt());
        state = State.IDLE;
        directions = null;
        trip = null;
    }

    private void getDirections(Point point) {
        nextDirectionIndex++;
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
                var start = new Direction(
                        (long) routeRes.destinationPointProjection().projectionPoint().x(),
                        (long) routeRes.destinationPointProjection().projectionPoint().y(),
                        60
                );
                newDirections.add(start);
            }

            directions = newDirections;
            nextDirectionIndex = 0;
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

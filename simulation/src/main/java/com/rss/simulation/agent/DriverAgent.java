package com.rss.simulation.agent;

import com.rss.simulation.clock.SimClock;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DriverAgent implements Agent {
    private final int id;
    private final SimClock clock;
    private final Random rng;
    private final DriverIdentity identity;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public DriverAgent(int id, SimClock clock, Random rng, DriverIdentity identity) {
        this.id = id;
        this.clock = clock;
        this.rng = rng;
        this.identity = identity;
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                // Placeholder: publish location and occasionally accept trips
                // For now, just sleep and log
                System.out.println("[DriverAgent] id=" + id + " tick; clock x" + clock.factor());
                clock.sleep(Duration.ofMillis(1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() { running.set(false); }

    @Override
    public String name() { return "driver-" + id; }
}

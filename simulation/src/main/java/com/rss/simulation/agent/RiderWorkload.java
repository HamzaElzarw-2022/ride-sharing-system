package com.rss.simulation.agent;

import com.rss.simulation.clock.SimClock;

import java.time.Duration;
import java.util.Random;

public class RiderWorkload implements Runnable {
    private final SimClock clock;
    private final double rps; // requests per second
    private final Random rng;
    private volatile boolean running = true;

    public RiderWorkload(SimClock clock, double rps, Random rng) {
        this.clock = clock;
        this.rps = rps;
        this.rng = rng;
    }

    @Override
    public void run() {
        try {
            long intervalMillis = rps <= 0 ? Long.MAX_VALUE : (long) (1000.0 / rps);
            while (running) {
                // Placeholder: issue a trip request to backend
                System.out.println("[RiderWorkload] issuing request");
                clock.sleep(Duration.ofMillis(intervalMillis));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() { this.running = false; }
}

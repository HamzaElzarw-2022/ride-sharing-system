package com.rss.simulation.agent;

import com.rss.simulation.clock.SimClock;

import java.time.Duration;
import java.util.Random;

public class RiderWorkload implements Runnable {
    private final SimClock clock;
    private final double riderCount; // requests per second
    private final Random rng;
    private volatile boolean running = true;

    public RiderWorkload(SimClock clock, int riderCount, Random rng) {
        this.clock = clock;
        this.riderCount = riderCount;
        this.rng = rng;
    }

    @Override
    public void run() {
        try {
            long intervalMillis = 1000;
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

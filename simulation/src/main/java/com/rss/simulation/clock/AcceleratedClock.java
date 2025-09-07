package com.rss.simulation.clock;

import java.time.Duration;
import java.time.Instant;

public class AcceleratedClock implements SimClock {
    private final double factor;

    public AcceleratedClock(double factor) {
        if (factor <= 0) throw new IllegalArgumentException("factor must be > 0");
        this.factor = factor;
    }

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public void sleep(Duration d) throws InterruptedException {
        long millis = (long) Math.max(0, Math.floor(d.toMillis() / factor));
        Thread.sleep(millis);
    }

    @Override
    public double factor() {
        return factor;
    }
}

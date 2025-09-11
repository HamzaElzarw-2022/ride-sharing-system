package com.rss.simulation.clock;

import java.time.Duration;
import java.time.Instant;

public class RealTimeClock implements SimClock {

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public void sleep(Duration d) throws InterruptedException {
        Thread.sleep(d.toMillis());
    }

    @Override
    public double factor() {
        return 1.0;
    }
}

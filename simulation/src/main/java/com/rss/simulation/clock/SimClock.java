package com.rss.simulation.clock;

import java.time.Duration;
import java.time.Instant;

public interface SimClock {
    Instant now();
    void sleep(Duration d) throws InterruptedException;
    double factor();
}

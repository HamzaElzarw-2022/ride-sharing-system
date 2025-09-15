package com.rss.simulation.trip;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;

/** Stores currently available rider IDs for RiderWorkload to poll. */
@Component
public class RiderAvailabilityInbox {
    private final Deque<Long> available = new ArrayDeque<>();

    public synchronized void initialize(Collection<Long> riderIds) {
        available.clear();
        available.addAll(riderIds);
    }

    public synchronized void markAvailable(long riderId) {
        available.addLast(riderId);
    }

    public synchronized Optional<Long> poll() {
        return Optional.ofNullable(available.pollFirst());
    }

    public synchronized int size() {
        return available.size();
    }
}

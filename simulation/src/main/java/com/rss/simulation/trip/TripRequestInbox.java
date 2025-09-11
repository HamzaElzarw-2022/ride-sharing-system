package com.rss.simulation.trip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Stores pending trip requests per driver with expiration (TTL). */
@Component
public class TripRequestInbox {

    private static final class Entry {
        final long tripId;
        final Instant expiresAt;
        Entry(long tripId, Instant expiresAt) {
            this.tripId = tripId;
            this.expiresAt = expiresAt;
        }
        boolean expired(Instant now) { return now.isAfter(expiresAt); }
    }

    private final Map<Long, Deque<Entry>> byDriver = new ConcurrentHashMap<>();
    private final Duration ttl;

    public TripRequestInbox(@Value("${simulation.request.ttl-seconds:30}") long ttlSeconds) {
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public void add(long driverId, long tripId, Instant producedAt) {
        Instant base = producedAt != null ? producedAt : Instant.now();
        Instant expires = base.plus(ttl);
        Deque<Entry> q = byDriver.computeIfAbsent(driverId, k -> new ArrayDeque<>());
        synchronized (q) {
            purgeExpired(q);
            q.addLast(new Entry(tripId, expires));
        }
    }

    public Optional<Long> pollNext(long driverId) {
        Deque<Entry> q = byDriver.get(driverId);
        if (q == null) return Optional.empty();
        synchronized (q) {
            purgeExpired(q);
            Entry e = q.peekFirst();
            if (e == null) return Optional.empty();
            q.removeFirst();
            return Optional.of(e.tripId);
        }
    }

    private void purgeExpired(Deque<Entry> q) {
        Instant now = Instant.now();
        while (!q.isEmpty() && q.peekFirst().expired(now)) {
            q.removeFirst();
        }
    }
}

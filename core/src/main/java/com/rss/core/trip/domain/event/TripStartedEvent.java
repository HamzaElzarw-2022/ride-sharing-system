package com.rss.core.trip.domain.event;

import java.time.LocalDateTime;

public record TripStartedEvent(Long id, LocalDateTime startTime) {
}

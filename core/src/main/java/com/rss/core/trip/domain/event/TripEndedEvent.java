package com.rss.core.trip.domain.event;

import java.time.LocalDateTime;

public record TripEndedEvent(Long id, LocalDateTime endTime) {
}

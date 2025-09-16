package com.rss.core.trip.domain.event;

import java.time.LocalDateTime;
import org.springframework.data.geo.Point;

public record TripCreatedEvent(Long id, Long riderId, LocalDateTime requestedAt, Point start, Point end) {
}

package com.rss.simulation.client.dto;


import java.time.LocalDateTime;

public record TripDto(
    Long id,
    String status,
    Long riderId,
    Long driverId,
    Double startX,
    Double startY,
    Double destX,
    Double destY,
    LocalDateTime createdAt,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Double fare
) {}
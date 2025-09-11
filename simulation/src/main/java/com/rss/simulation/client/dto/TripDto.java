package com.rss.simulation.client.dto;


import java.time.LocalDateTime;

public record TripDto(
    Long id,
    String status,
    Long riderId,
    Long driverId,
    Double startLatitude,
    Double startLongitude,
    Double endLatitude,
    Double endLongitude,
    LocalDateTime createdAt,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Double fare
) {}
package com.rss.backend.trip.application.dto;

import com.rss.backend.trip.domain.entity.Trip.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {
    private Long id;
    private TripStatus status;
    private Long riderId;
    private Long driverId;

    // Start point coordinates
    private Double startLatitude;
    private Double startLongitude;

    // End point coordinates
    private Double endLatitude;
    private Double endLongitude;

    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double fare;
}
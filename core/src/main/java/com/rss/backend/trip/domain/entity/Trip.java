package com.rss.backend.trip.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatus status;

    @Column(name = "rider_id", nullable = false)
    private Long riderId;

    @Column(name = "start_point", nullable = false)
    private Point startPoint;

    @Column(name = "end_point", nullable = false)
    private Point endPoint;

    private Long driverId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Double fare;

    public enum TripStatus {
        MATCHING, PICKING_UP, STARTED, COMPLETED, CANCELLED
    }
}

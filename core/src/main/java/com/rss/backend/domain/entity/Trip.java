package com.rss.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.security.Timestamp;

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

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
    @ManyToOne
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    private Timestamp startTime;
    private Timestamp endTime;
    private TripStatus status;

    private Point startPoint;
    private Point endPoint;

    public enum TripStatus {
        PICKING_UP, STARTED, COMPLETED, CANCELLED
    }
}

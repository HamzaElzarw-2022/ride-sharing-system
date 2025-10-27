package com.rss.core.map.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "polygon_points")
public class PolygonPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long x;
    private Long y;

    @ManyToOne
    @JoinColumn(name = "polygon_id", nullable = false)
    private PolygonArea polygon;
}
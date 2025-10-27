package com.rss.core.map.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "polygons")
public class PolygonArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PolygonType type;

    @OneToMany(mappedBy = "polygon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolygonPoint> points;

    public enum PolygonType {
        GRASS, WATER
    }
}


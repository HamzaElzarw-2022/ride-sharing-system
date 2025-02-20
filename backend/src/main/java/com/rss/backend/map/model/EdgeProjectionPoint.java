package com.rss.backend.map.model;

import com.rss.backend.domain.entity.Edge;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.geo.Point;

@Data
@AllArgsConstructor
public class EdgeProjectionPoint {
    private Edge edge;
    private Integer distanceFromStart;
    private Point originalPoint;
    private Point projectionPoint;

}

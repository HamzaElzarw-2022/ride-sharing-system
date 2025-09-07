package com.rss.core.map.model;

import com.rss.core.map.dto.EdgeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.geo.Point;

@Data
@Builder
@AllArgsConstructor
public class EdgeProjectionPoint {
    private EdgeDTO edge;
    private Integer distanceFromStart;
    private Point originalPoint;
    private Point projectionPoint;

}

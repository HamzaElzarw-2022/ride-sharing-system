package com.rss.core.map.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimRouteRequest {
    private Integer currentSpeed;
    private Long nodeDirectedTo;
    private Point destinationPoint;
}

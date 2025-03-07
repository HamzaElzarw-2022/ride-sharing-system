package com.rss.backend.map.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class simRouteRequest {
    private Integer currentSpeed;
    private Long nodeDirectedTo;
    private Point destinationPoint;
}

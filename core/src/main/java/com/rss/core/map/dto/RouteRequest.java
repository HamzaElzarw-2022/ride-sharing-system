package com.rss.core.map.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequest {
    // TODO: implement Route Response dto.
    private Point startPoint;
    private Point destinationPoint;
}

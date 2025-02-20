package com.rss.backend.map.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequest {
    // TODO: implement Route Response dto.
    private Point startPoint;
    private Point destinationPoint;
}

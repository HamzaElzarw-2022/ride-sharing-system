package com.rss.backend.map.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.geo.Point;

@Data
@Builder
@AllArgsConstructor
public class RouteStep {
    private long targetY;
    private long targetX;
    private int speed;
    private String instruction;
}

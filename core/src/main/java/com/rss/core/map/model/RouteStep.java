package com.rss.core.map.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RouteStep {
    private long targetY;
    private long targetX;
    private int speed;
    private String instruction;
}

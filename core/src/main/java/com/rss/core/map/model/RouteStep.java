package com.rss.core.map.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RouteStep {
    private long x;
    private long y;
    private int speed;
    private String instruction;
}

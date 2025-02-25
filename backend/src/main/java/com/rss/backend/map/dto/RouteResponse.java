package com.rss.backend.map.dto;

import com.rss.backend.map.model.EdgeProjectionPoint;
import com.rss.backend.map.model.RouteStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse {
    // TODO: implement Route Response dto.
    private EdgeProjectionPoint startPointProjection;
    private EdgeProjectionPoint destinationPointProjection;
    private List<RouteStep> route;
}

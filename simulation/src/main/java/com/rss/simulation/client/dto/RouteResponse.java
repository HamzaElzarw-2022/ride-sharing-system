package com.rss.simulation.client.dto;

import java.util.List;

public record RouteResponse(
    EdgeProjectionPoint startPointProjection,
    EdgeProjectionPoint destinationPointProjection,
    List<Direction> route
) {}
package com.rss.simulation.client.dto;

public record SimRouteRequest(
    Integer currentSpeed,
    Long nodeDirectedTo,
    Point destinationPoint
) {}
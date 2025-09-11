package com.rss.simulation.client.dto;

public record EdgeProjectionPoint(
        EdgeDTO edge,
        Integer distanceFromStart,
        Point originalPoint,
        Point projectionPoint
) {}
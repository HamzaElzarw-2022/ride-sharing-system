package com.rss.simulation.client.dto;

public record EdgeDTO(
    Long id,
    String name,
    Long startNodeId,
    Long endNodeId,
    Integer speed,
    String direction
) {}
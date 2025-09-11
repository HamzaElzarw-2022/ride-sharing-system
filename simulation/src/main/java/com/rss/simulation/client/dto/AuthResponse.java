package com.rss.simulation.client.dto;
public record AuthResponse(String token, Long userId, Long driverId, Long riderId) {}
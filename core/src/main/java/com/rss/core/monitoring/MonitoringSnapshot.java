package com.rss.core.monitoring;

import com.rss.core.trip.application.dto.TripDto;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Map;

public record MonitoringSnapshot(List<TripDto> trips, Map<Long, Point> drivers) {}

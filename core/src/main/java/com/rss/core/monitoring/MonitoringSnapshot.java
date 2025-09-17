package com.rss.core.monitoring;

import com.rss.core.location.DriverLocation;
import com.rss.core.trip.application.dto.TripDto;

import java.util.List;
import java.util.Map;

public record MonitoringSnapshot(List<TripDto> trips, Map<Long, DriverLocation> drivers) {}

package com.rss.core.trip.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.geo.Point;

@Data
public class CreateTripRequest {
    @NotNull
    private Point start;

    @NotNull
    private Point end;
}
package com.rss.backend.trip.api.controller;

import com.rss.backend.common.annotation.CurrentDriverId;
import com.rss.backend.common.annotation.CurrentRiderId;
import com.rss.backend.trip.application.dto.CreateTripRequest;
import com.rss.backend.trip.application.dto.TripDto;
import com.rss.backend.trip.application.port.in.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripDto> createTrip(
            @CurrentRiderId Long riderId,
            @Valid @RequestBody CreateTripRequest request) {
        TripDto dto = tripService.createTrip(riderId, request.getStart(), request.getEnd());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{tripId}/accept")
    public TripDto acceptTrip(@CurrentDriverId Long driverId, @PathVariable Long tripId) {
        return tripService.acceptTrip(driverId, tripId);
    }

    @PostMapping("/{tripId}/start")
    public TripDto startTrip(@CurrentDriverId Long driverId, @PathVariable Long tripId) {
        return tripService.startTrip(driverId, tripId);
    }

    @PostMapping("/{tripId}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endTrip(@CurrentDriverId Long driverId, @PathVariable Long tripId) {
        tripService.endTrip(driverId, tripId);
    }

    @GetMapping("/{tripId}")
    public TripDto getTrip(@PathVariable Long tripId) {
        return tripService.getTrip(tripId);
    }

    @GetMapping("/history")
    public List<TripDto> getTripHistory(@CurrentRiderId Long riderId) {
        return tripService.getTripHistory(riderId);
    }
}

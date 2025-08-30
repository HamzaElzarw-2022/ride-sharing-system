package com.rss.backend.location;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping(value = "/update")
    public ResponseEntity<String> getRoute(@RequestBody Point coordinates) {
        locationService.updateDriverLocation(coordinates);
        return ResponseEntity.ok("driver location updated.");
    }
}

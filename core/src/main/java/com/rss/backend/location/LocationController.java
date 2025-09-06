package com.rss.backend.location;

import com.rss.backend.common.annotation.CurrentDriverId;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping(value = "/location/update")
    public ResponseEntity<String> updateDriverLocation(
            @RequestParam("x") Double x,
            @RequestParam("y") Double y,
            @RequestParam("degree") Double degree,
            @CurrentDriverId Long id) {
        locationService.updateDriverLocation(id, x, y, degree);
        return ResponseEntity.ok("driver location updated.");
    }

    @GetMapping("/{id}/location")
    public DriverLocation getDriverLocation(@PathVariable Long id) {
        return locationService.getDriverLocation(id);
    }

    @GetMapping("/nearby")
    public Set<Long> findDriversWithinRadius(
            @RequestParam("x") double x,
            @RequestParam("y") double y,
            @RequestParam("radius") double radiusUnits) {
        return locationService.findDriversWithinRadius(x, y, radiusUnits);
    }
}

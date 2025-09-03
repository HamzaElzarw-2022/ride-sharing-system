package com.rss.backend.location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping(value = "/update")
    public ResponseEntity<String> getRoute(
            @RequestParam("x") Double x,
            @RequestParam("y") Double y) {
        locationService.updateDriverLocationInternal(x, y);
        return ResponseEntity.ok("driver location updated.");
    }

    @GetMapping
    public ResponseEntity<?> getDriverLocationInternal(@RequestParam("username") String username) {
        double[] coords = locationService.getDriverLocationInternal(username);
        if (coords == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Driver location not found");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("x", coords[0]);
        body.put("y", coords[1]);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> findDriversWithinRadius(
            @RequestParam("x") double x,
            @RequestParam("y") double y,
            @RequestParam("radius") double radiusUnits) {
        Set<String> drivers = locationService.findDriversWithinRadiusInternal(x, y, radiusUnits);
        Map<String, Object> body = new HashMap<>();
        body.put("centerX", x);
        body.put("centerY", y);
        body.put("radiusUnits", radiusUnits);
        body.put("drivers", drivers);
        return ResponseEntity.ok(body);
    }
}

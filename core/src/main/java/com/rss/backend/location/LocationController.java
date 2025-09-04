package com.rss.backend.location;

import com.rss.backend.common.annotation.CurrentDriverId;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @NotNull @CurrentDriverId Long id) {
        locationService.updateDriverLocation(id, x, y);
        return ResponseEntity.ok("driver location updated.");
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<?> getDriverLocationInternal(@PathVariable Long id) {
        double[] location = locationService.getDriverLocation(id);
        if (location == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Driver location not found");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        body.put("x", location[0]);
        body.put("y", location[1]);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> findDriversWithinRadius(
            @RequestParam("x") double x,
            @RequestParam("y") double y,
            @RequestParam("radius") double radiusUnits) {
        Set<Long> drivers = locationService.findDriversWithinRadius(x, y, radiusUnits);
        Map<String, Object> body = new HashMap<>();
        body.put("centerX", x);
        body.put("centerY", y);
        body.put("radiusUnits", radiusUnits);
        body.put("drivers", drivers);
        return ResponseEntity.ok(body);
    }
}

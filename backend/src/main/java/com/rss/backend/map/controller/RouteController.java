package com.rss.backend.map.controller;

import com.rss.backend.map.dto.RouteRequest;
import com.rss.backend.map.dto.RouteResponse;
import com.rss.backend.map.dto.simRouteRequest;
import com.rss.backend.map.model.EdgeProjectionPoint;
import com.rss.backend.map.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    @PostMapping(value = "/route")
    public ResponseEntity<RouteResponse> getRoute(@RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.getRoute(request));
    }

    @PostMapping(value = "/simRoute")
    public ResponseEntity<RouteResponse> getSimRoute(@RequestBody simRouteRequest request) {
        return ResponseEntity.ok(routeService.getSimRoute(request));
    }

    @PostMapping("/closest")
    public ResponseEntity<EdgeProjectionPoint> getClosestEdge(@RequestBody Point point) {
        return ResponseEntity.ok(routeService.findClosestEdge(point));
    }
}

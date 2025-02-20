package com.rss.backend.map.service;

import com.rss.backend.domain.entity.Edge;
import com.rss.backend.map.dto.RouteRequest;
import com.rss.backend.map.dto.RouteResponse;
import com.rss.backend.map.model.EdgeProjectionPoint;
import com.rss.backend.map.model.Route;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    public EdgeProjectionPoint getProjectionPoint(Point point) {
        // TODO: implement algorithm to find projection of point on closest edge.
        return new EdgeProjectionPoint(new Edge(), 20, new Point(10,10), point);
    }

    public Route searchRoute(EdgeProjectionPoint startPoint, EdgeProjectionPoint DestinationPoint) {
        // TODO: implement algorithm to find shortest route.
        return new Route();
    }

    public RouteResponse getRoute(RouteRequest request) {
        // TODO: handle get Route request.
        return new RouteResponse();
    }
}

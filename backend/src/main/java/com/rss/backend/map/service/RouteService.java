package com.rss.backend.map.service;

import com.rss.backend.domain.entity.Edge;
import com.rss.backend.domain.entity.Node;
import com.rss.backend.map.dto.EdgeDTO;
import com.rss.backend.map.dto.RouteRequest;
import com.rss.backend.map.dto.RouteResponse;
import com.rss.backend.map.model.EdgeProjectionPoint;
import com.rss.backend.map.model.Route;
import com.rss.backend.map.repository.EdgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final EdgeRepository edgeRepository;

    public RouteResponse getRoute(RouteRequest request) {

        EdgeProjectionPoint startProjection= findClosestEdge(request.getStartPoint());
        EdgeProjectionPoint destinationProjection = findClosestEdge(request.getDestinationPoint());
        Route route = searchRoute(startProjection, destinationProjection);

        return RouteResponse.builder()
                .startPointProjection(startProjection)
                .destinationPointProjection(destinationProjection)
                .route(route)
                .build();
    }

    public Route searchRoute(EdgeProjectionPoint startPoint, EdgeProjectionPoint DestinationPoint) {
        // TODO: implement algorithm to find shortest route.
        return new Route();
    }

    public EdgeProjectionPoint findClosestEdge(Point point) {

        List<Edge> candidateEdges;
        int searchRadius = 100;

        do {
            candidateEdges = edgeRepository.findEdgesInBoundingBox(point.getX(), point.getY(), searchRadius);
            searchRadius *= 2;
        } while (candidateEdges.isEmpty() && searchRadius < 2000);

        if(candidateEdges.isEmpty())
            return null; // TODO: throw exception

        System.out.println("NUMBER OF CANDIDATES: " + candidateEdges.size() + " SEARCH RADIUS: " + searchRadius);

        Edge closestEdge = candidateEdges.stream()
                .min(Comparator.comparing(edge ->
                        pointToEdgeDistance(point, edge.getStartNode(), edge.getEndNode())
                ))
                .orElseThrow();

        return getProjectionPoint(point, closestEdge);
    }

    public EdgeProjectionPoint getProjectionPoint(Point point, Edge edge) {
        double x1 = edge.getStartNode().getLatitude(), y1 = edge.getStartNode().getLongitude();
        double x2 = edge.getEndNode().getLatitude(), y2 = edge.getEndNode().getLongitude();

        // Edge vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        // Point to start vector
        double px1 = point.getX() - x1;
        double py1 = point.getY() - y1;
        // Projection scalar t
        double t = (px1 * dx + py1 * dy) / (dx * dx + dy * dy);
        // Clamp t to [0,1] to ensure the point is within the edge
        t = Math.max(0, Math.min(1, t));

        Point projectionPoint = new Point(x1 + t * dx, y1 + t * dy);
        int distanceFromStart = (int) (t * Math.sqrt(dx * dx + dy * dy));

        return EdgeProjectionPoint.builder()
                .projectionPoint(projectionPoint)
                .originalPoint(point)
                .edge(EdgeDTO.toEdgeDTO(edge))
                .distanceFromStart(distanceFromStart)
                .build();
    }

    public double pointToEdgeDistance(Point point, Node start, Node end) {
        double x1 = start.getLatitude(), y1 = start.getLongitude();
        double x2 = end.getLatitude(), y2 = end.getLongitude();

        double numerator = Math.abs((x2 - x1) * (y1 - point.getY()) - (x1 - point.getX()) * (y2 - y1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        return numerator / denominator;
    }
}

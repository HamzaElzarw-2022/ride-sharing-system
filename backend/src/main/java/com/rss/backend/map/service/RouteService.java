package com.rss.backend.map.service;

import com.rss.backend.domain.entity.Edge;
import com.rss.backend.domain.entity.Node;
import com.rss.backend.map.dto.EdgeDTO;
import com.rss.backend.map.dto.RouteRequest;
import com.rss.backend.map.dto.RouteResponse;
import com.rss.backend.map.dto.simRouteRequest;
import com.rss.backend.map.model.EdgeProjectionPoint;
import com.rss.backend.map.model.RouteStep;
import com.rss.backend.map.repository.EdgeRepository;
import com.rss.backend.map.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;

    public RouteResponse getRoute(RouteRequest request) {

        EdgeProjectionPoint startProjection= findClosestEdge(request.getStartPoint());
        EdgeProjectionPoint destinationProjection = findClosestEdge(request.getDestinationPoint());
        List<RouteStep> route = searchRoute(startProjection.getEdge().getSpeed(), startProjection.getEdge().getEndNodeId(), destinationProjection);

        return RouteResponse.builder()
                .startPointProjection(startProjection)
                .destinationPointProjection(destinationProjection)
                .route(route)
                .build();
    }

    public RouteResponse getSimRoute(simRouteRequest request) {

        EdgeProjectionPoint destinationProjection = findClosestEdge(request.getDestinationPoint());
        List<RouteStep> route = searchRoute(request.getCurrentSpeed(), request.getNodeDirectedTo(), destinationProjection);

        return RouteResponse.builder()
                .destinationPointProjection(destinationProjection)
                .route(route)
                .build();
    }

    public List<RouteStep> searchRoute(int currentSpeed, Long nodeId, EdgeProjectionPoint destinationPoint) {
        List<RouteStep> routeSteps = new ArrayList<>();

        // Retrieve start node and target edge, nodes, and coordinate
        Node startNode = nodeRepository.findById(nodeId).orElseThrow();
        Edge targetEdge = edgeRepository.findById(destinationPoint.getEdge().getId()).orElseThrow();
        Set<Long> targetNodeIds = Set.of(targetEdge.getStartNode().getId(), targetEdge.getEndNode().getId());
        Point targetCoordinate = destinationPoint.getProjectionPoint();

        // A* algorithm data structures
        PriorityQueue<NodeWithScore> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, NodeWithParent> cameFrom = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        // Initialize with start node
        gScore.put(startNode.getId(), 0.0);
        openSet.add(new NodeWithScore(startNode, 0.0, calculateHeuristic(startNode, targetCoordinate)));

        Node goalNode = null;

        // Main A* loop
        while (!openSet.isEmpty() && goalNode == null) {
            NodeWithScore current = openSet.poll();

            if (closedSet.contains(current.node.getId())) {
                continue;
            }

            closedSet.add(current.node.getId());

            // Check if we've reached one of the target nodes
            if (targetNodeIds.contains(current.node.getId())) {
                goalNode = current.node;
                break;
            }

            // Process all connected edges
            Set<Edge> allEdges = new HashSet<>();
            allEdges.addAll(current.node.getOutgoingEdges());
            allEdges.addAll(current.node.getIncomingEdges());

            for (Edge edge : allEdges) {
                // Skip if this is a one-way edge we can't traverse
                if (edge.getDirection() == Edge.Direction.ONE_WAY &&
                        edge.getEndNode().getId().equals(current.node.getId())) {
                    continue;
                }

                // Determine the neighboring node
                Node neighbor = edge.getStartNode().getId().equals(current.node.getId()) ?
                        edge.getEndNode() : edge.getStartNode();

                if (closedSet.contains(neighbor.getId())) {
                    continue;
                }

                // Calculate new g-score
                double edgeDistance = calculateDistance(
                        current.node.getLongitude(), current.node.getLatitude(),
                        neighbor.getLongitude(), neighbor.getLatitude()
                );

                double tentativeGScore = gScore.get(current.node.getId()) + edgeDistance;

                if (!gScore.containsKey(neighbor.getId()) || tentativeGScore < gScore.get(neighbor.getId())) {
                    cameFrom.put(neighbor.getId(), new NodeWithParent(current.node, edge));
                    gScore.put(neighbor.getId(), tentativeGScore);

                    double heuristic = calculateHeuristic(neighbor, targetCoordinate);
                    double fScore = tentativeGScore + heuristic;

                    openSet.add(new NodeWithScore(neighbor, tentativeGScore, fScore));
                }
            }
        }

        // Path reconstruction and RouteStep creation
        if (goalNode != null) {
            List<Node> pathNodes = new ArrayList<>();
            List<Edge> pathEdges = new ArrayList<>();

            Node current = goalNode;

            while (cameFrom.containsKey(current.getId())) {
                pathNodes.add(0, current);
                NodeWithParent parent = cameFrom.get(current.getId());
                pathEdges.add(0, parent.edge);
                current = parent.node;
            }

            // Add the start node
            pathNodes.add(0, startNode);

            // Create RouteSteps for each node in the path
            for (int i = 0; i < pathNodes.size(); i++) {
                Node node = pathNodes.get(i);
                int speed;
                String instruction;

                if (i == 0) {
                    speed = currentSpeed;
                    instruction = "Start journey";
                } else {
                    Edge edge = pathEdges.get(i - 1);
                    speed = edge.getSpeed();
                    instruction = "Follow " + edge.getName();
                }

                routeSteps.add(RouteStep.builder()
                        .targetX(node.getLatitude())
                        .targetY(node.getLongitude())
                        .speed(speed)
                        .instruction(instruction)
                        .build());
            }

            // Add final step to target point
            routeSteps.add(RouteStep.builder()
                    .targetX((long) targetCoordinate.getX())
                    .targetY((long) targetCoordinate.getY())
                    .speed(targetEdge.getSpeed())
                    .instruction("Arrive at destination")
                    .build());
        } else {
            // No path found
            routeSteps.add(RouteStep.builder()
                    .targetX(startNode.getLatitude())
                    .targetY(startNode.getLongitude())
                    .speed(currentSpeed)
                    .instruction("No route found")
                    .build());
        }

        return routeSteps;
    }

    private double calculateHeuristic(Node node, Point targetPoint) {
        return calculateDistance(
                node.getLongitude(), node.getLatitude(),
                (double) targetPoint.getY(), (double) targetPoint.getX()
        );
    }

    private double calculateDistance(Long longitude1, Long latitude1, double longitude2, double latitude2) {
        return Math.sqrt(
                Math.pow(longitude2 - longitude1, 2) +
                        Math.pow(latitude2 - latitude1, 2)
        );
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

    // Helper classes for A* algorithm
    private static class NodeWithScore {
        private final Node node;
        private final double gScore;
        private final double fScore;

        public NodeWithScore(Node node, double gScore, double fScore) {
            this.node = node;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }

    private static class NodeWithParent {
        private final Node node;
        private final Edge edge;

        public NodeWithParent(Node node, Edge edge) {
            this.node = node;
            this.edge = edge;
        }
    }
}

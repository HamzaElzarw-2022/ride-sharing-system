package com.rss.core.map.service;

import com.rss.core.map.MapInternalApi;
import com.rss.core.map.entity.Edge;
import com.rss.core.map.entity.Node;
import com.rss.core.map.dto.EdgeDTO;
import com.rss.core.map.dto.RouteRequest;
import com.rss.core.map.dto.RouteResponse;
import com.rss.core.map.dto.SimRouteRequest;
import com.rss.core.map.model.EdgeProjectionPoint;
import com.rss.core.map.model.RouteStep;
import com.rss.core.map.repository.EdgeRepository;
import com.rss.core.map.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteService implements MapInternalApi {
    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;

    public RouteResponse getRoute(RouteRequest request) {

        EdgeProjectionPoint startProjection= findClosestEdge(request.getStartPoint());
        EdgeProjectionPoint destinationProjection = findClosestEdge(request.getDestinationPoint());
        List<RouteStep> route = searchRoute(
                startProjection.getEdge().getSpeed(),
                startProjection.getEdge().getStartNodeId(),
                startProjection.getEdge().getEndNodeId(),
                destinationProjection);

        return RouteResponse.builder()
                .startPointProjection(startProjection)
                .destinationPointProjection(destinationProjection)
                .route(route)
                .build();
    }

    public RouteResponse getSimRoute(SimRouteRequest request) {
        // TODO: remove this method and use getRoute instead
        return null;
    }

    private List<RouteStep> searchRoute(int currentSpeed, Long startNodeId, Long endNodeId, EdgeProjectionPoint destinationPoint) {
        List<RouteStep> routeSteps = new ArrayList<>();

        // Retrieve both possible start nodes and target info
        Node startNodeA = nodeRepository.findById(startNodeId).orElseThrow();
        Node startNodeB = nodeRepository.findById(endNodeId).orElseThrow();
        Edge targetEdge = edgeRepository.findById(destinationPoint.getEdge().getId()).orElseThrow();
        Set<Long> targetNodeIds = Set.of(targetEdge.getStartNode().getId(), targetEdge.getEndNode().getId());
        Point targetCoordinate = destinationPoint.getProjectionPoint();

        // A* algorithm data structures
        PriorityQueue<NodeWithScore> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, NodeWithParent> cameFrom = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        // Initialize with both potential start nodes (multi-source)
        gScore.put(startNodeA.getId(), 0.0);
        gScore.put(startNodeB.getId(), 0.0);
        openSet.add(new NodeWithScore(startNodeA, 0.0, calculateHeuristic(startNodeA, targetCoordinate)));
        if (!startNodeB.getId().equals(startNodeA.getId())) {
            openSet.add(new NodeWithScore(startNodeB, 0.0, calculateHeuristic(startNodeB, targetCoordinate)));
        }

        Node goalNode = null;

        // Main A* loop
        //noinspection ReassignedVariable
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
                        current.node.getX(), current.node.getY(),
                        neighbor.getX(), neighbor.getY()
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
                pathNodes.addFirst(current);
                NodeWithParent parent = cameFrom.get(current.getId());
                pathEdges.addFirst(parent.edge);
                current = parent.node;
            }

            // current is now the starting node selected by A*
            Node chosenStart = current;
            pathNodes.addFirst(chosenStart);

            // If the first two nodes are the two ends of the same initial edge (back-and-forth), trim the first node
            if (pathNodes.size() >= 2) {
                Node n0 = pathNodes.get(0);
                Node n1 = pathNodes.get(1);
                // If there is an edge in pathEdges that connects n0 and n1 and also both n0 and n1 are exactly the provided start edge ends
                boolean areEndsOfStartEdge =
                        (n0.getId().equals(startNodeId) && n1.getId().equals(endNodeId)) ||
                        (n0.getId().equals(endNodeId) && n1.getId().equals(startNodeId));
                if (areEndsOfStartEdge) {
                    // remove n0 and its edge to n1 so we start directly at n1
                    pathNodes.remove(0);
                    if (!pathEdges.isEmpty()) {
                        pathEdges.remove(0);
                    }
                }
            }

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
                        .x(node.getX())
                        .y(node.getY())
                        .speed(speed)
                        .instruction(instruction)
                        .build());
            }

            // Add final step to target point
            routeSteps.add(RouteStep.builder()
                    .x((long) targetCoordinate.getX())
                    .y((long) targetCoordinate.getY())
                    .speed(targetEdge.getSpeed())
                    .instruction("Arrive at destination")
                    .build());
        } else {
            // No path found - return one of the start nodes (A)
            routeSteps.add(RouteStep.builder()
                    .x(startNodeA.getX())
                    .y(startNodeA.getY())
                    .speed(currentSpeed)
                    .instruction("No route found")
                    .build());
        }

        return routeSteps;
    }

    private double calculateHeuristic(Node node, Point targetPoint) {
        return calculateDistance(
                node.getX(), node.getY(),
                targetPoint.getX(), targetPoint.getY()
        );
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(
                Math.pow(y2 - y1, 2) +
                        Math.pow(x2 - x1, 2)
        );
    }

    private double calculateDistance(Point p1, Point p2) {
        return calculateDistance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    @Override
    public EdgeProjectionPoint findClosestEdge(Point point) {

        List<Edge> candidateEdges;
        int searchRadius = 100;

        do {
            candidateEdges = edgeRepository.findEdgesInBoundingBox(point.getX(), point.getY(), searchRadius);
            searchRadius *= 2;
        } while (candidateEdges.isEmpty() && searchRadius < 2000);

        if (candidateEdges.isEmpty()) {
            return null; // TODO: throw exception
        }

//        System.out.println("NUMBER OF CANDIDATES: " + candidateEdges.size() + " SEARCH RADIUS: " + searchRadius/2);

        EdgeProjectionPoint closestProjection = null;
        double minDistance = Double.MAX_VALUE;

        for (Edge edge : candidateEdges) {
            // Calculate projection point for this edge
            EdgeProjectionPoint projection = getProjectionPoint(point, edge);

            // Calculate actual distance from original point to projection point
            double distance = Math.sqrt(
                    Math.pow(projection.getProjectionPoint().getX() - point.getX(), 2) +
                            Math.pow(projection.getProjectionPoint().getY() - point.getY(), 2)
            );

            if (distance < minDistance) {
                minDistance = distance;
                closestProjection = projection;
            }
        }

        return closestProjection;
    }

    @Override
    public boolean isAtExpectedOrProjection(Point actual, Point expected) {
        final double TOLERANCE = 3.0;

        // Check if actual point close to expected point?
        double directDistance = calculateDistance(actual, expected);
        if (directDistance <= TOLERANCE) return true;

        EdgeProjectionPoint projectionPoint = findClosestEdge(expected);
        if (projectionPoint == null) return false;

        // Calculate distance between actual point and projection point
        Point projection = projectionPoint.getProjectionPoint();
        double projectionDistance = calculateDistance(actual, projection);

        return projectionDistance <= TOLERANCE;
    }

    public EdgeProjectionPoint getProjectionPoint(Point point, Edge edge) {
        double x1 = edge.getStartNode().getX(), y1 = edge.getStartNode().getY();
        double x2 = edge.getEndNode().getX(), y2 = edge.getEndNode().getY();

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

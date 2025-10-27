package com.rss.core.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rss.core.map.entity.Edge;
import com.rss.core.map.entity.MapMetadata;
import com.rss.core.map.entity.Node;
import com.rss.core.map.entity.PolygonArea;
import com.rss.core.map.entity.PolygonPoint;
import com.rss.core.map.repository.EdgeRepository;
import com.rss.core.map.repository.MapMetadataRepository;
import com.rss.core.map.repository.NodeRepository;
import com.rss.core.map.repository.PolygonRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final MapMetadataRepository metadataRepository;
    private final PolygonRepository polygonRepository;
    private final ObjectMapper objectMapper;

    @Value("${map.path}")
    private String DEFAULT_MAP_PATH;

    @Value("${map.longitude}")
    private Integer DEFAULT_LONGITUDE;

    @Value("${map.latitude}")
    private Integer DEFAULT_LATITUDE;

    @Value("${map.edge.speed}")
    private Integer DEFAULT_EDGE_SPEED;

    @PostConstruct
    public void loadMapOnStartup() {
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_MAP_PATH);
            MapMetadata metaData = metadataRepository.findByKey("meta").orElse(null);

            if (resource.exists()) {
                log.info("Map file found at: {}", DEFAULT_MAP_PATH);
                JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
                String version = rootNode.get("version").asText();

                if(metaData != null && metaData.getVersion().equals(version)) {
                    log.info("Map up to date (version: {})", version);
                    return;
                } else if(metaData == null) {
                    metaData = new MapMetadata();
                    metaData.setKey("meta");
                }

                metaData.setVersion(version);
                metaData.setLongitude(DEFAULT_LONGITUDE);
                metaData.setLatitude(DEFAULT_LATITUDE);
                metaData.setUpdatedAt(java.time.LocalDateTime.now());
                metadataRepository.save(metaData);

                log.info("Map version changed to {}. Updating map...", version);
                updateMap(rootNode.get("edges"), rootNode.get("nodes"), rootNode.get("grass"), rootNode.get("water"));

            } else {
                log.info("Map file not found at: {}. Using existing map data.", DEFAULT_MAP_PATH);
                if(metaData == null)
                    log.error("No map metadata found. The map might be empty.");
            }
        } catch (IOException e) {
            log.error("Error loading map file: {}", e.getMessage(), e);
        }
    }

    public void updateMap(JsonNode jsonEdges, JsonNode jsonNodes, JsonNode jsonGrass, JsonNode jsonWater) {
        if(jsonNodes == null || !jsonNodes.isArray() || jsonNodes.isEmpty()) {
            log.warn("No nodes provided for map update. Aborting.");
            return;
        }

        edgeRepository.deleteAll();
        nodeRepository.deleteAll();
        polygonRepository.deleteAll();

        // Determine original bounds (x,y can be negative or positive)
        long minX = Long.MAX_VALUE, minY = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE, maxY = Long.MIN_VALUE;
        for (int i = 0; i < jsonNodes.size(); i++) {
            JsonNode n = jsonNodes.get(i);
            long x = n.get("x").asLong();
            long y = n.get("y").asLong();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        long rangeX = maxX - minX;
        long rangeY = maxY - minY;
        if (rangeX == 0) {
            log.warn("All node x values identical ({}). X normalization will collapse to 0.", maxX);
        }
        if (rangeY == 0) {
            log.warn("All node y values identical ({}). Y normalization will collapse to 0.", maxY);
        }

        double scaleX = rangeX == 0 ? 0d : (double) DEFAULT_LONGITUDE / (double) rangeX;
        double scaleY = rangeY == 0 ? 0d : (double) DEFAULT_LATITUDE / (double) rangeY;

        Map<Integer, Node> tempNodes = new LinkedHashMap<>(); // preserve order
        for(int i = 0; i < jsonNodes.size(); i++) {
            var jsonNode = jsonNodes.get(i);
            long rawX = jsonNode.get("x").asLong();
            long rawY = jsonNode.get("y").asLong();

            long normX = Math.round((rawX - minX) * scaleX); // in [0, DEFAULT_LONGITUDE]
            long normY = Math.round((rawY - minY) * scaleY); // in [0, DEFAULT_LATITUDE]
            if (normX < 0) normX = 0; else if (normX > DEFAULT_LONGITUDE) normX = DEFAULT_LONGITUDE;
            if (normY < 0) normY = 0; else if (normY > DEFAULT_LATITUDE) normY = DEFAULT_LATITUDE;

            var node = Node.builder()
                    .name(jsonNode.get("name").asText())
                    .x(normX)
                    .y(normY)
                    .build();
            tempNodes.put(jsonNode.get("id").asInt(), node);
        }

        // Persist nodes first to generate DB IDs
        nodeRepository.saveAll(tempNodes.values());

        List<Edge> edges = new ArrayList<>();
        if(jsonEdges != null && jsonEdges.isArray()) {
            for(int i = 0; i < jsonEdges.size(); i++) {
                var jsonEdge = jsonEdges.get(i);
                var speedNode = jsonEdge.get("speed");
                int speed = (speedNode == null || speedNode.isNull()) ? DEFAULT_EDGE_SPEED : speedNode.asInt();
                var edge = Edge.builder()
                        .name(jsonEdge.get("name").asText())
                        .speed(speed)
                        .direction(Edge.Direction.TWO_WAY)
                        .startNode(tempNodes.get(jsonEdge.get("startId").asInt()))
                        .endNode(tempNodes.get(jsonEdge.get("endId").asInt()))
                        .build();
                edges.add(edge);
            }
        }

        edgeRepository.saveAll(edges);

        List<PolygonArea> polygons = new ArrayList<>();
        if (jsonGrass != null && jsonGrass.isArray()) {
            for (JsonNode polygonNode : jsonGrass) {
                PolygonArea polygon = new PolygonArea();
                polygon.setType(PolygonArea.PolygonType.GRASS);
                List<PolygonPoint> points = new ArrayList<>();
                for (JsonNode pointNode : polygonNode) {
                    long rawX = pointNode.get("x").asLong();
                    long rawY = pointNode.get("y").asLong();
                    long normX = Math.round((rawX - minX) * scaleX);
                    long normY = Math.round((rawY - minY) * scaleY);
                    PolygonPoint point = new PolygonPoint();
                    point.setX(normX);
                    point.setY(normY);
                    point.setPolygon(polygon);
                    points.add(point);
                }
                polygon.setPoints(points);
                polygons.add(polygon);
            }
        }
        if (jsonWater != null && jsonWater.isArray()) {
            for (JsonNode polygonNode : jsonWater) {
                PolygonArea polygon = new PolygonArea();
                polygon.setType(PolygonArea.PolygonType.WATER);
                List<PolygonPoint> points = new ArrayList<>();
                for (JsonNode pointNode : polygonNode) {
                    long rawX = pointNode.get("x").asLong();
                    long rawY = pointNode.get("y").asLong();
                    long normX = Math.round((rawX - minX) * scaleX);
                    long normY = Math.round((rawY - minY) * scaleY);
                    PolygonPoint point = new PolygonPoint();
                    point.setX(normX);
                    point.setY(normY);
                    point.setPolygon(polygon);
                    points.add(point);
                }
                polygon.setPoints(points);
                polygons.add(polygon);
            }
        }
        polygonRepository.saveAll(polygons);

        log.info("Map updated: {} nodes, {} edges, {} polygons. Normalized to (0,0)-( {}, {} ).", tempNodes.size(), edges.size(), polygons.size(), DEFAULT_LONGITUDE, DEFAULT_LATITUDE);
    }

    public Map<String, Object> getCurrentMap() {
        MapMetadata metadata = metadataRepository.findByKey("meta").orElse(null);
        String version = metadata != null ? metadata.getVersion() : "unknown";

        List<Node> persistedNodes = nodeRepository.findAll();
        List<Map<String, Object>> nodes = persistedNodes.stream()
                .sorted(Comparator.comparing(Node::getId))
                .map(n -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", n.getId());
                    m.put("name", n.getName());
                    m.put("x", n.getX());
                    m.put("y", n.getY());
                    return m;
                }).collect(Collectors.toList());

        List<Edge> persistedEdges = edgeRepository.findAll();
        List<Map<String, Object>> edges = persistedEdges.stream()
                .sorted(Comparator.comparing(Edge::getId))
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", e.getId());
                    m.put("name", e.getName());
                    m.put("startId", e.getStartNode().getId());
                    m.put("endId", e.getEndNode().getId());
                    if (e.getSpeed() != null) {
                        m.put("speed", e.getSpeed());
                    }
                    return m;
                }).collect(Collectors.toList());

        List<PolygonArea> persistedPolygons = polygonRepository.findAll();
        List<Map<String, Object>> grassPolygons = new ArrayList<>();
        List<Map<String, Object>> waterPolygons = new ArrayList<>();

        for (PolygonArea polygon : persistedPolygons) {
            List<Map<String, Long>> points = polygon.getPoints().stream()
                    .map(p -> {
                        Map<String, Long> point = new LinkedHashMap<>();
                        point.put("x", p.getX());
                        point.put("y", p.getY());
                        return point;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> polygonMap = new LinkedHashMap<>();
            polygonMap.put("points", points);

            if (polygon.getType() == PolygonArea.PolygonType.GRASS) {
                grassPolygons.add(polygonMap);
            } else if (polygon.getType() == PolygonArea.PolygonType.WATER) {
                waterPolygons.add(polygonMap);
            }
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", version);
        root.put("nodes", nodes);
        root.put("edges", edges);
        root.put("grass", grassPolygons);
        root.put("water", waterPolygons);
        return root;
    }
}

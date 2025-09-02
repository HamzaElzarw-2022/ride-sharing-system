package com.rss.backend.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rss.backend.map.entity.Edge;
import com.rss.backend.map.entity.MapMetadata;
import com.rss.backend.map.entity.Node;
import com.rss.backend.map.repository.EdgeRepository;
import com.rss.backend.map.repository.MapMetadataRepository;
import com.rss.backend.map.repository.NodeRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final MapMetadataRepository metadataRepository;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_MAP_PATH = "map.json";

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
                metaData.setLongitude(rootNode.get("longitude").asInt());
                metaData.setLatitude(rootNode.get("latitude").asInt());
                metaData.setUpdatedAt(java.time.LocalDateTime.now());
                metadataRepository.save(metaData);

                log.info("Map version changed to {}. Updating map...", version);
                updateMap(rootNode.get("edges"), rootNode.get("nodes"));

            } else {
                log.info("Map file not found at: {}. Using existing map data.", DEFAULT_MAP_PATH);
                if(metaData == null)
                    log.error("No map metadata found. The map might be empty.");
            }
        } catch (IOException e) {
            log.error("Error loading map file: {}", e.getMessage(), e);
        }
    }

    public void updateMap(JsonNode jsonEdges, JsonNode jsonNodes) {
        edgeRepository.deleteAll();
        nodeRepository.deleteAll();

        Map<Integer, Node> nodes = new HashMap<>();
        List<Edge> edges = new ArrayList<>();

        for(int i = 0; i < jsonNodes.size(); i++) {
            var jsonNode = jsonNodes.get(i);
            var node = Node.builder()
                    .name(jsonNode.get("name").asText())
                    .latitude(jsonNode.get("x").asLong())
                    .longitude(jsonNode.get("y").asLong())
                    .build();

            nodes.put(jsonNode.get("id").asInt(), node);
        }

        for(int i = 0; i < jsonEdges.size(); i++) {
            var jsonEdge = jsonEdges.get(i);
            var edge = Edge.builder()
                    .name(jsonEdge.get("name").asText())
                    .speed(jsonEdge.get("speed").asInt())
                    .direction(Edge.Direction.TWO_WAY)
                    .startNode(nodes.get(jsonEdge.get("startId").asInt()))
                    .endNode(nodes.get(jsonEdge.get("endId").asInt()))
                    .build();
            edges.add(edge);
        }

        nodeRepository.saveAll(nodes.values());
        edgeRepository.saveAll(edges);
    }
}

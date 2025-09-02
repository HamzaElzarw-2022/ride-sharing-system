package com.rss.backend.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rss.backend.map.entity.Edge;
import com.rss.backend.map.entity.Node;
import com.rss.backend.map.repository.EdgeRepository;
import com.rss.backend.map.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MapService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

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

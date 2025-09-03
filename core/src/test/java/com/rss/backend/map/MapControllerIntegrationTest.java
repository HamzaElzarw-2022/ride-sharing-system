package com.rss.backend.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode originalRoot;
    private Map<String, long[]> expectedCoordsByName = new HashMap<>();
    private Set<String> edgeNamesWithSpeed = new HashSet<>();

    @BeforeAll
    void setUp() throws IOException {
        ClassPathResource resource = new ClassPathResource("map.json");
        originalRoot = objectMapper.readTree(resource.getInputStream());
        JsonNode nodes = originalRoot.get("nodes");

        long minX = Long.MAX_VALUE, minY = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE, maxY = Long.MIN_VALUE;
        for (JsonNode n : nodes) {
            long x = n.get("x").asLong();
            long y = n.get("y").asLong();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }
        long rangeX = maxX - minX;
        long rangeY = maxY - minY;
        double scaleX = rangeX == 0 ? 0d : 500d / (double) rangeX;
        double scaleY = rangeY == 0 ? 0d : 500d / (double) rangeY;

        for (JsonNode n : nodes) {
            long x = n.get("x").asLong();
            long y = n.get("y").asLong();
            long normX = Math.round((x - minX) * scaleX);
            long normY = Math.round((y - minY) * scaleY);
            if (normX < 0) normX = 0; else if (normX > 500) normX = 500;
            if (normY < 0) normY = 0; else if (normY > 500) normY = 500;
            expectedCoordsByName.put(n.get("name").asText(), new long[]{normX, normY});
        }

        for (JsonNode e : originalRoot.get("edges")) {
            if (e.has("speed")) {
                edgeNamesWithSpeed.add(e.get("name").asText());
            }
        }
    }

    @Test
    void getMap_shouldReturnNormalizedStructureUsingDbIds() throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/map", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("version").asText()).isEqualTo(originalRoot.get("version").asText());

        JsonNode nodes = root.get("nodes");
        assertThat(nodes).isNotNull();
        assertThat(nodes.isArray()).isTrue();
        assertThat(nodes.size()).isEqualTo(expectedCoordsByName.size());

        Set<Long> dbIds = new HashSet<>();
        for (JsonNode n : nodes) {
            long id = n.get("id").asLong();
            assertThat(dbIds.add(id)).as("DB id should be unique").isTrue();
            String name = n.get("name").asText();
            assertThat(expectedCoordsByName).containsKey(name);
            long[] expected = expectedCoordsByName.get(name);
            assertThat(n.get("x").asLong()).isEqualTo(expected[0]);
            assertThat(n.get("y").asLong()).isEqualTo(expected[1]);
            assertThat(n.get("x").asLong()).isBetween(0L, 500L);
            assertThat(n.get("y").asLong()).isBetween(0L, 500L);
        }

        JsonNode edges = root.get("edges");
        assertThat(edges).isNotNull();
        assertThat(edges.isArray()).isTrue();
        assertThat(edges.size()).isEqualTo(originalRoot.get("edges").size());

        // Validate edges by name
        Set<String> namesSeen = new HashSet<>();
        for (JsonNode e : edges) {
            String name = e.get("name").asText();
            assertThat(namesSeen.add(name)).as("Edge name should be unique").isTrue();
            assertThat(e.get("startId").asLong()).isNotEqualTo(e.get("endId").asLong());
        }

        // Ensure startId/endId refer to existing node IDs
        Set<Long> nodeIds = new HashSet<>();
        for (JsonNode node : nodes) {
            nodeIds.add(node.get("id").asLong());
        }
        for (JsonNode e : edges) {
            assertThat(nodeIds).contains(e.get("startId").asLong());
            assertThat(nodeIds).contains(e.get("endId").asLong());
        }
    }
}

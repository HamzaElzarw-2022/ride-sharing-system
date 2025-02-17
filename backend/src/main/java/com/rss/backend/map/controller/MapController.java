package com.rss.backend.map.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rss.backend.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity updateMap(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty())
            return ResponseEntity.badRequest().body("File is empty. Please upload a valid JSON file.");
        try {
            // Convert file content to JSON object
            JsonNode jsonNode = objectMapper.readTree(file.getInputStream());

            mapService.updateMap(jsonNode.get("edges"), jsonNode.get("nodes"));

            return ResponseEntity.ok(jsonNode);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid JSON file: " + e.getMessage());
        }
    }
}

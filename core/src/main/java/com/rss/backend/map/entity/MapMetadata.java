package com.rss.backend.map.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "map_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapMetadata {
    @Id
    @Column(name = "metadata_key", nullable = false)
    private String key;

    @Column(name = "version", nullable = false)
    private String version;
    
    @Column(name = "longitude", nullable = false)
    private Integer longitude;

    @Column(name = "latitude", nullable = false)
    private Integer latitude;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
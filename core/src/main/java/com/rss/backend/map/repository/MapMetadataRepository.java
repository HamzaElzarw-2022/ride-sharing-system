package com.rss.backend.map.repository;

import com.rss.backend.map.entity.MapMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MapMetadataRepository extends JpaRepository<MapMetadata, String> {
    Optional<MapMetadata> findByKey(String key);
}
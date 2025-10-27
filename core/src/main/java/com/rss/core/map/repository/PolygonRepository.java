package com.rss.core.map.repository;

import com.rss.core.map.entity.PolygonArea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolygonRepository extends JpaRepository<PolygonArea, Long> {
}



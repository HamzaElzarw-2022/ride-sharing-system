package com.rss.backend.map.repository;

import com.rss.backend.domain.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {
}

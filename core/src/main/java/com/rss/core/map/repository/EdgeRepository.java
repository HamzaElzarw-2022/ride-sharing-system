package com.rss.core.map.repository;

import com.rss.core.map.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EdgeRepository extends JpaRepository<Edge, Long> {

    @Query(value = """
    SELECT e.id, e.start_node_id, e.end_node_id, e.speed, e.name, e.direction,
           n1.latitude AS start_node_latitude, n1.longitude AS start_node_longitude,
           n2.latitude AS end_node_latitude, n2.longitude AS end_node_longitude
    FROM edges e
    JOIN nodes n1 ON e.start_node_id = n1.id
    JOIN nodes n2 ON e.end_node_id = n2.id
    WHERE
        (n1.latitude BETWEEN :lat - :range AND :lat + :range
        AND n1.longitude BETWEEN :lon - :range AND :lon + :range)
        OR (n2.latitude BETWEEN :lat - :range AND :lat + :range
        AND n2.longitude BETWEEN :lon - :range AND :lon + :range)
    """, nativeQuery = true)
    List<Edge> findEdgesInBoundingBox(@Param("lat") double lat,
                                      @Param("lon") double lon,
                                      @Param("range") double range);
}

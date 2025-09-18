package com.rss.core.map.repository;

import com.rss.core.map.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EdgeRepository extends JpaRepository<Edge, Long> {

    @Query(value = """
    SELECT e.id, e.start_node_id, e.end_node_id, e.speed, e.name, e.direction,
           n1.x AS start_node_x, n1.y AS start_node_y,
           n2.x AS end_node_x, n2.y AS end_node_y
    FROM edges e
    JOIN nodes n1 ON e.start_node_id = n1.id
    JOIN nodes n2 ON e.end_node_id = n2.id
    WHERE
        (n1.x BETWEEN :x - :range AND :x + :range
        AND n1.y BETWEEN :y - :range AND :y + :range)
        OR (n2.x BETWEEN :x - :range AND :x + :range
        AND n2.y BETWEEN :y - :range AND :y + :range)
    """, nativeQuery = true)
    List<Edge> findEdgesInBoundingBox(@Param("x") double x,
                                      @Param("y") double y,
                                      @Param("range") double range);
}

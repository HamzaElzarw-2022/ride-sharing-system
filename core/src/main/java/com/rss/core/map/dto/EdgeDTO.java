package com.rss.core.map.dto;

import com.rss.core.map.entity.Edge;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EdgeDTO {
    private Long id;
    private String name;
    private Long startNodeId;
    private Long endNodeId;
    private Integer speed;
    private Edge.Direction direction;

    public static EdgeDTO toEdgeDTO(Edge edge) {
        return new EdgeDTO(
                edge.getId(),
                edge.getName(),
                edge.getStartNode().getId(),
                edge.getEndNode().getId(),
                edge.getSpeed(),
                edge.getDirection()
        );
    }
}

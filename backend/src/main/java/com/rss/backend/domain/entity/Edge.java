package com.rss.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "edges")
public class Edge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "start_node_id", nullable = false)
    private Node startNode;

    @ManyToOne
    @JoinColumn(name = "end_node_id", nullable = false)
    private Node endNode;

    private String name;
    private Integer speed;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    public enum Direction {
        ONE_WAY, TWO_WAY
    }
}

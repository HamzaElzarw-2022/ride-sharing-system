package com.rss.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long longitude;
    private Long latitude;
    private String name;

    @OneToMany(mappedBy = "startNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Edge> OutgoingEdges;

    @OneToMany(mappedBy = "endNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Edge> IncomingEdges;

}

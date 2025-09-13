package com.rss.core.map;

import com.rss.core.map.model.EdgeProjectionPoint;
import org.springframework.data.geo.Point;

public interface MapInternalApi {

    /**
     * Finds the closest point on any edge to the given point.
     *
     * @param point the reference point
     * @return an EdgeProjectionPoint containing the closest point on the edge and related information
     */
    EdgeProjectionPoint findClosestEdge(Point point);

    /**
     * Checks if the actual point is either exactly at the expected point,
     * or at the projection of the expected point onto an edge.
     *
     * @param actual the point to check
     * @param expected the reference point or the point to project
     * @return true if actual is at expected or at its projection on an edge, false otherwise
     */
    boolean isAtExpectedOrProjection(Point actual, Point expected);
}

import api from './api';

export interface Point { x: number; y: number }

export interface EdgeDTO {
  id: number;
  name: string;
  startId: number;
  endId: number;
  speed: number;
}

export interface EdgeProjectionPoint {
  edge: EdgeDTO;
  distanceFromStart: number;
  originalPoint: Point;
  projectionPoint: Point;
}

export interface RouteStep {
  x: number;
  y: number;
  speed: number;
  instruction?: string;
}
export interface RouteResponse {
  startPointProjection: EdgeProjectionPoint;
  destinationPointProjection: EdgeProjectionPoint;
  route: RouteStep[];
}

export interface RouteRequest { startPoint: Point; destinationPoint: Point }

export async function fetchRoute(req: RouteRequest) {
  const { data } = await api.post<RouteResponse>('/map/route', req);
  return data;
}

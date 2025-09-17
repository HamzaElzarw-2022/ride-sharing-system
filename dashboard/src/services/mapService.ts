import api from './api';

export interface MapNode {
  id: number;
  name: string;
  x: number;
  y: number;
}

export interface MapEdge {
  id: number;
  name: string;
  startId: number;
  endId: number;
  speed: number;
}

export interface MapData {
  version: string;
  nodes: MapNode[];
  edges: MapEdge[];
}

export async function fetchMap(): Promise<MapData> {
  const { data } = await api.get<MapData>('/map');
  return data;
}

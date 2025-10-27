import type { MapData } from '../../services/mapService';

// Keep a local Vec2 to avoid circular imports
type Vec2 = { x: number; y: number };

// Local worldToScreen to avoid coupling to BaseMap during module init
function worldToScreen(p: Vec2, zoom: number, offset: Vec2): Vec2 {
  return { x: (p.x - offset.x) * zoom, y: (p.y - offset.y) * zoom };
}

// Visual styles tuned for Google-like dark basemap
// More opaque fills (Google dark maps polygons are not obviously translucent)
// Using 8-digit hex (#RRGGBBAA) with full opacity
const GRASS_FILL = '#1F3A2EFF'; // muted deep green, opaque
const WATER_FILL = '#0F2E44FF'; // muted deep blue, opaque

function drawPolygon(
  ctx: CanvasRenderingContext2D,
  pts: { x: number; y: number }[],
  zoom: number,
  offset: Vec2,
  fillStyle: string
) {
  if (!pts || pts.length < 3) return; // need at least a triangle
  const first = worldToScreen(pts[0], zoom, offset);
  ctx.beginPath();
  ctx.moveTo(first.x, first.y);
  for (let i = 1; i < pts.length; i++) {
    const p = worldToScreen(pts[i], zoom, offset);
    ctx.lineTo(p.x, p.y);
  }
  ctx.closePath();
  ctx.fillStyle = fillStyle;
  ctx.fill();
}

export function drawPolygons(
  ctx: CanvasRenderingContext2D,
  data: MapData,
  zoom: number,
  offset: Vec2
) {
  if (data.grass) {
    for (const poly of data.grass) {
      drawPolygon(ctx, poly.points, zoom, offset, GRASS_FILL);
    }
  }
  if (data.water) {
    for (const poly of data.water) {
      drawPolygon(ctx, poly.points, zoom, offset, WATER_FILL);
    }
  }
}

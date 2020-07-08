package view.drawer;

import model.terrain.Terrain;
import processing.core.PApplet;
import view.camera.Camera;
import view.constants.DrawingConstants;
import view.utils.DrawingUtils;

public class MapDrawer {

    PApplet applet;

    public MapDrawer(PApplet inputApplet) {
        applet = inputApplet;
    }

    /**
     * Draw the terrain line relative to the camera position.
     */
    public void drawTerrainLine(Terrain terrain, Camera camera) {
        int[] gridLimits = DrawingUtils.getVisibleGridBoundary(terrain, camera);
        int minX = gridLimits[0];
        int maxX = gridLimits[1];
        int minY = gridLimits[2];
        int maxY = gridLimits[3];
        if (minX >= terrain.getNumX()) return;
        if (minY >= terrain.getNumY()) return;
        if (maxX < 0) return;
        if (maxY < 0) return;
        applet.noFill();
        applet.strokeWeight(1);
        int[] color = DrawingConstants.COLOR_TERRAIN_LINE;
        applet.stroke(color[0], color[1], color[2], DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA);
        for (int i = minX; i <= maxX; i++) {
            applet.beginShape();
            // Begin line
            double[] beginPos = terrain.getPosFromTileIndex(i, minY);
            double beginZ = terrain.getHeightFromTileIndex(i, minY);
            double[] drawBegin = camera.getDrawingPosition(beginPos[0], beginPos[1], beginZ);
            applet.vertex((float) drawBegin[0], (float) drawBegin[1]);

            for (int j = minY; j < maxY; j++) {

                // NextPoint
                double[] nextPos = terrain.getPosFromTileIndex(i, j + 1);
                double nextZ = terrain.getHeightFromTileIndex(i, j + 1);
                double[] drawNext = camera.getDrawingPosition(nextPos[0], nextPos[1], nextZ);

                // Draw line
                if (terrain.getMinZ() == terrain.getMaxZ()) {
                    applet.stroke(color[0], color[1], color[2], DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA);
                } else {
                    applet.stroke(color[0], color[1], color[2],
                            (float) ((nextZ - terrain.getMinZ()) / (terrain.getMaxZ() - terrain.getMinZ()) *
                                    DrawingConstants.COLOR_TERRAIN_LINE_ALPHA_RANGE +
                                    DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA));
                }
                applet.vertex((float) drawNext[0], (float) drawNext[1]);
            }
            applet.endShape();
        }

        for (int j = minY; j <= maxY; j++) {
            // Begin line
            applet.beginShape();
            double[] beginPos = terrain.getPosFromTileIndex(minX, j);
            double beginZ = terrain.getHeightFromTileIndex(minX, j);
            double[] drawBegin = camera.getDrawingPosition(beginPos[0], beginPos[1], beginZ);
            applet.vertex((float) drawBegin[0], (float) drawBegin[1]);

            for (int i = minX; i < maxX; i++) {

                // End line
                double[] nextPos = terrain.getPosFromTileIndex(i + 1, j);
                double nextZ = terrain.getHeightFromTileIndex(i + 1, j);
                double[] drawNext = camera.getDrawingPosition(nextPos[0], nextPos[1], nextZ);

                // Draw line
                if (terrain.getMinZ() == terrain.getMaxZ()) {
                    applet.stroke(color[0], color[1], color[2], DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA);
                } else {
                    applet.stroke(color[0], color[1], color[2],
                            (float) ((nextZ - terrain.getMinZ()) / (terrain.getMaxZ() - terrain.getMinZ()) *
                                    DrawingConstants.COLOR_TERRAIN_LINE_ALPHA_RANGE +
                                    DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA));
                }
                applet.vertex((float) drawNext[0], (float) drawNext[1]);
            }
            applet.endShape();
        }
        applet.strokeWeight(0);
    }
}

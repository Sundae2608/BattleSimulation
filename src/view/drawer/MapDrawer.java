package view.drawer;

import model.terrain.Terrain;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.BaseCamera;
import view.constants.DrawingConstants;

public class MapDrawer extends BaseDrawer {

    PApplet applet;
    BaseCamera camera;

    public MapDrawer(PApplet inputApplet, BaseCamera inputCamera) {
        applet = inputApplet;
        camera = inputCamera;
    }

    @Override
    public void preprocess() {
        return;
    }

    /**
     * Draw the terrain line relative to the camera position.
     */
    public void drawTerrainLine(Terrain terrain) {
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

    /**
     * Draw map texture.
     */
    public void drawMapTexture(Terrain terrain, PImage mapTexture) {
        int[] gridLimits = DrawingUtils.getVisibleGridBoundary(terrain, camera);
        int minX = gridLimits[0];
        int maxX = gridLimits[1];
        int minY = gridLimits[2];
        int maxY = gridLimits[3];
        if (minX >= terrain.getNumX()) return;
        if (minY >= terrain.getNumY()) return;
        if (maxX < 0) return;
        if (maxY < 0) return;
        applet.strokeWeight(2);
        applet.noFill();
        applet.textureMode(PApplet.NORMAL);
        for (int i = minX; i < maxX - 1; i++) {
            applet.beginShape(PApplet.TRIANGLE_STRIP);
            applet.texture(mapTexture);
            for (int j = minY; j < maxY; j++) {
                // Get drawing position of the two points
                double[] pos1 = terrain.getPosFromTileIndex(i, j);
                double height1 = terrain.getHeightFromTileIndex(i, j);
                double[] draw1 = camera.getDrawingPosition(pos1[0], pos1[1], height1);

                double[] pos2 = terrain.getPosFromTileIndex(i + 1, j);
                double height2 = terrain.getHeightFromTileIndex(i + 1, j);
                double[] draw2 = camera.getDrawingPosition(pos2[0], pos2[1], height2);

                // Get uv position
                float u1 = applet.map(i, 0, terrain.getNumX(), 0, 1);
                float u2 = applet.map(i + 1, 0, terrain.getNumX(), 0, 1);
                float v = applet.map(j, 0, terrain.getNumY(), 0, 1);

                // Draw the two vertex
                applet.vertex((float) draw1[0], (float) draw1[1], u1, v);
                applet.vertex((float) draw2[0], (float) draw2[1], u2, v);
            }
            applet.endShape();
        }
    }
}

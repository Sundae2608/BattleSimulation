package model.utils;

import model.terrain.Terrain;
import processing.core.PImage;
import model.enums.PoliticalFaction;
import view.camera.Camera;

import java.util.ArrayList;
import java.util.HashSet;

public final class DrawingUtils {

    public final static int[] COLOR_UNKNOWN = {128, 128, 128, 255};  // #808080
    public final static int[] COLOR_ROMAN = {191, 73, 68, 255};      // #BF4944
    public final static int[] COLOR_GAUL = {89, 191, 104, 255};      // #59BF68
    public final static int[] COLOR_ATHENS = {61, 108, 140, 255};    // #3D6C8C
    public final static int[] COLOR_SPARTA = {133, 21, 23, 255};     // #851517
    public final static int[] COLOR_THEBES = {232, 176, 67, 255};    // #E4F4FA

    public final static int[] COLOR_DEAD = {0, 0, 0, 76};                 // #000000, 76%
    public final static int[] COLOR_IN_DANGER = {226, 73, 255, 255};      // #E249FF
    public final static int[] COLOR_IN_POSITION = {192, 227, 75, 255};    // #C0E34B
    public final static int[] COLOR_GOAL_POSITION = {255, 226, 18, 255};  // #FFE212

    public final static int[] COLOR_HEALTH = {183, 62, 50, 255};    // #B73E32
    public final static int[] COLOR_MORALE = {209, 170, 47, 255};  // #D1AA2F

    public final static int[] COLOR_TERRAIN_DOT = {0, 0, 0, 128};  // #000000, 12%
    public final static int[] COLOR_TERRAIN_LINE = {0, 0, 0};
    public final static int COLOR_TERRAIN_LINE_MAX_ALPHA = 160;
    public final static int COLOR_TERRAIN_LINE_MIN_ALPHA = 50;
    public final static int COLOR_TERRAIN_LINE_ALPHA_RANGE = COLOR_TERRAIN_LINE_MAX_ALPHA - COLOR_TERRAIN_LINE_MIN_ALPHA;

    // Drawing boundary
    public final static double DRAWING_OUTER_BOUNDARY = 20;

    public static int[] getFactionColor(PoliticalFaction faction) {
        switch (faction) {
            case ROME:
                return COLOR_ROMAN;
            case GAUL:
                return COLOR_GAUL;
            case ATHENS:
                return COLOR_ATHENS;
            case SPARTA:
                return COLOR_SPARTA;
            case THEBES:
                return COLOR_THEBES;
            default:
                return COLOR_UNKNOWN;
        }
    }

    /**
     * Return whether the object needs to be drawn
     */
    public static boolean drawable(double x, double y, double width, double height) {
        if (x < -DRAWING_OUTER_BOUNDARY || x > width + DRAWING_OUTER_BOUNDARY ||
                y <-DRAWING_OUTER_BOUNDARY || y > height + DRAWING_OUTER_BOUNDARY) {
            return false;
        }
        return true;
    }

    /**
     * Get all alpha value from images
     */
    public static int[][] getAlphaArray(PImage image) {
        int[] pixels = image.pixels;
        int[][] alpha = new int[image.height][image.width];
        for (int i = 0; i < image.height; i++) {
            for (int j = 0; j < image.width; j++) {
                int index = i * image.width + j;
                alpha[i][j] = pixels[index] >> 24;
            }
        }
        return alpha;
    }

    /**
     * Return the following four numbers in order:
     * - Lowest visible row.
     * - Highest visible row.
     * - Lowest visible column.
     * - Highest visible column.
     */
    public static int[] getVisibleGridBoundary(Terrain terrain, Camera camera) {
        // The top left, top right, bottom left, bottom right represents the furthest point still visible to the cam
        double[] topLeft = camera.getActualPositionFromScreenPosition(0, 0, terrain.getMinZ());
        double[] topRight = camera.getActualPositionFromScreenPosition(0, camera.getHeight(), terrain.getMinZ());
        double[] botLeft = camera.getActualPositionFromScreenPosition(camera.getWidth(), 0, terrain.getMinZ());
        double[] botRight = camera.getActualPositionFromScreenPosition(camera.getWidth(), camera.getHeight(), terrain.getMinZ());

        // Use BFS to generate the list of all points
        double minX = Math.min(topLeft[0], Math.min(topRight[0], Math.min(botLeft[0], botRight[0])));
        double maxX = Math.max(topLeft[0], Math.max(topRight[0], Math.max(botLeft[0], botRight[0])));
        double minY = Math.min(topLeft[1], Math.min(topRight[1], Math.min(botLeft[1], botRight[1])));
        double maxY = Math.max(topLeft[1], Math.max(topRight[1], Math.max(botLeft[1], botRight[1])));

        // Calculate the 4 points
        int minI = (int) ((minX - terrain.getTopX()) / terrain.getDiv());
        int maxI = (int) ((maxX - terrain.getTopX()) / terrain.getDiv());
        int minJ = (int) ((minY - terrain.getTopY()) / terrain.getDiv());
        int maxJ = (int) ((maxY - terrain.getTopY()) / terrain.getDiv());

        minI = Math.max(minI, 0);
        maxI = Math.min(maxI, terrain.getNumX() - 1);
        minJ = Math.max(minJ, 0);
        maxJ = Math.min(maxJ, terrain.getNumY() - 1);

        return new int[] {minI, maxI, minJ, maxJ};
    }

}

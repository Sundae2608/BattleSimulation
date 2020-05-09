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

    public final static int[] COLOR_TERRAIN_DOT = {0, 0, 0, 128};  // #7E7260, 12%

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
        System.out.println(image.height);
        System.out.println(image.width);
        System.out.println();
        int[][] alpha = new int[image.height][image.width];
        for (int i = 0; i < image.height; i++) {
            for (int j = 0; j < image.width; j++) {
                int index = i * image.width + j;
                alpha[i][j] = pixels[index] >> 24;
            }
        }
        return alpha;
    }

    private static void spread(int i, int j, Terrain terrain, Camera camera, HashSet<Integer> visited, int[] gridLimits) {
        // Return if the position is already visited.
        int key = i * terrain.getNumY() + j;
        if (visited.contains(key)) return;

        // Otherwise, mark the position as visited.
        visited.add(key);
        if (i < gridLimits[0]) gridLimits[0] = i;
        if (i > gridLimits[1]) gridLimits[1] = i;
        if (j < gridLimits[2]) gridLimits[2] = j;
        if (j > gridLimits[3]) gridLimits[3] = j;

        // Stop spreading if the new position is not visible from the camera
        double x = i * terrain.getDiv() + terrain.getTopX();
        double y = j * terrain.getDiv() + terrain.getTopY();
        if (!camera.positionIsVisible(x, y, terrain.getHeightFromTileIndex(i, j))) return;

        // Else, spread the point in 4 directions
        spread(i + 1, j, terrain, camera, visited, gridLimits);
        spread(i - 1, j, terrain, camera, visited, gridLimits);
        spread(i, j - 1, terrain, camera, visited, gridLimits);
        spread(i, j + 1, terrain, camera, visited, gridLimits);
    }

    /**
     * Return the following four numbers in order:
     * - Lowest visible row.
     * - Highest visible row.
     * - Lowest visible column.
     * - Highest visible column.
     */
    public static int[] getVisibleGridBoundary(Terrain terrain, Camera camera) {
        // The anchor point close to the camera is guaranteed to be visible on camera
        int i = (int) ((camera.getX() - terrain.getTopX()) / terrain.getDiv());
        int j = (int) ((camera.getY() - terrain.getTopY()) / terrain.getDiv());

        // Use BFS to generate the list of all points
        HashSet<Integer> integerSet = new HashSet<>();
        int[] gridLimits = {i, i, j , j};
        spread(i, j, terrain, camera, integerSet, gridLimits);
        return gridLimits;
    }

}

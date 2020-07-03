package view.utils;

import model.surface.BaseSurface;
import model.terrain.Terrain;
import processing.core.PImage;
import model.enums.PoliticalFaction;
import view.camera.Camera;
import view.constants.DrawingConstants;

public final class DrawingUtils {

    public static int[] getFactionColor(PoliticalFaction faction) {
        switch (faction) {
            case ROME:
                return DrawingConstants.COLOR_ROMAN;
            case GAUL:
                return DrawingConstants.COLOR_GAUL;
            case ATHENS:
                return DrawingConstants.COLOR_ATHENS;
            case SPARTA:
                return DrawingConstants.COLOR_SPARTA;
            case THEBES:
                return DrawingConstants.COLOR_THEBES;
            default:
                return DrawingConstants.COLOR_UNKNOWN;
        }
    }

    public static int[] getSurfaceColor(BaseSurface surface) {
        switch (surface.getType()) {
            case SHALLOW_RIVER:
                return DrawingConstants.SURFACE_COLOR_SHADOW_RIVER;
            case RIVERSIDE:
                return DrawingConstants.SURFACE_COLOR_RIVERSIDE;
            case FOREST:
                return DrawingConstants.SURFACE_COLOR_FOREST;
            case DESERT:
                return DrawingConstants.SURFACE_COLOR_DESERT;
            case MARSH:
                return DrawingConstants.SURFACE_COLOR_MARSH;
            case BEACH:
                return DrawingConstants.SURFACE_COLOR_BEACH;
            case SNOW:
                return DrawingConstants.SURFACE_COLOR_SNOW;
            default:
                return DrawingConstants.SURFACE_COLOR_DEFAULT;
        }
    }

    /**
     * Return whether the object needs to be drawn
     */
    public static boolean drawable(double x, double y, double width, double height) {
        if (x < -DrawingConstants.DRAWING_OUTER_BOUNDARY || x > width + DrawingConstants.DRAWING_OUTER_BOUNDARY ||
                y <-DrawingConstants.DRAWING_OUTER_BOUNDARY || y > height + DrawingConstants.DRAWING_OUTER_BOUNDARY) {
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
        int minI = (int) Math.floor((minX - terrain.getTopX()) / terrain.getDiv()) - 1;
        int maxI = (int) Math.ceil((maxX - terrain.getTopX()) / terrain.getDiv()) + 2;
        int minJ = (int) Math.floor((minY - terrain.getTopY()) / terrain.getDiv()) - 1;
        int maxJ = (int) Math.ceil((maxY - terrain.getTopY()) / terrain.getDiv()) + 2;

        minI = Math.max(minI, 0);
        maxI = Math.min(maxI, terrain.getNumX() - 1);
        minJ = Math.max(minJ, 0);
        maxJ = Math.min(maxJ, terrain.getNumY() - 1);

        return new int[] {minI, maxI, minJ, maxJ};
    }

}

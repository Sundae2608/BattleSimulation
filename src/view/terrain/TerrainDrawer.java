package view.terrain;

import model.terrain.Terrain;
import model.utils.MathUtils;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.Camera;
import view.utils.DrawingUtils;
import view.utils.ImageProcessingUtils;

import java.nio.charset.MalformedInputException;

public class TerrainDrawer {

    PApplet applet;
    Camera camera;
    Terrain terrain;
    PImage[][] images;
    int size;

    /**
     * Check whether the input to terrain drawer is valid. There are two main conditions:
     * - The number of tiles match the tile of the terrain.
     * - The size of each tile is square.
     * @param terrain
     * @param images
     * @return
     */
    private void checkValidInput(Terrain terrain, PImage[][] images) throws Exception {
        // Check the number of tiles.
        if ((terrain.getNumX() != images.length) || (terrain.getNumY() != images[0].length)) {
            throw new Exception("Number of tiles don't match the terrain");
        }
        // Check the size of each tile.
        for (int i = 0; i < images.length; i++) {
            for (int j = 0; j < images[0].length; j++) {
                if (images[i][j].width != images[i][j].height) {
                    throw new Exception("Tile at [" + i + ", " + j + "] is not a square.");
                }
            }
        }
        // Check that the size of all tiles are equal.
        int standardSize = images[0][0].width;
        for (int i = 0; i < images.length; i++) {
            for (int j = 0; j < images[0].length; j++) {
                if (images[i][j].width != standardSize && images[i][j].height != standardSize) {
                    throw new Exception("All tiles must have equal size to each other.");
                }
            }
        }
        return;
    }

    public TerrainDrawer(Terrain inputTerrain, PImage[][] inputImages, Camera inputCamera, PApplet inputApplet) throws Exception {
        checkValidInput(inputTerrain, inputImages);
        terrain = inputTerrain;
        images = inputImages;
        camera = inputCamera;
        applet = inputApplet;
        size = images[0][0].width;
    }

    /**
     * Draw the terrain based on the camera positions.
     */
    public void drawTerrain() {
        // Get the visible grid boundary.
        int[] gridLimits = DrawingUtils.getVisibleGridBoundary(terrain, camera);
        int minX = gridLimits[0];
        int maxX = gridLimits[1];
        int minY = gridLimits[2];
        int maxY = gridLimits[3];

        // For each these tiles, draw them out.
        double topX = terrain.getTopX();
        double topY = terrain.getTopY();
        double div = terrain.getDiv();
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                if (i >= 0 && i < images.length && j >= 0 && j < images[0].length) {
                    double[] drawingPos1 = camera.getDrawingPosition(
                            topX + i * div, topY + j * div, terrain.getHeightFromTileIndex(i, j));
                    double[] drawingPos2 = camera.getDrawingPosition(
                            topX + i * div, topY + (j + 1) * div, terrain.getHeightFromTileIndex(i, j + 1));
                    double[] drawingPos3 = camera.getDrawingPosition(
                                    topX + (i + 1) * div, topY + j * div, terrain.getHeightFromTileIndex(i + 1, j));
                    double[] drawingPos4 = camera.getDrawingPosition(
                            topX + (i + 1) * div, topY + (j + 1) * div, terrain.getHeightFromTileIndex(i + 1, j + 1));
                    double[][] pts = new double[][] {
                            drawingPos1, drawingPos2, drawingPos3, drawingPos4
                    };
                    if (DrawingUtils.drawable(drawingPos1[0], drawingPos1[1], camera.getWidth(), camera.getHeight()) ||
                        DrawingUtils.drawable(drawingPos2[0], drawingPos2[1], camera.getWidth(), camera.getHeight()) ||
                        DrawingUtils.drawable(drawingPos3[0], drawingPos3[1], camera.getWidth(), camera.getHeight()) ||
                        DrawingUtils.drawable(drawingPos4[0], drawingPos4[1], camera.getWidth(), camera.getHeight())) {
                        PImage drawTile = ImageProcessingUtils.transformImage(images[i][j], pts);
                        double topLeftX = MathUtils.findMin(new double[] {pts[0][0], pts[1][0], pts[2][0], pts[3][0]});
                        double topLeftY = MathUtils.findMin(new double[] {pts[0][1], pts[1][1], pts[2][1], pts[3][1]});
                        applet.image(drawTile, (float) topLeftX, (float) topLeftY);
                    }
                }
            }
        }
    }
}

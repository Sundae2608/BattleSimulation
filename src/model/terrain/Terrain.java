package model.terrain;
import processing.core.PApplet;

public class Terrain {

    public double PERLIN_SCALE = 0.2;

    double topX;
    double topY;
    double div;
    int numX;
    int numY;
    double minZ;
    double maxZ;
    double[][] heightField;
    double[][] dx;
    double[][] dy;

    public Terrain(double inputTopX, double inputTopY, double inputDiv,
                   int inputNumX, int inputNumY, int taper, double minHeight, double maxHeight) {
        topX = inputTopX;
        topY = inputTopY;
        div = inputDiv;
        numX = inputNumX;
        numY = inputNumY;
        heightField = new double[numX][numY];
        dx = new double[numX-1][numY-1];
        dy = new double[numX-1][numY-1];
        minZ = minHeight;
        maxZ = maxHeight;
        for (int i = 0; i < numX; i++) {
            for (int j = 0; j < numY; j++) {
                heightField[i][j] = PerlinNoise.noise(i * PERLIN_SCALE, j * PERLIN_SCALE, 0) *
                        (maxHeight - minHeight) + minHeight;
            }
        }
        for (int i = 0; i < numX - 1; i++) {
            for (int j = 0; j < numY - 1; j++) {
                dx[i][j] = (heightField[i + 1][j] - heightField[i][j]) / div;
                dy[i][j] = (heightField[i][j + 1] - heightField[i][j]) / div;
            }
        }
    }



    public double getTopX() {
        return topX;
    }

    public double getTopY() {
        return topY;
    }

    public double getDiv() {
        return div;
    }

    public int getNumX() {
        return numX;
    }

    public int getNumY() {
        return numY;
    }

    public double getHeightFromTileIndex(int i, int j) {
        return heightField[i][j];
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getHeightFromPos(int x, int y) {
        // TODO: Make it a bit more specific with interpolation.
        //       If the object is not in the exact height of the terrain, then the object should be somewhere in the
        //       middle of the position.
        int i = (int) (x / div);
        int j = (int) (y / div);
        if ((i >= 0) && (i < numX) && (j >= 0) && (j < numY)) {
            return heightField[i][j];
        }
        return 0;
    }

    public double[] getDeltaVelFromPos(double x, double y) {
        int i = (int) (x / div);
        int j = (int) (y / div);
        if ((i >= 0) && (i < numX) && (j >= 0) && (j < numY)) {
            return new double[] {dx[i][j], dy[i][j]};
        }
        return new double[]{0, 0};
    }
}

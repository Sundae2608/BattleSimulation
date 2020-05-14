package model.terrain;
import processing.core.PApplet;

public class Terrain {

    public double PERLIN_SCALE = 0.08;
    public double PERLIN_DETAIL_SCALE = 0.8;
    public double PERLIN_DETAIL_HEIGHT_RATIO = 0.05;

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
                        (maxHeight - minHeight) + minHeight +
                        PERLIN_DETAIL_HEIGHT_RATIO * PerlinNoise.noise(i * PERLIN_DETAIL_SCALE, j * PERLIN_DETAIL_SCALE, 0) *
                                (maxHeight - minHeight)
                ;
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

    public double[] getPosFromTileIndex(int i, int j) {
        return new double[] {topX + i * div, topY + j * div};
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getHeightFromPos(double x, double y) {
        int i = (int) ((x - topX) / div);
        int j = (int) ((y - topY) / div);
        double offsetPortionX = ((x - topX) - i * div) / div;
        double offsetPortionY = ((y - topY) - j * div) / div;
        double weightTL = (1 - offsetPortionX) * (1 - offsetPortionY);
        double weightTR = (1 - offsetPortionX) * offsetPortionY;
        double weightBL = offsetPortionX * (1 - offsetPortionY);
        double weightBR = offsetPortionX * offsetPortionY;
        if ((i >= 0) && (i < numX - 1) && (j >= 0) && (j < numY - 1)) {
            double topLeft = heightField[i][j];
            double topRight = heightField[i][j + 1];
            double botLeft = heightField[i + 1][j];
            double botRight = heightField[i + 1][j + 1];
            return topLeft * weightTL + topRight * weightTR + botLeft * weightBL + botRight * weightBR;
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

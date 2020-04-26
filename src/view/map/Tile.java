package view.map;

import processing.core.PImage;

public class Tile {

    // By convention, this will be the top left corner position of each tile
    private double x;
    private double y;
    private double size;
    private PImage tile;
    private double[][] fourCorners;

    public Tile(double inputX, double inputY, double inputSize, PImage inputTile) {
        x = inputX;
        y = inputY;
        size = inputSize;
        tile = inputTile;
        fourCorners = new double[][]{
            {x, y},
            {x + inputSize, y},
            {x + inputSize, y + inputSize},
            {x, y + inputSize}
        };
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public PImage getTile() {
        return tile;
    }
    public double getSize() {
        return size;
    }
    public double[][] getFourCorners() {
        return fourCorners;
    }
}

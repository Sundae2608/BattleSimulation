package model.map_objects;

public class Tree {

    double x;
    double y;
    double radius;
    double height;

    public Tree(double inputX, double inputY, double inputRadius, double inputHeight) {
        x = inputX;
        y = inputY;
        radius = inputRadius;
        height = inputHeight;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }
}

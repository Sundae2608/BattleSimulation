package model.surface;

public class Tree {

    double x;
    double y;
    double radius;
    public Tree(double inputX, double inputY, double inputRadius) {
        x = inputX;
        y = inputY;
        radius = inputRadius;
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
}

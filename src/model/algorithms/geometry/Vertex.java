package model.algorithms.geometry;

import model.utils.MathUtils;

import java.util.HashMap;

public class Vertex {
    double x;
    double y;

    public Vertex(double inputX, double inputY) {
        x = inputX;
        y = inputY;
    }

    public double[] getPt() {
        return new double[] {x, y};
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o)
    {
        // TODO: Overall all of edge graphing system so that we compare systematically from address to content.
        // Checks specified object is "equal to" current object or not
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vertex vertex = (Vertex) o;

        // If they are not the same memory, check if they are the same node.
        return x == vertex.x && y == vertex.y;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = Double.valueOf(x).hashCode();
        result = 31 * result + Double.valueOf(y).hashCode();
        return result;
    }
}

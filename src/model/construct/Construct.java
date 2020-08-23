package model.construct;

import java.util.ArrayList;

/**
 * A construct represents a static object on the battlefield that will always be stationary and that troops simply
 * cannot move through.
 */
public class Construct {
    String name;
    double[][] boundaryPoints;
    double height;

    /**
     * Construct an object given the boundary points.
     * @param points The points that make the boundary of the construct.
     */
    public Construct(String inputName, double[][] points) {
        if (points[0].length != 2) {
            throw new RuntimeException("points input must be an n-by-2 array");
        }
        name = inputName;
        boundaryPoints = points;
    }

    public Construct(String inputName, ArrayList<double[]> points) {
        double[][] pts = new double[points.size()][2];
        for (int i = 0; i < points.size(); i++) {
            pts[i][0] = points.get(i)[0];
            pts[i][1] = points.get(i)[1];
        }
        name = inputName;
        boundaryPoints = pts;
    }

    public double[][] getBoundaryPoints() {
        return boundaryPoints;
    }

    public double getHeight() {
        return height;
    }
}

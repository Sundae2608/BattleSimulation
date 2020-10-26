package model.construct;

import model.utils.GeometryUtils;

import java.util.ArrayList;

/**
 * A construct represents a static object on the battlefield that will always be stationary and that troops simply
 * cannot move through.
 */
public class Construct {
    double x;
    double y;
    ConstructType type;
    double[][] boundaryPoints;
    double height;

    /**
     * Construct an object given the boundary points.
     * @param points The points that make the boundary of the construct.
     */
    public Construct(ConstructType constructType, double[][] points) {
        if (points[0].length != 2) {
            throw new RuntimeException("points input must be an n-by-2 array");
        }
        type = constructType;
        boundaryPoints = points;
        double[] com = GeometryUtils.getCenterOfMass(boundaryPoints);
        x = com[0];
        y = com[1];
    }

    public Construct(ConstructType constructType, ArrayList<double[]> points) {
        double[][] pts = new double[points.size()][2];
        for (int i = 0; i < points.size(); i++) {
            pts[i][0] = points.get(i)[0];
            pts[i][1] = points.get(i)[1];
        }
        type = constructType;
        boundaryPoints = pts;
        double[] com = GeometryUtils.getCenterOfMass(boundaryPoints);
        x = com[0];
        y = com[1];
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double[][] getBoundaryPoints() {
        return boundaryPoints;
    }

    public double getHeight() {
        return height;
    }
}

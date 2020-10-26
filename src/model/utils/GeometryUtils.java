package model.utils;

public class GeometryUtils {

    /**
     * Calculate the center of mass given a list of points.
     * @param pts An n-by-2 array representing n points.
     * @return a double arra with two number representing the (x, y) position of the center of the houses.
     */
    public static double[] getCenterOfMass(double[][] pts) {
        double x = 0;
        double y = 0;
        for (int i = 0; i < pts.length; i++) {
            x += pts[i][0];
            y += pts[i][1];
        }
        return new double[] {
            x / pts.length, y / pts.length
        };
    }
}

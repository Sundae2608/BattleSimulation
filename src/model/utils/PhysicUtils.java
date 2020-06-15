package model.utils;

import model.constants.UniversalConstants;
import model.singles.BaseSingle;
import model.terrain.Terrain;
import model.units.BaseUnit;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;

public final class PhysicUtils {

    /**
     * Check if two axis-aligned bounding boxes collide
     * @param box1 four corners of box 1
     * @param box2 four corners of box 2
     * @return true if they are collided
     */
    public static boolean axisAlignedBoundingBoxCollide(double[][] box1, double[][] box2) {
        double left1 = Math.min(Math.min(Math.min(box1[0][0], box1[1][0]), box1[2][0]), box1[3][0]);
        double left2 = Math.min(Math.min(Math.min(box2[0][0], box2[1][0]), box2[2][0]), box2[3][0]);

        double top1 = Math.min(Math.min(Math.min(box1[0][1], box1[1][1]), box1[2][1]), box1[3][1]);
        double top2 = Math.min(Math.min(Math.min(box2[0][1], box2[1][1]), box2[2][1]), box2[3][1]);

        double right1 = Math.max(Math.max(Math.max(box1[0][0], box1[1][0]), box1[2][0]), box1[3][0]);
        double right2 = Math.max(Math.max(Math.max(box2[0][0], box2[1][0]), box2[2][0]), box2[3][0]);

        double bot1 = Math.max(Math.max(Math.max(box1[0][1], box1[1][1]), box1[2][1]), box1[3][1]);
        double bot2 = Math.max(Math.max(Math.max(box2[0][1], box2[1][1]), box2[2][1]), box2[3][1]);

        return !(left2 > right1
                || right2 < left1
                || top2 > bot1
                || bot2 < top1);
    }
    /**
     * Check if two rotated bounding boxes collide
     * @param box1 four corners of box 1
     * @param box2 four corners of box 2
     * @return true if they are collided
     */
    public static boolean rotatedBoundingBoxCollide(double[][] box1, double[][] box2) {
        double[][][] boxes = {box1, box2};

        for (int box_i = 0; box_i < 2; box_i++) {
            double[][] box = boxes[box_i];
            for (int i1 = 0; i1 < 4; i1++) {
                int i2 = (i1 + 1) % 4;

                // Grab 2 vertices to create an edge.
                double[] p1 = box[i1];
                double[] p2 = box[i2];

                // Perpendicular line
                double[] normal = {p1[1] - p2[1], p1[0] - p2[0]};

                // for each vertex in the first shape, project it onto the line perpendicular to the edge
                // and keep track of the min and max of these values
                double minA = Double.MAX_VALUE;
                double maxA = Double.MIN_VALUE;
                for (int j = 0; j < 4; j++) {
                    double projected = normal[0] * box1[j][0] + normal[1] * box1[j][1];
                    if (projected < minA) {
                        minA = projected;
                    }
                    if (projected > maxA) {
                        maxA = projected;
                    }
                }

                // for each vertex in the second shape, project it onto the line perpendicular to the edge
                // and keep track of the min and max of these values
                double minB = Double.MAX_VALUE;
                double maxB = Double.MIN_VALUE;
                for (int j = 0; j < 4; j++) {
                    double projected = normal[0] * box2[j][0] + normal[1] * box2[j][1];
                    if (projected < minB) {
                        minB = projected;
                    }
                    if (projected > maxB) {
                        maxB = projected;
                    }
                }

                // if there is no overlap between the projects, the edge we are looking at separates the two
                // polygons, and we know there is no overlap
                if (maxA < minB || maxB < minA) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Give the speed and lifeTime of the projectile, and the speed of the projectile.
     * The current height function is (-x^2 / 4 + 1) * 0.05, ranging from -2 to +2.
     * @param speed
     * @param lifeTime
     * @return An array list of number showing the height of the projectiles overtime.
     */
    public static double[] calculateProjectileArch(double speed, int lifeTime) {
        double distance = speed * lifeTime;
        double maxHeight = distance / 4 * 0.20;
        double[] arr = new double[lifeTime];
        for (int i = 0; i < lifeTime; i++) {
            double x = 4.0 * i / lifeTime - 2;
            double height = (-x * x / 4.0 + 1) * maxHeight;
            arr[i] = height;
        }
        return arr;
    }

    /**
     * Given the speed and distance that the projectile aims to shoot toward, return vx and z.
     * @param speed the speed of the projectile. (v)
     * @param distance the distance the projectile aims towards
     * @return A pair in which the first element is the ground speed vx of the projectile, while the second element
     *  is a double array representing the height of elements over time.
     */
    public static Pair<Double, Double[]> calculateProjectileArchGivenSpeedAndDist(double speed, double distance) {
        double g = UniversalConstants.GRAVITATIONAL_CONSTANT_FPM2;

        // Calculating the angle of shooting
        double max_distance = speed * speed / g; // pixels
        double alpha; // radiant
        if (distance >= max_distance) {
            alpha = MathUtils.PIO4; //radian;
        } else {
            alpha = Math.asin(distance * g / (speed * speed)) / 2; // radian
        }

        // Calculating output vx
        double vx = speed * Math.cos(alpha); // pixels / frames

        // Calculating the time series
        int flightTime = (int) Math.floor(2 * speed * MathUtils.quickSin((float) alpha) / g); // frames; This is the flight time
        Double[] heightArr = new Double[flightTime];
        for (int i = 0; i < flightTime; i++) {
            // Each i marks 1 frame
            double height = speed * MathUtils.quickSin((float) alpha) * i - g * (i * i) / 2; // pixels
            heightArr[i] = height; // pixels
        }
        return new Pair<>(vx, heightArr);
    }

    /**
     * Checks the vision of the unit base on where the units are on the terrain. Currently returning all units by
     * default for the purpose of development. (all units are visible to each other)
     * @param unit The unit whose vision we are checking.
     * @param allUnits The list of all units alive on the battle field.
     * @param terrain The terrain that all the units are operate on.
     * @return An array list containing all visible units.
     */
    public static ArrayList<BaseUnit> checkUnitVision(BaseUnit unit, ArrayList<BaseUnit> allUnits, Terrain terrain) {
        ArrayList<BaseUnit> allVisibleUnits = new ArrayList<BaseUnit>();

        // Getting the coordinate of the current unit
        double averageX = unit.getAverageX();
        double averageY = unit.getAverageY();
        double averageZ = unit.getAverageZ();

        // Looping through the list of units
        for (BaseUnit queryUnit : allUnits) {
            // If it is the same unit, there is no need to check.
            if (queryUnit == unit) {
                continue;
            }

            // Getting the coordinate of the queryUnit
            double queryAverageX = queryUnit.getAverageX();
            double queryAverageY = queryUnit.getAverageY();
            double queryAverageZ = queryUnit.getAverageZ();
            double div = terrain.getDiv();

            // Creating a parameter t in [0, 1] to get the line segment from the current unit to the queryUnit
            // https://math.stackexchange.com/questions/2876828/the-equation-of-the-line-pass-through-2-points-in-3d-space
            double temp = MathUtils.squareDistance(queryAverageX, queryAverageY, averageX, averageY);
            temp = 10 * Math.ceil(MathUtils.quickRoot2((float) temp) / div); // Magic number 10
            if (temp == 0) {
                allVisibleUnits.add(queryUnit);
                continue;
            }

            double[] t = new double[(int) temp];
            t[0] = 0;
            for (int i = 1; i < t.length; i++) {
                t[i] = t[i-1] + 1.0 / t.length;
            }

            // Declaring the terrain height at certain coordinate and the visibility boolean
            double[] terrainArrayZ = new double[t.length];
            boolean[] visibilityArray = new boolean[t.length];

            // Finding the coordinate for the line of sight
            double[] arrayX = new double[t.length];
            double[] arrayY = new double[t.length];
            double[] arrayZ = new double[t.length];

            for (int j = 1; j < t.length; j++) {
                // This is a line equation
                arrayX[j] = (queryAverageX - averageX) * t[j] + averageX;
                arrayY[j] = (queryAverageY - averageY) * t[j] + averageY;
                arrayZ[j] = (queryAverageZ - averageZ) * t[j] + averageZ;
                terrainArrayZ[j] = terrain.getHeightFromPos(arrayX[j], arrayY[j]);
                visibilityArray[j] = arrayZ[j] > terrainArrayZ[j];
            }
            // Finding whether the queryUnit is visible
            boolean visibility = true;
            for (int j = 1; j < t.length; j++) {
                visibility = visibility && visibilityArray[j];
            }
            System.out.println(visibility);
            if (visibility) {
                allVisibleUnits.add(queryUnit);
            }
        }
        return allVisibleUnits;
    }

    /**
     * Checks the vision of the unit base on where the units are on the terrain. Currently returning all units by
     * default for the purpose of development. (all units are visible to each other)
     * @param unit The unit whose vision we are checking.
     * @param allSingles The list of all units alive on the battle field.
     * @param terrain The terrain that all the units are operate on.
     * @return An array list containing all visible units.
     */
    public static ArrayList<BaseSingle> checkSingleVision(BaseSingle unit, ArrayList<BaseSingle> allSingles, Terrain terrain) {
        return allSingles;
    }
}

package model.utils;

import model.constants.GameplayConstants;
import model.constants.UniversalConstants;
import model.construct.Construct;
import model.singles.BaseSingle;
import model.surface.BaseSurface;
import model.surface.Tree;
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

    /**
     * Check whether point (px, py) lies within the polygon constructed by vertices.
     * @return
     */
    public static boolean checkPolygonPointCollision(double[][] vertices, double px, double py) {
        boolean collision = false;

        // Go through each edge
        for (int i = 0; i < vertices.length; i++) {
            double[] vc = vertices[i];
            double[] vn = vertices[(i + 1) % vertices.length];

            // compare position, flip 'collision' variable
            // back and forth
            if (((vc[1] > py && vn[1] < py) || (vc[1] < py && vn[1] > py)) &&
                    (px < (vn[0]-vc[0]) * (py-vc[1]) / (vn[1]-vc[1]) + vc[0])) {
                collision = !collision;
            }
        }
        return collision;
    }

    /**
     * Check whether point (px, py) is inside or outside the circle at (cx, cy) with radius r.
     * @return true if the two collides, false otherwise.
     */
    public static boolean checkPointCircleCollision(double px, double py, double cx, double cy, double r) {
        double squareDistance = MathUtils.squareDistance(px, py, cx, cy);
        if (squareDistance <= r * r) {
            return true;
        }
        return false;
    }

    /**
     * If (px, py) is within the circle at (cx, cy) with radius r, return the point in which t
     */
    public static double[] getCirclePushPoint(double cx, double cy, double r, double px, double py) {
        double angle = MathUtils.atan2(py - cy, px - cx);
        double unitX = MathUtils.quickCos((float) angle);
        double unitY = MathUtils.quickSin((float) angle);
        return new double[] {cx + r * unitX, cy + r * unitY};
    }

    /**
     * Check whether the line (x1, y1), (x2, y2) cuts the line (x3, y3), (x4, y4)
     */
    public static boolean checkLineLineCollision(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {

        // calculate the direction of the lines
        double v = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        double uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / v;
        double uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / v;

        // if uA and uB are between 0-1, lines are colliding
        if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
            return true;
        }
        return false;
    }


    /**
     * Check whether the line constructed by (x1, y1) and (x2, y2) collide with the polygon constructed by
     * polygon boundaries.
     * Based on implementation by Jeffrey Thompson: http://www.jeffreythompson.org/collision-detection/poly-line.php
     */
    public static boolean checkLinePolygonCollision(double x1, double y1, double x2, double y2, double[][] polygonBoundaries) {
        // Go through each line of the polygons.
        for (int i = 0; i < polygonBoundaries.length; i++) {
            // Get the pair of point that make the current line of the polygon.
            double[] pt1 = polygonBoundaries[i];
            double[] pt2 = polygonBoundaries[(i + 1) % polygonBoundaries.length];

            // Check if this line cuts the input line (x1, y1) -> (x2, y2)
            double x3 = pt1[0];
            double y3 = pt1[1];
            double x4 = pt2[0];
            double y4 = pt2[1];

            // do a Line/Line comparison
            // if true, return 'true' immediately and
            // stop testing (faster)
            boolean hit = checkLineLineCollision(x1, y1, x2, y2, x3, y3, x4, y4);
            if (hit) {
                return true;
            }
        }

        // never got a hit
        return false;
    }

    /**
     * Check whether the line constructed by (x1, y1) and (x2, y2) collide with the circle with center (cx, cy) and
     * radius r.
     * @return true if the two collides, false otherwise.
     */
    public static boolean checkLineCircleCollision(double x1, double y1, double x2, double y2, double cx, double cy, double r) {
        // is either end INSIDE the circle?
        // if so, return true immediately
        boolean inside1 = checkPointCircleCollision(x1, y1, cx, cy, r);
        boolean inside2 = checkPointCircleCollision(x2, y2, cx, cy, r);
        if (inside1 || inside2) return true;

        // get length of the line
        double squareDist = MathUtils.squareDistance(x1, y1, x2, y2);

        // get dot product of the line and circle
        double dot = (((cx-x1)*(x2-x1)) + ((cy-y1)*(y2-y1))) / squareDist;

        // find the closest point on the line
        double closestX = x1 + (dot * (x2-x1));
        double closestY = y1 + (dot * (y2-y1));

        // get distance to closest point
        double distance = MathUtils.quickDistance(closestX, closestY, cx, cy);

        // is the circle on the line?
        if (distance <= r) {
            return true;
        }
        return false;
    }

    /**
     * Check collision between a construct and a single. This check is more lenient than
     * checkConstructAndTroopCollision in that it only checks the position of the troop and ignores the space occupied
     * by the troop, which makes this check a faster check.
     * @return True if they collide, false otherwise.
     */
    public static boolean checkConstructAndTroopPositionCollision(Construct construct, BaseSingle single) {
        return checkPolygonPointCollision(construct.getBoundaryPoints(), single.getX(), single.getY());
    }

    /**
     * Check collision between a construct and a single. This check is more lenient than
     * checkConstructAndTroopCollision in that it only checks the position of the troop and ignores the space occupied
     * by the troop, which makes this check a faster check.
     * @return True if they collide, false otherwise.
     */
    public static boolean checkSurfaceAndTroopPositionCollision(BaseSurface surface, BaseSingle single) {
        return checkPolygonPointCollision(surface.getSurfaceBoundary(), single.getX(), single.getY());
    }

    /**
     * Returns the projection of point (px, py) on to the line created by (x1, y1) and (x2, y2).
     * Based on implementation at:
     * http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html
     */
    private static double[] projectPointToLine(double px, double py, double x1, double y1, double x2, double y2) {

        // Throws error if (x1, y1) and (x2, y2) are too close to each other
        if (MathUtils.doubleEqual(x1, x2) && MathUtils.doubleEqual(y1, y2)) {
            throw new RuntimeException("(x1, y1) and (x2, y2) must be two different points");
        }

        // Create a third point that is the mid point of either point.
        // If P is too close to either point, we will move that point to the mid point instead to avoid zeroth division.
        double x3 = (x1 + x2) / 2;
        double y3 = (y1 + y2) / 2;
        if (MathUtils.doubleEqual(x1, px) && MathUtils.doubleEqual(y1, py)) {
            x1 = x3;
            y1 = y3;
        } else if (MathUtils.doubleEqual(x2, px) && MathUtils.doubleEqual(y2, py)) {
            x2 = x3;
            y2 = y3;
        }

        // Dot product of e1 and e2.
        double[] e1 = new double[] {x2 - x1, y2 - y1};
        double[] e2 = new double[] {px - x1, py - y1};
        double dp = MathUtils.dotProduct(e1[0], e1[1], e2[0], e2[1]);

        // Get the length of vectors.
        // We don't use quick norm in this case because it actually does not project to the surface at a perpendicular
        // angle.
        double lenLine1 = MathUtils.norm(e1[0], e1[1]);
        double lenLine2 = MathUtils.norm(e2[0], e2[1]);
        double cos = dp / (lenLine1 * lenLine2);

        // Length of projection length line.
        double projectionLength = cos * lenLine2;
        return new double[] {
                x1 + (projectionLength * e1[0]) / lenLine1,
                y1 + (projectionLength * e1[1]) / lenLine1
        };
    }

    public static double[] getClosestPointToEdge(double px, double py, double[][] boundary) {
        int numPoints = boundary.length;
        double minDist = Double.MAX_VALUE;
        int minEdge = 0;
        for (int i = 0; i < numPoints; i++) {
            // Get the two points of the edge
            double[] pt1 = boundary[i];
            double[] pt2 = boundary[(i + 1) % numPoints];
            double x1 = pt1[0]; double y1 = pt1[1];
            double x2 = pt2[0]; double y2 = pt2[1];
            // Select the edge that has the smallest distance to the single
            double distanceToLine = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) /
                    MathUtils.quickDistance(x1, y1, x2, y2);
            if (distanceToLine < minDist) {
                minDist = distanceToLine;
                minEdge = i;
            }
        }
        // Project the point onto the close edge
        double[] pt1 = boundary[minEdge];
        double[] pt2 = boundary[(minEdge + 1) % numPoints];
        double x1 = pt1[0]; double y1 = pt1[1];
        double x2 = pt2[0]; double y2 = pt2[1];
        double [] minPt = projectPointToLine(px, py, x1, y1, x2, y2);
        return minPt;
    }

    /**
     * Using the construct to push the single outside of its boundary, and modify the velocity vector to make it
     * http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html
     */
    public static void constructPushSingle(Construct construct, BaseSingle single) {
        // Go through each edge of the construct, check for the collision with the single, which is treated as a circle.
        double[][] constructBoundary = construct.getBoundaryPoints();
        int numPoints = constructBoundary.length;
        double minDist = Double.MAX_VALUE;
        int minEdge = 0;
        for (int i = 0; i < numPoints; i++) {
            // Get the two points of the edge
            double[] pt1 = constructBoundary[i];
            double[] pt2 = constructBoundary[(i + 1) % numPoints];
            double x1 = pt1[0]; double y1 = pt1[1];
            double x2 = pt2[0]; double y2 = pt2[1];
            // Select the edge that has the smallest distance to the single
            double distanceToLine = Math.abs((y2 - y1) * single.getX() - (x2 - x1) * single.getY() + x2 * y1 - y2 * x1) /
                    MathUtils.quickDistance(x1, y1, x2, y2);
            if (distanceToLine < minDist) {
                minDist = distanceToLine;
                minEdge = i;
            }
        }
        // Set the new position to the edge of the object, simulating the construct "pushing" the single out.
        // Also change the velocity vector by projecting it to the edge of the object (effectively canceling the force
        // that pushes the single into the object.
        double[] pt1 = constructBoundary[minEdge];
        double[] pt2 = constructBoundary[(minEdge + 1) % numPoints];
        double x1 = pt1[0]; double y1 = pt1[1];
        double x2 = pt2[0]; double y2 = pt2[1];
        double [] minPt = projectPointToLine(single.getX(), single.getY(), x1, y1, x2, y2);
        double [] minVel = MathUtils.vectorProjection(single.getxVel(), single.getyVel(), x1 - x2, y1 - y2);
        single.setX(minPt[0]);
        single.setY(minPt[1]);
        single.setxVel(minVel[0]);
        single.setyVel(minVel[1]);
    }
    /**
     * Using the tree to push the single outside of its boundary.
     */
    public static void treePushSingle(Tree tree, BaseSingle single) {
        // Go through each edge of the construct, check for the collision with the single, which is treated as a circle.
        double[] newPt = getCirclePushPoint(tree.getX(), tree.getY(), tree.getRadius(), single.getX(), single.getY());
        single.setX(newPt[0]);
        single.setY(newPt[1]);
        // Apply a small speed reduction
        single.setxVel(single.getxVel() * GameplayConstants.TREE_COLLISION_SLOWDOWN);
        single.setyVel(single.getyVel() * GameplayConstants.TREE_COLLISION_SLOWDOWN);
    }
}

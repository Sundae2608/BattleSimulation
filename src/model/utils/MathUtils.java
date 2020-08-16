package model.utils;

import it.unimi.dsi.util.XoShiRo256PlusRandom;
import javafx.util.Pair;
import model.singles.BaseSingle;
import model.surface.BaseSurface;

import java.util.*;

public final class MathUtils {

    // RNG
    private final static XoShiRo256PlusRandom random = new XoShiRo256PlusRandom();

    // Hackish number
    static final double sq2p1 = 2.414213562373095048802e0;
    static final double sq2m1 = .414213562373095048802e0;
    static final double p4 = .161536412982230228262e2;
    static final double p3 = .26842548195503973794141e3;
    static final double p2 = .11530293515404850115428136e4;
    static final double p1 = .178040631643319697105464587e4;
    static final double p0 = .89678597403663861959987488e3;
    static final double q4 = .5895697050844462222791e2;
    static final double q3 = .536265374031215315104235e3;
    static final double q2 = .16667838148816337184521798e4;
    static final double q1 = .207933497444540981287275926e4;
    static final double q0 = .89678597403663861962481162e3;

    /**
     * Precalulated PI numbers.
     *
     * PI divided by 2, 4, 180
     * PI times 2, 4
     * 180 divided by PI
     */
    public static final double PIO2 = 1.5707963267948966135e0;
    public static final double PIO4 = 0.7853981633974483096e0;
    public static final double PIX2 = 6.2831853071795864766e0;
    public static final double PIX4 = 12.566370614359172953e0;
    public static final double PIO180 = 0.01745329251994329e0;
    public static final double PIU180 = 57.2957795130823208e0;

    /**
     * Some other useful numbers
     */
    public static final double ROOT3 = Math.sqrt(3);
    public static final double ROOT3O2 = Math.sqrt(3) / 2;

    // Sin look up table
    private static float[] sinLookupTable = new float[65536];

    /**
     * Lookup table initiation
     */
    static {
        for (int i = 0; i < 65536; ++i) {
            sinLookupTable[i] = (float) Math.sin(i * PIX2 / 65536.0D);
        }
    }

    // Mathematical constant
    public final static double EPSILON = 1e-6;
    public final static double MAX_DOUBLE = Double.MAX_VALUE;
    /**
     * Compare if two doubles are equal. They are essentially equal if their diff < 1e-6
     */
    public static boolean doubleEqual(double a, double b) {
        double diff = Math.abs(a - b);
        return diff < MathUtils.EPSILON;
    }

    /**
     * Compare if two doubles are equal. They are essentially equal if their diff < epsilon.
     */
    public static boolean doubleEqual(double a, double b, double epsilon) {
        double diff = Math.abs(a - b);
        return diff < epsilon;
    }

    /**
     * Uniformly random between 0.0 and 1.0
     */
    public static double randUniform() {
        return random.nextDoubleFast();
    }

    /**
     * Uniformly random integer between min and max
     */
    public static int randint(int min, int max) {
        return min + (int)(random.nextDoubleFast() * ((max - min) + 1));
    }

    /**
     * Uniformly random double between min and max
     */
    public static double randDouble(double min, double max) { return min + (random.nextDoubleFast() * (max - min));}

    /**
     * Magnitude of vector (vx, vy)
     */
    public static double magnitude(double vx, double vy) {
        return Math.sqrt(vx * vx + vy * vy);
    }

    /**
     * Magnitude of vector (vx, vy, vz)
     */
    public static double magnitude(double vx, double vy, double vz) {
        return Math.sqrt(vx * vx + vy * vy + vz * vz);
    }

    /**
     * Signed angle difference
     */
    public static double signedAngleDifference(double angle, double angleGoal) {
        double angleDifference = angleGoal - angle;
        if (angleDifference > Math.PI) angleDifference -= Math.PI * 2;
        else if (angleDifference < - Math.PI) angleDifference += Math.PI * 2;

        return angleDifference;
    }

    /**
     * Square the numbers
     */
    public static double square(double x) {
        return x * x;
    }

    /**
     * Square distance
     */
    public static double squareDistance(double x1, double y1, double x2, double y2) {
        return square(x1 - x2) + square(y1 - y2);
    }

    /**
     * Distance between two points, using quick root method
     */
    public static double quickDistance(double x1, double y1, double x2, double y2) {
        return quickRoot2((float) squareDistance(x1, y1, x2, y2));
    }

    /**
     * Distance between two points
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(squareDistance(x1, y1, x2, y2));
    }

    /**
     * Semi-Accurate approximation for a floating-point square root.
     * Roughly 1.4x as fast as java.lang.Math.sqrt(x);
     * Source: https://github.com/Fishrock123/Optimized-Java/blob/master/src/com/fishrock123/math/RootMath.java
     */
    public static float quickRoot1(float f) {
        float y = Float.intBitsToFloat(0x5f375a86 - (Float.floatToIntBits(f) >> 1)); // evil floating point bit level hacking -- Use 0x5f375a86 instead of 0x5f3759df, due to slight accuracy increase. (Credit to Chris Lomont)
        y = y * (1.5F - (0.5F * f * y * y)); 	// Newton step, repeating increases accuracy
        return f * y;
    }

    /**
     * Approximation for a floating-point square root.
     * This method is not very accurate past two digits, but up to 2.4 times as fast as java.lang.Math.sqrt(x);
     * Source: https://github.com/Fishrock123/Optimized-Java/blob/master/src/com/fishrock123/math/RootMath.java
     */
    public static float quickRoot2(float f) {
        return f * Float.intBitsToFloat(0x5f375a86 - (Float.floatToIntBits(f) >> 1)); // evil floating point bit level hacking -- Use 0x5f375a86 instead of 0x5f3759df, due to slight accuracy increase. (Credit to Chris Lomont)
    }

    /**
     * Fast sin lookup
     * Source: https://github.com/Fishrock123/Optimized-Java/blob/master/src/com/fishrock123/math/TrigMath.java
     */
    public static final float quickSin(float radians) {
        return sinLookupTable[(int) (radians * 10430.378F) & '\uffff']; //Floating-point bit hack.
        //(rad * degrees to index) [bit conjuction] sine-mask
    }

    /**
     * Fast cosine lookup
     * Source: https://github.com/Fishrock123/Optimized-Java/blob/master/src/com/fishrock123/math/TrigMath.java
     */
    public static final float quickCos(float radians) {
        return sinLookupTable[(int) (radians * 10430.378F + 16384.0F) & '\uffff']; //Even more Floating-point bit hack.
    }

    private static double mxatan(double d) {
        final double asq = d * d;
        double value = ((((p4 * asq + p3) * asq + p2) * asq + p1) * asq + p0);
        value = value / (((((asq + q4) * asq + q3) * asq + q2) * asq + q1) * asq + q0);
        return value * d;
    }

    private static double msatan(double a) {
        return a < sq2m1 ? mxatan(a)
                : a > sq2p1 ? PIO2 - mxatan(1 / a)
                : PIO4 + mxatan((a - 1) / (a + 1));
    }

    public static double atan(double a) {
        return a > 0 ? msatan(a) : -msatan(-a);
    }

    /**
     * Fast arctangent2 calculation
     * Source: https://github.com/Fishrock123/Optimized-Java/blob/master/src/com/fishrock123/math/TrigMath.java
     */
    public static double atan2(double y, double x) {
        if (y + x == y)
            return y >= 0 ? PIO2 : -PIO2;
        y = atan(y / x);
        return x < 0 ? y <= 0 ? y + Math.PI : y - Math.PI : y;
    }

    /**
     * Fast conversion from degrees to radians
     */
    public static final double toRadians(double degrees) {
        return degrees * PIO180;
    }

    /**
     * Fast arcsin
     */
    public static final double quickAsin(double x) {
        return 0;
    }

    /**
     * Pair two integers in a long using bit manipulation. This is better than using Pair objectdd
     */
    public static long pairInt(int first, int second) {
        return ((long)first << 32) | (second & 0XFFFFFFFFL);
    }

    /**
     * Get first int in long
     */
    public static int getFirst(long pairInt) {
        return (int) (pairInt >> 32);
    }

    /**
     * Get second int in long
     */
    public static int getSecond(long pairInt) {
        return (int) pairInt;
    }

    /**
     * Rotate vector (x, y) to a certain angle
     */
    public static Pair<Double, Double> rotate(double x, double y, double angle) {
        double newX = x * quickCos((float) angle) - y * quickSin((float) angle);
        double newY = x * quickSin((float) angle) + y * quickCos((float) angle);
        return new Pair<>(newX, newY);
    }

    /**
      Cap the length of the
     */
    public static double capMinMax(double x, double min, double max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }

    /**
     * Ratio projection of v onto u
     */
    public static double ratioProjection(double vx, double vy, double ux, double uy) {
        if (ux == 0.0 && uy == 0) {
            return 0.0;
        }
        return (vx * ux + vy * uy) / (ux * ux + uy * uy);
    }

    /**
     * Project vector u on to v
     */
    public static double[] vectorProjection(double ux, double uy, double vx, double vy) {
        if (vx == 0.0 && vy == 0) {
            return new double[] {0.0, 0.0};
        }
        double scale = (ux * vx + uy * vy) / (vx * vx + vy * vy);
        return new double[] {vx * scale, vy * scale};
    }

    /**
     * Sort singles by angle
     */
    public static void sortSinglesByAngle(ArrayList<BaseSingle> singles, double angle) {
        Collections.sort(singles, new Comparator<BaseSingle>() {
            @Override
            public int compare(BaseSingle s1, BaseSingle s2) {
                double x1 = s1.getX() * MathUtils.quickCos((float) angle) - s1.getY() * MathUtils.quickSin((float) angle);
                double x2 = s2.getX() * MathUtils.quickCos((float) angle) - s2.getY() * MathUtils.quickSin((float) angle);
                if (x1 < x2) return -1;
                else if (x1 == x2) return 0;
                else return 1;
            }
        });
    }

    /**
     * Return the distance of the most forward single in the unit, according to the angle.
     * This is calculated by:
     * 1. Rotate all positions so that the face the input angle.
     * 2. Calculate the average position after the transformation.
     * 3. Find the single that has the smallest X (the most forward), and take the x-difference between them and the
     *    average x-position.
     */
    public static double mostForwardDistance(ArrayList<BaseSingle> singles, double angle) {
        double sumX = 0;
        double minX = Double.MAX_VALUE;
        for (BaseSingle single : singles) {
            double x = single.getX() * MathUtils.quickCos((float) angle)
                    - single.getY() * MathUtils.quickSin((float) angle);
            sumX += x;
            if (x < minX) minX = x;
        }
        double avgX = sumX / singles.size();
        return avgX - minX;
    }

    /**
     * Find the minimum of all numbers in the array
     */
    public static double findMin(double[] nums) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] < min) {
                min = nums[i];
            }
        }
        return min;
    }

    /**
     * Find the maximum of all numbers in the array.
     */
    public static double findMax(double[] nums) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > max) {
                max = nums[i];
            }
        }
        return max;
    }

    /**
     * Dot product of (x1, y1) and (x2, y2).
     */
    public static double dotProduct(double x1, double y1, double x2, double y2) {
        return x1 * x2 + y1 * y2;
    }

    /**
     * Return the length of the vector.
     */
    public static double norm(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Return the length of the vector using quick root function
     */
    public static double quickNorm(double x, double y) {
        return quickRoot2((float) (x * x + y * y));
    }

    /**
     * Generate hexagonal ring
     * We will use the 3D Hexagonal cubic system delineated here:
     * https://www.redblobgames.com/grids/hexagons/.
     * This system posits the hexagonal index as having 3 axis x, y, z, which changes as followed.
     *        (_, +1, -1)   (+1, _, -1)
     *                \      /
     * (-1, +1, _) - (_, _, _) - (+1, -1, _)
     *               /      \
     *      (-1, _, +1)   (_, -1, +1)
     */
    public static HashSet<Triplet<Integer, Integer, Integer>> getHexagonalIndicesRingAtOffset(int offset) {
        HashSet<Triplet<Integer, Integer, Integer>> arr = new HashSet<>();
        if (offset == 0) {
            arr.add(new Triplet<>(0, 0, 0));
            return arr;
        }

        // Start from the top left cell
        int x = 0;
        int y = offset;
        int z = -offset;

        // Then loop through each edge.
        for (int i = 0; i < offset; i++) {
            x += 1;
            y -= 1;
            arr.add(new Triplet<>(x, y, z));
        }
        for (int i = 0; i < offset; i++) {
            y -= 1;
            z += 1;
            arr.add(new Triplet<>(x, y, z));
        }
        for (int i = 0; i < offset; i++) {
            x -= 1;
            z += 1;
            arr.add(new Triplet<>(x, y, z));
        }
        for (int i = 0; i < offset; i++) {
            x -= 1;
            y += 1;
            arr.add(new Triplet<>(x, y, z));
        }
        for (int i = 0; i < offset; i++) {
            y += 1;
            z -= 1;
            arr.add(new Triplet<>(x, y, z));
        }
        for (int i = 0; i < offset; i++) {
            x += 1;
            z -= 1;
            arr.add(new Triplet<>(x, y, z));
        }

        // Return the triple array
        return arr;
    }

    /**
     * Generate offset based on hex triplet index
     */
    public static double[] generateOffsetBasedOnHexTripletIndices(int x, int y, int z, double scale) {
        double offsetX = 0;
        double offsetY = 0;

        // Handle x offset
        offsetX += scale * x * ROOT3O2;
        offsetY -= scale * x * 0.5;

        // Handle y offset
        offsetX -= scale * y * ROOT3O2;
        offsetY -= scale * y * 0.5;

        // Handle z offset
        offsetY += scale * z;
        return new double[] {offsetX, offsetY};
    }

    /**
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param numberOfPoints: the number of equally spaced points including the two endpoints
     * @return
     */
    public static double[][] findEqualSpacePointsGivenEndPoints(double x1, double y1, double x2, double y2, int numberOfPoints){
        double[][] pointsCoordinates = new double[0][2];
        // Adding the first endpoint to the 2D array
        pointsCoordinates[0][0] = x1;
        pointsCoordinates[0][1] = y1;

        int i = 1;
        while ( i <=  numberOfPoints){
            double newX = x1 + (x1 - x2)/(numberOfPoints - 1)*i;
            double newY = y1 + (y1 - y2)/(numberOfPoints - 1)*i;
            int numberOfCurrentRow = pointsCoordinates.length;
            pointsCoordinates[numberOfCurrentRow][0] = newX;
            pointsCoordinates[numberOfCurrentRow][1] = newY;
            i++;
        }
        return pointsCoordinates;
    }

    /**
     * This function returns the unit vector from point 1 (x1, y1) to point 2 (x2, y2)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double[] unitVectorBetweenTwoPoints(double x1,double y1, double x2, double y2){
        double x12 = x2 - x1;
        double y12 = y2 - y1;
        double unitX = x12/quickNorm(x12, y12);
        double unitY = y12/quickNorm(x12, y12);
        double[] unitVector = new double[2];
        unitVector[0] = unitX;
        unitVector[1] = unitY;

        return unitVector;
    }

    public static double[][] doubleRowConcatenate(double[][] var1, double [][] var2){
        double rowLength1 = var1.length;
        double columnLength1 = var1[0].length;

        double rowLength2 = var2.length;
        double columnLength2 = var2[0].length;

        double[] temp = new double[]{columnLength1, columnLength2};
        double minColumnLength = findMin(temp);

        double[][] output = new double[0][(int) minColumnLength];

        for (int i = 0; i< rowLength1; ++i){
            if (minColumnLength >= 0) System.arraycopy(var1[i], 0, output[i], 0, (int) minColumnLength);
        }

        for (int i = 0; i< rowLength2; ++i){
            if (minColumnLength >= 0) System.arraycopy(var2[i], 0, output[i + (int) rowLength1], 0, (int) minColumnLength);
        }
        return output;
    }


}



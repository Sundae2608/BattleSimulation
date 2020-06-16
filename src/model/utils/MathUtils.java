package model.utils;

import it.unimi.dsi.util.XoShiRo256PlusRandom;
import javafx.util.Pair;
import model.singles.BaseSingle;
import model.units.BaseUnit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    public static boolean equal(double a, double b) {
        double diff = Math.abs(a - b);
        return diff < MathUtils.EPSILON;
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
     * Distance between two points
     */
    public static double quickDistance(double x1, double y1, double x2, double y2) {
        return quickRoot2((float) squareDistance(x1, y1, x2, y2));
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
     * Project vector v on to u
     */
    public static double[] vectorProjection(double vx, double vy, double ux, double uy) {
        if (ux == 0.0 && uy == 0) {
            return new double[] {0.0};
        }
        double normal_projection = (vx * ux + vy * uy) / (ux * ux + uy * uy);
        return new double[] {normal_projection * ux, normal_projection * uy};
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
     * Find the maximum of all numbers in the array
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

}

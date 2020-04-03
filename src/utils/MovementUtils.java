package utils;

public final class MovementUtils {
    public static double rotate(double angle, double angleGoal, double rotationSpeed) {

        // First, calculate the signed angle
        double angleDifference = angleGoal - angle;
        if (angleDifference > Math.PI) angleDifference -= Math.PI * 2;
        else if (angleDifference < - Math.PI) angleDifference += Math.PI * 2;

        if (Math.abs(angleDifference) < rotationSpeed) angle = angleGoal;
        else if (angleDifference < 0) angle -= rotationSpeed;
        else angle += rotationSpeed;

        if (angle > Math.PI) angle -= Math.PI * 2;
        else if (angle < - Math.PI) angle += Math.PI * 2;

        // Return the new angle
        return angle;
    }
}

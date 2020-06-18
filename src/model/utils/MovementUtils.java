package model.utils;

public final class MovementUtils {
    public static double rotate(double angle, double angleGoal, double rotationSpeed) {

        // First, calculate the signed angle
        double angleDifference = angleGoal - angle;
        if (angleDifference > Math.PI) angleDifference -= MathUtils.PIX2;
        else if (angleDifference < - Math.PI) angleDifference +=MathUtils.PIX2;

        if (Math.abs(angleDifference) < rotationSpeed) angle = angleGoal;
        else if (angleDifference < 0) angle -= rotationSpeed;
        else angle += rotationSpeed;

        if (angle > Math.PI) angle -= MathUtils.PIX2;
        else if (angle < - Math.PI) angle += MathUtils.PIX2;

        // Return the new angle
        return angle;
    }
}

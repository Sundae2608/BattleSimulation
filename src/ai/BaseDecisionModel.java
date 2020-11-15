package ai;

public class BaseDecisionModel {

    public final static double WEIGHT_WALKING_DISTANCE = -1.0;
    public final static double WEIGHT_TOTAL_ENEMY_ANGLE = -1.0;
    public final static double WEIGHT_OVERLAPPING_WITH_ENEMY = 1000.0;

    public double calculateDecisionValue(DecisionFactor factor) {
        return WEIGHT_WALKING_DISTANCE * factor.walkingDistance
                + WEIGHT_TOTAL_ENEMY_ANGLE * factor.totalEnemyAngle
                + (factor.overlappingWithEnemy ? WEIGHT_OVERLAPPING_WITH_ENEMY : 0);
    }
}

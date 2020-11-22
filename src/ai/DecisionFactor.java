package ai;

/**
 * DecisionFactor class contains all variables in which the AI uses to consider the next move in the decision making
 * process.
 */
public class DecisionFactor {

    // Walking distance
    double walkingDistance;

    // Total angle against enemy.
    double totalEnemyAngle;

    // Difference in strength against the enemy unit, measured by total number of alive units.
    // TODO: Split into melee strength and projectile strength.
    double unitStrengthCountDiff;

    // Difference in height against the enemy unit.
    double unitHeightDiff;

    // Effectiveness according to types
    double typeEffectiveness;

    // Whether this position overlaps with ally or enemies
    boolean overlappingWithAlly;
    boolean overlappingWithEnemy;

    // The amount of time already spent waiting.
    int waitSteps;
}

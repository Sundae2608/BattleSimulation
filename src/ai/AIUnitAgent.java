package ai;

import model.GameEnvironment;
import model.units.BaseUnit;
import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * AIUnitAgent is a class that controls the movement of each unit
 */
public class AIUnitAgent {
    // The Unit in which the agent controls
    private BaseUnit unit;

    // The environment that the unit belongs to
    private GameEnvironment env;

    // The view of the environment according to the AI.
    private AIView state;

    // The model used to make decisions
    private BaseDecisionModel model;

    public AIUnitAgent(BaseUnit unit, GameEnvironment env){
        this.env = env;
        this.unit = unit;
        this.state = new AIView(env, this);
        this.model = new BaseDecisionModel();
    }

    /**
     * Move method is called whenever an unit needs to take a decision.
     * The method updates the internal view of the AI, assess different potential decisions and make them.
     */
    public void move() {
        // Update the game state so that the AI agent has the most updated view.
        state.updateState();

        // Create a map of potential decision with its decision factors
        ArrayList<Decision> potentialDecisions = generatePotentialDecisions();
        HashMap<Decision, DecisionFactor> decisionFactorMap = new HashMap<>();
        for (Decision decision : potentialDecisions) {
            decisionFactorMap.put(decision, getDecisionFactor(decision));
        }

        // Calculate the score of each decision
        HashMap<Decision, Double> decisionScoreMap = new HashMap<>();
        for (Decision decision : potentialDecisions) {
            double score = model.calculateDecisionValue(decisionFactorMap.get(decision));
            decisionScoreMap.put(decision, score);
        }

        // Make decision based on the score
        double maxScore = -Double.MIN_VALUE;
        Decision maxDecision = null;
        for (Decision decision : decisionScoreMap.keySet()) {
            double score = decisionScoreMap.get(decision);
            if (score > maxScore) {
                maxScore = score;
                maxDecision = decision;
            }
        }

        // Control the unit based on made decisions, if a decision is available.
        if (maxDecision != null) {
            unit.moveFormationKeptTo(maxDecision.x, maxDecision.y, maxDecision.facingAngle);
        }
    }

    /**
     * Calculate different decision factors.
     */
    private DecisionFactor getDecisionFactor(Decision decision) {
        DecisionFactor factor = new DecisionFactor();

        // Walking distance from the unit current position to the end position.
        factor.walkingDistance = MathUtils.quickDistance(
                decision.x, decision.y, unit.getAverageX(), unit.getAverageY());

        // Check whether the units overlap with allies
        // TODO: This could be optimized further, since positional overlapping doesn't have to be recalculated all the
        //  time. Also, overlapping does not need to check with bounding box.
        // TODO: Also, create something called AI
        factor.overlappingWithAlly = false;
        for (BaseUnit allyUnit : state.allyUnits) {
            if (PhysicUtils.checkPolygonPolygonCollision(
                    unit.getBoundingBoxAtPos(decision.x, decision.y, decision.facingAngle),
                    allyUnit.getBoundingBox())) {
                factor.overlappingWithAlly = true;
                break;
            }
        }

        // Check whether the units overlap with enemies. If there is overlap, calculated difference in strength
        factor.overlappingWithEnemy = false;
        factor.unitStrengthCountDiff = unit.getNumAlives();
        factor.unitHeightDiff = 0;
        for (BaseUnit enemyUnit : state.enemyUnits) {
            if (PhysicUtils.checkPolygonPolygonCollision(
                    unit.getBoundingBoxAtPos(decision.x, decision.y, decision.facingAngle),
                    enemyUnit.getBoundingBox())) {
                factor.overlappingWithEnemy = true;
                factor.unitStrengthCountDiff -= enemyUnit.getNumAlives();
                factor.unitHeightDiff += unit.getAverageZ() - enemyUnit.getAverageZ();
                break;
            }
        }

        // Calculate total angle difference against the enemy. This helps us optimized the facing angle of the unit
        // so that it faces the enemy instead of showing its back to the enemy
        // TODO: Add total enemy angle weighted by strength.
        factor.totalEnemyAngle = 0;
        for (BaseUnit enemyUnit : state.enemyUnits) {
            double enemyAngle = MathUtils.atan2(
                    enemyUnit.getAverageY() - unit.getAverageY(),
                    enemyUnit.getAverageX() - unit.getAverageX()
            );
            factor.totalEnemyAngle = Math.abs(MathUtils.signedAngleDifference(unit.getAnchorAngle(), enemyAngle));
        }

        // TODO: Type effectiveness. This should comes from an input table mapping strength and weakness of different
        //  unit, and is used to calculate combat bonus and decision factor.

        // TODO: Wait steps. If the units do nothing, we add this variable so that they are more "biased toward action"
        return factor;
    }

    /**
     * Return a list of potential decisions for the unit.
     */
    private ArrayList<Decision> generatePotentialDecisions() {
        ArrayList<Decision> decisions = new ArrayList<>();
        for (int i = 0; i < DecisionConstants.AI_MOVEMENT_ANGLE_GRANULARITY; i++) {
            double moveAngle = 1.0 * i / DecisionConstants.AI_MOVEMENT_ANGLE_GRANULARITY * MathUtils.PIX2;
            double x = MathUtils.quickCos((float) moveAngle) * DecisionConstants.AI_MOVEMENT_RANGE + unit.getAverageX();
            double y = MathUtils.quickSin((float) moveAngle) * DecisionConstants.AI_MOVEMENT_RANGE + unit.getAverageY();
            for (int j = 0; j < DecisionConstants.AI_FACING_ANGLE_GRANULARITY; j++) {
                double facingAngle = 1.0 * j / DecisionConstants.AI_FACING_ANGLE_GRANULARITY * MathUtils.PIX2;
                Decision newDecision = new Decision();
                newDecision.x = x;
                newDecision.y = y;
                newDecision.facingAngle = facingAngle;
                decisions.add(newDecision);
            }
        }
        return decisions;
    }

    public BaseUnit getUnit(){
        return unit;
    }
}

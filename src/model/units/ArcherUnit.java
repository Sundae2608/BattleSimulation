package model.units;

import model.GameEnvironment;
import model.algorithms.ObjectHasher;
import model.enums.PoliticalFaction;
import model.events.EventBroadcaster;
import model.settings.GameSettings;
import model.singles.ArcherSingle;
import model.singles.BaseSingle;
import model.singles.SingleStats;
import model.terrain.Terrain;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ArcherUnit extends BaseUnit {

    // Variation in width position to simulate archers finding angle to shoot.
    double[] widthVariation;
    double[] depthVariation;

    // Unit fired against (unique to archers)
    // Indicate the model.units the archer is currently aimed at .
    BaseUnit unitFiredAgainst;
    private ArrayList<BaseSingle> targetIterator;
    private int iteratorIndex;

    public ArcherUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, UnitStats unitStats,
                      SingleStats singleStats, int unitWidth, ObjectHasher hasher, GameEnvironment inputEnv) {
        super(unitStats, inputEnv);

        // Assign default attributes
        currUnitPatience = unitStats.patience;

        // Assign political attributes
        politicalFaction = faction;

        // Assign composition attribute
        width = unitWidth;
        depth = unitSize / width;

        // Assign physical attributes
        speed = unitStats.speed;

        // Assign anchor position and direction with given x, y, angle. This will be the position of two front soldiers.
        anchorX = x;
        anchorY = y;
        anchorAngle = angle;

        // Calculate unit vector
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Width variation
        generatePositionalVariation(unitSize);

        // Create troops and set starting positions for each troop
        double topX = x - (width - 1) * unitStats.spacing * sideUnitX / 2;
        double topY = y - (width - 1) * unitStats.spacing * sideUnitY / 2;
        troops = new ArrayList<>();
        aliveTroopsMap = new HashMap<>();
        aliveTroopsFormation = new BaseSingle[depth][width];
        for (int i = 0; i < unitSize; i++) {
            int row = i / width;
            int col = i % width;
            double singleX = topX + col * unitStats.spacing * sideUnitX + widthVariation[i] * sideUnitX
                    + (row * unitStats.spacing + depthVariation[i]) * downUnitX;
            double singleY = topY + col * unitStats.spacing * sideUnitY + widthVariation[i] * sideUnitY
                    + (row * unitStats.spacing + depthVariation[i]) * downUnitY;
            BaseSingle single = new ArcherSingle(singleX, singleY, politicalFaction, this, singleStats, i, hasher);
            troops.add(single);
            aliveTroopsFormation[row][col] = single;
            aliveTroopsMap.put(single, i);
        }

        // Goal position and direction are equal to anchor ones so that the army stand still.
        goalX = anchorX;
        goalY = anchorY;
        goalAngle = anchorAngle;

        // Set of flanker counts and frontline patient counters
        frontlinePatientCounters = new int[width];
        flankersCount = new int[width];
    }

    @Override
    public void updateGoalPositions() {

        // Convert angle to unit vector
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = anchorX - (width - 1) * unitStats.spacing * sideUnitX / 2;
        double topY = anchorY - (width - 1) * unitStats.spacing * sideUnitY / 2;

        // Update troops goal positions
        for (int i = 0; i < troops.size(); i++) {

            int row = i / width;
            int col = i % width;
            double xGoalSingle = topX + col * unitStats.spacing * sideUnitX + widthVariation[i] * sideUnitX
                    + (row * unitStats.spacing + depthVariation[i]) * downUnitX;
            double yGoalSingle = topY + col * unitStats.spacing * sideUnitY + widthVariation[i] * sideUnitY
                    + (row * unitStats.spacing + depthVariation[i]) * downUnitY;

            // Set the goal and change the state
            BaseSingle troop = troops.get(i);
            troop.setxGoal(xGoalSingle);
            troop.setyGoal(yGoalSingle);
            troop.setAngleGoal(anchorAngle);
        }
    }

    /**
     * Generates width variation for archers.
     * Archers need space to shoot the enemy, so it would make sense to actually let them stand slightly off their width
     * position too simulate them naturally trying to find angle to shooot at the enemy.
     */
    protected void generatePositionalVariation(int unitSize) {
        widthVariation = new double[unitSize];
        depthVariation = new double[unitSize];
        for (int i = 0; i < unitSize; i++) {
            widthVariation[i] = MathUtils.randDouble(- unitStats.widthVariation, unitStats.widthVariation);
            depthVariation[i] = MathUtils.randDouble(- unitStats.depthVariation, unitStats.depthVariation);
        }
    }

    /**
     * Set unit to be fired at by the archers.
     */
    public void setUnitFiredAt(BaseUnit unitFiredAgainst) {
        this.unitFiredAgainst = unitFiredAgainst;
        if (unitFiredAgainst == null) {
            targetIterator = new ArrayList<>();
        } else {
            targetIterator = new ArrayList(unitFiredAgainst.getAliveTroopsSet());
        }
    }

    /**
     * Pick a target for the archer. it will take advantage of the prebuilt iterator for randomness and efficiency
     */
    public BaseSingle pickNextTarget() {
        if (unitFiredAgainst.getAliveTroopsSet().size() == 0) {
            unitFiredAgainst = null;
            return null;
        }
        while (true) {
            if (iteratorIndex >= targetIterator.size()) {
                targetIterator = new ArrayList(unitFiredAgainst.getAliveTroopsSet());
                iteratorIndex = 0;
            }
            BaseSingle potentialTarget = targetIterator.get(iteratorIndex);
            iteratorIndex++;
            if (unitFiredAgainst.getAliveTroopsSet().contains(potentialTarget)) {
                return potentialTarget;
            }
        }
    }

    public BaseUnit getUnitFiredAgainst() {
        return unitFiredAgainst;
    }

}

package units;

import algorithms.ObjectHasher;
import constants.ArcherConstants;
import singles.ArcherSingle;
import singles.BaseSingle;
import utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class ArcherUnit extends BaseUnit{

    // Variation in width position to simulate archers finding angle to shoot.
    double[] widthVariation;
    double[] depthVariation;

    // Unit fired against (unique to archers)
    // Indicate the units the archer is currently aimed at .
    BaseUnit unitFiredAgainst;
    private ArrayList<BaseSingle> targetIterator;
    private int iteratorIndex;

    public ArcherUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, int unitWidth,
                      ObjectHasher hasher) {

        // Assign default attributes
        spacing = ArcherConstants.SPACING;
        unitPatience = ArcherConstants.UNIT_FIGHT_DELAY;
        currUnitPatience = ArcherConstants.UNIT_FIGHT_DELAY;
        // Assign political attributes
        politicalFaction = faction;

        // Assign composition attribute
        width = unitWidth;
        depth = unitSize / width;

        // Assign physical attributes
        unitSpeed = ArcherConstants.SPEED_STAT;

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
        double topX = x - (width - 1) * spacing * sideUnitX / 2;
        double topY = y - (width - 1) * spacing * sideUnitY / 2;
        troops = new ArrayList<>();
        for (int i = 0; i < unitSize; i++) {
            double singleX = topX + (i % width) * spacing * sideUnitX + widthVariation[i] * sideUnitX
                    + ((i / width) * spacing + depthVariation[i]) * downUnitX;
            double singleY = topY + (i % width) * spacing * sideUnitY + widthVariation[i] * sideUnitY
                    + ((i / width) * spacing + depthVariation[i]) * downUnitY;
            troops.add(new ArcherSingle(singleX, singleY, politicalFaction, this, i, hasher));
        }
        aliveTroopsSet = new HashSet<>(troops);

        // Goal position and direction are equal to anchor ones so that the army stand still.
        goalX = anchorX;
        goalY = anchorY;
        goalAngle = anchorAngle;
    }

    @Override
    public void updateGoalPositions() {

        // Convert angle to unit vector
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = anchorX - (width - 1) * spacing * sideUnitX / 2;
        double topY = anchorY - (width - 1) * spacing * sideUnitY / 2;

        // Update troops goal positions
        for (int i = 0; i < troops.size(); i++) {

            int row = i / width;
            int col = i % width;
            double xGoalSingle = topX + col * spacing * sideUnitX + widthVariation[i] * sideUnitX
                    + (row * spacing + depthVariation[i]) * downUnitX;
            double yGoalSingle = topY + col * spacing * sideUnitY + widthVariation[i] * sideUnitY
                    + (row * spacing + depthVariation[i]) * downUnitY;

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
            widthVariation[i] = MathUtils.randDouble(- ArcherConstants.WIDTH_VARIATION, ArcherConstants.WIDTH_VARIATION);
            depthVariation[i] = MathUtils.randDouble(- ArcherConstants.DEPTH_VARIATION, ArcherConstants.DEPTH_VARIATION);
        }
    }

    /**
     * Set unit to be fired at by the archers.
     */
    public void setUnitFiredAt(BaseUnit unitFiredAgainst) {
        this.unitFiredAgainst = unitFiredAgainst;
        targetIterator = new ArrayList(unitFiredAgainst.getAliveTroopsSet());
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

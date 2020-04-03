package units;

import constants.ArcherConstants;
import constants.SkirmisherConstants;
import singles.BaseSingle;
import singles.SkirmisherSingle;
import utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class SkirmisherUnit extends BaseUnit {

    // Variation in width position to simulate archers finding angle to shoot.
    double[] widthVariation;
    double[] depthVariation;

    public SkirmisherUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, int unitWidth) {

        // Assign default attributes
        spacing = SkirmisherConstants.SPACING;
        unitPatience = SkirmisherConstants.UNIT_FIGHT_DELAY;
        currUnitPatience = SkirmisherConstants.UNIT_FIGHT_DELAY;

        // Assign political attributes
        politicalFaction = faction;

        // Assign composition attribute
        width = unitWidth;
        depth = unitSize / width;

        // Assign physical attributes
        unitSpeed = SkirmisherConstants.SPEED_STAT;

        // Assign anchor position and direction with given x, y, angle. This will be the position of two front soldiers.
        anchorX = x;
        anchorY = y;
        anchorAngle = angle;

        // Width variation
        generatePositionalVariation(unitSize);

        // Calculate unit vector
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = x - (width - 1) * spacing * sideUnitX / 2;
        double topY = y - (width - 1) * spacing * sideUnitY / 2;
        troops = new ArrayList<>();
        for (int i = 0; i < unitSize; i++) {
            double singleX = topX + (i % width) * spacing * sideUnitX + (i / width) * spacing * downUnitX;
            double singleY = topY + (i % width) * spacing * sideUnitY + (i / width) * spacing * downUnitY;
            troops.add(new SkirmisherSingle(singleX, singleY, politicalFaction, this, i));
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
            widthVariation[i] = MathUtils.randDouble(- SkirmisherConstants.WIDTH_VARIATION, SkirmisherConstants.WIDTH_VARIATION);
            depthVariation[i] = MathUtils.randDouble(- SkirmisherConstants.DEPTH_VARIATION, SkirmisherConstants.DEPTH_VARIATION);
        }
    }
}

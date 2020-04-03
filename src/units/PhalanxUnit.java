package units;

import constants.PhalanxConstants;
import singles.BaseSingle;
import singles.PhalanxSingle;
import utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class PhalanxUnit extends BaseUnit{

    public PhalanxUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, int unitWidth) {

        // Assign default attributes
        spacing = PhalanxConstants.SPACING;
        unitPatience = PhalanxConstants.UNIT_FIGHT_DELAY;
        currUnitPatience = PhalanxConstants.UNIT_FIGHT_DELAY;

        // Assign political attributes
        politicalFaction = faction;

        // Assign composition attribute
        width = unitWidth;
        depth = unitSize / width;

        // Assign physical attributes
        unitSpeed = PhalanxConstants.SPEED_STAT;

        // Calculate unit vector
        double downUnitX = MathUtils.quickCos((float) (angle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (angle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (angle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (angle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = x - (width - 1) * spacing * sideUnitX / 2;
        double topY = y - (width - 1) * spacing * sideUnitY / 2;
        troops = new ArrayList<>();
        for (int i = 0; i < unitSize; i++) {
            double singleX = topX + (i % width) * spacing * sideUnitX + (i / width) * spacing * downUnitX;
            double singleY = topY + (i % width) * spacing * sideUnitY + (i / width) * spacing * downUnitY;
            troops.add(new PhalanxSingle(singleX, singleY, politicalFaction, this, i));
        }
        aliveTroopsSet = new HashSet<>(troops);

        // Assign anchor position and direction with given x, y, angle. This will be the position of two front soldiers.
        anchorX = x;
        anchorY = y;
        anchorAngle = angle;

        // Goal position and direction are equal to anchor ones so that the army stand still.
        goalX = anchorX;
        goalY = anchorY;
        goalAngle = anchorAngle;
    }

    @Override
    public void updateGoalPositions() {
        // Convert angle to unit vector
        double offAngleFirstRow = PhalanxConstants.OFF_ANGLE_FIRST_ROWS;
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double downUnitXFirstFive = MathUtils.quickCos((float) (anchorAngle + Math.PI + offAngleFirstRow));
        double downUnitYFirstFive = MathUtils.quickSin((float) (anchorAngle + Math.PI + offAngleFirstRow));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = anchorX - (width - 1) * spacing * sideUnitX / 2;
        double topY = anchorY - (width - 1) * spacing * sideUnitY / 2;

        // Update troops goal positions
        int numFirstRows = PhalanxConstants.NUM_FIRST_ROWS;
        for (int i = 0; i < troops.size(); i++) {

            int row = i / width;
            int col = i % width;
            double xGoalSingle = topX + col * spacing * sideUnitX
                    + Math.min(row, numFirstRows) * spacing * downUnitXFirstFive
                    + Math.max(0, row - numFirstRows) * spacing * downUnitX;
            double yGoalSingle = topY + col * spacing * sideUnitY
                    + Math.min(row, numFirstRows) * spacing * downUnitYFirstFive
                    + Math.max(0, row - numFirstRows) * spacing * downUnitY;

            // Set the goal and change the state
            BaseSingle troop = troops.get(i);
            troop.setxGoal(xGoalSingle);
            troop.setyGoal(yGoalSingle);
            troop.setAngleGoal(anchorAngle);
        }
    }
}

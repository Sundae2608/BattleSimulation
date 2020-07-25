package model.units;

import model.GameEnvironment;
import model.enums.PoliticalFaction;
import model.singles.BaseSingle;
import model.singles.PhalanxSingle;
import model.singles.SingleStats;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PhalanxUnit extends BaseUnit{

    public PhalanxUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction,
                       UnitStats unitStats, SingleStats singleStats, int unitWidth, GameEnvironment inputEnv) {
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

        // Calculate unit vector
        double downUnitX = MathUtils.quickCos((float) (angle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (angle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (angle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (angle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = x - (width - 1) * unitStats.spacing * sideUnitX / 2;
        double topY = y - (width - 1) * unitStats.spacing * sideUnitY / 2;
        troops = new ArrayList<>();
        aliveTroopsFormation = new BaseSingle[depth][width];
        aliveTroopsMap = new HashMap<>();
        for (int i = 0; i < unitSize; i++) {
            int row = i / width;
            int col = i % width;
            double singleX = topX + (i % width) * unitStats.spacing * sideUnitX + (i / width) * unitStats.spacing * downUnitX;
            double singleY = topY + (i % width) * unitStats.spacing * sideUnitY + (i / width) * unitStats.spacing * downUnitY;
            BaseSingle single = new PhalanxSingle(singleX, singleY, politicalFaction, this, singleStats, i);
            troops.add(single);
            aliveTroopsFormation[row][col] = single;
            aliveTroopsMap.put(single, i);
        }

        // Assign anchor position and direction with given x, y, angle. This will be the position of two front soldiers.
        anchorX = x;
        anchorY = y;
        anchorAngle = angle;

        // Goal position and direction are equal to anchor ones so that the army stand still.
        goalX = anchorX;
        goalY = anchorY;
        goalAngle = anchorAngle;

        // Set of flanker counts and frontline patient counters
        frontLinePatientCounters = new int[width];
        flankersCount = new int[width];
        flankerOffsets = new ArrayList[width];
        for (int i = 0; i < width; i++) {
            flankerOffsets[i] = new ArrayList<>();
        }
    }

    @Override
    public void updateGoalPositions() {
        // Convert angle to unit vector
        double offAngleFirstRow = unitStats.offAngleFirstRow;
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double downUnitXFirstFive = MathUtils.quickCos((float) (anchorAngle + Math.PI + offAngleFirstRow));
        double downUnitYFirstFive = MathUtils.quickSin((float) (anchorAngle + Math.PI + offAngleFirstRow));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = anchorX - (width - 1) * unitStats.spacing * sideUnitX / 2;
        double topY = anchorY - (width - 1) * unitStats.spacing * sideUnitY / 2;

        // Update troops goal positions
        int numFirstRows = unitStats.numFirstRows;

        for (int i = 0; i < aliveTroopsFormation.length; i++) {
            for (int j = 0; j < aliveTroopsFormation[0].length; i++) {
                if (aliveTroopsFormation[i][j] == null) continue;
                double xGoalSingle = topX + j * unitStats.spacing * sideUnitX
                        + Math.min(i, numFirstRows) * unitStats.spacing * downUnitXFirstFive
                        + Math.max(0, i - numFirstRows) * unitStats.spacing * downUnitX;
                double yGoalSingle = topY + j * unitStats.spacing * sideUnitY
                        + Math.min(i, numFirstRows) * unitStats.spacing * downUnitYFirstFive
                        + Math.max(0, i - numFirstRows) * unitStats.spacing * downUnitY;

                // Set the goal and change the state
                BaseSingle troop = aliveTroopsFormation[i][j];
                troop.setxGoal(xGoalSingle);
                troop.setyGoal(yGoalSingle);
                troop.setAngleGoal(anchorAngle);
            }
        }
    }
}

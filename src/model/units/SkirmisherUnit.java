package model.units;

import model.GameEnvironment;
import model.enums.PoliticalFaction;
import model.singles.BaseSingle;
import model.singles.SingleStats;
import model.singles.SkirmisherSingle;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SkirmisherUnit extends BaseUnit {

    // Variation in width position to simulate archers finding angle to shoot.
    double[] widthVariation;
    double[] depthVariation;

    public SkirmisherUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction,
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
            BaseSingle single = new SkirmisherSingle(singleX, singleY, politicalFaction, this, singleStats, i);
            troops.add(single);
            aliveTroopsFormation[row][col] = single;
            aliveTroopsMap.put(single, i);
        }

        // Post initialization
        postInitialization();
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

        for (int i = 0; i < aliveTroopsFormation.length; i++) {
            for (int j = 0; j < aliveTroopsFormation[0].length; i++) {
                if (aliveTroopsFormation[i][j] == null) continue;
                int row = i / width;
                int col = i % width;
                double xGoalSingle = topX + col * unitStats.spacing * sideUnitX + widthVariation[i] * sideUnitX
                        + (row * unitStats.spacing + depthVariation[i]) * downUnitX;
                double yGoalSingle = topY + col * unitStats.spacing * sideUnitY + widthVariation[i] * sideUnitY
                        + (row * unitStats.spacing + depthVariation[i]) * downUnitY;

                // Set the goal and change the state
                BaseSingle troop = aliveTroopsFormation[i][j];
                troop.setxGoal(xGoalSingle);
                troop.setyGoal(yGoalSingle);
                troop.setAngleGoal(anchorAngle);
            }
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
}

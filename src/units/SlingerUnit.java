package units;

import constants.SlingerConstants;
import singles.SlingerSingle;
import utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class SlingerUnit extends BaseUnit{
    public SlingerUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, int unitWidth) {

        // Assign default attributes
        spacing = SlingerConstants.SPACING;
        unitPatience = SlingerConstants.UNIT_FIGHT_DELAY;
        currUnitPatience = SlingerConstants.UNIT_FIGHT_DELAY;

        // Assign political attributes
        politicalFaction = faction;

        // Assign composition attribute
        width = unitWidth;
        depth = unitSize / width;

        // Assign physical attributes
        unitSpeed = SlingerConstants.SPEED_STAT;

        // Assign anchor position and direction with given x, y, angle. This will be the position of two front soldiers.
        anchorX = x;
        anchorY = y;
        anchorAngle = angle;

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
            troops.add(new SlingerSingle(singleX, singleY, politicalFaction, this, i));
        }
        aliveTroopsSet = new HashSet<>(troops);

        // Goal position and direction are equal to anchor ones so that the army stand still.
        goalX = anchorX;
        goalY = anchorY;
        goalAngle = anchorAngle;
    }
}

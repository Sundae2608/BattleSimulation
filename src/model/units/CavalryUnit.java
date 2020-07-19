package model.units;

import model.enums.PoliticalFaction;
import model.events.EventBroadcaster;
import model.settings.GameSettings;
import model.singles.BaseSingle;
import model.singles.CavalrySingle;
import model.singles.SingleStats;
import model.terrain.Terrain;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CavalryUnit extends BaseUnit {

    public CavalryUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, UnitStats unitStats,
                       SingleStats singleStats, int unitWidth, Terrain terrain,
                       EventBroadcaster broadcaster, GameSettings gameSettings) {
        super(unitStats, terrain, broadcaster, gameSettings);

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
            BaseSingle single = new CavalrySingle(singleX, singleY, politicalFaction, this, singleStats, i);
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
}

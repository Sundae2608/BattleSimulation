package model.units;

import model.algorithms.ObjectHasher;
import model.enums.PoliticalFaction;
import model.events.EventBroadcaster;
import model.singles.BaseSingle;
import model.singles.CatapultSingle;
import model.singles.SingleStats;
import model.terrain.Terrain;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CatapultUnit extends BaseUnit {

    // Unit fired against (unique to archers)
    // Indicate the model.units the archer is currently aimed at .
    BaseUnit unitFiredAgainst;
    private ArrayList<BaseSingle> targetIterator;
    private int iteratorIndex;

    public CatapultUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, UnitStats unitStats,
                       SingleStats singleStats, int unitWidth, ObjectHasher hasher, Terrain terrain,
                       EventBroadcaster broadcaster) {
        super(unitStats, terrain, broadcaster);

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
            double singleX = topX + col * unitStats.spacing * sideUnitX
                    + (row * unitStats.spacing) * downUnitX;
            double singleY = topY + col * unitStats.spacing * sideUnitY
                    + (row * unitStats.spacing) * downUnitY;
            BaseSingle single = new CatapultSingle(singleX, singleY, politicalFaction, this, singleStats, i, hasher);
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
        for (int i = 0; i < aliveTroopsFormation.length; i++) {
            for (int j = 0; j < aliveTroopsFormation[0].length; j++) {
                if (aliveTroopsFormation[i][j] == null) continue;
                double xGoalSingle = topX + j * unitStats.spacing * sideUnitX
                        + (i * unitStats.spacing) * downUnitX;
                double yGoalSingle = topY + j * unitStats.spacing * sideUnitY
                        + (i * unitStats.spacing) * downUnitY;
                aliveTroopsFormation[i][j].setxGoal(xGoalSingle);
                aliveTroopsFormation[i][j].setyGoal(yGoalSingle);
                aliveTroopsFormation[i][j].setAngleGoal(anchorAngle);
            }
        }
    }

    /**
     * Set unit to be fired at by the archers.
     */
    public void setUnitFiredAt(BaseUnit unitFiredAgainst) {
        this.unitFiredAgainst = unitFiredAgainst;
        if (unitFiredAgainst == null) return;
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

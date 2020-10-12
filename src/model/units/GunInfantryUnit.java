package model.units;

import model.GameEnvironment;
import model.algorithms.HitscanHasher;
import model.enums.PoliticalFaction;
import model.singles.BaseSingle;
import model.singles.GunInfantrySingle;
import model.singles.SingleStats;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class GunInfantryUnit extends BaseUnit {

    // Unit fired against (unique to archers)
    // Indicate the model.units the archer is currently aimed at .
    BaseUnit unitFiredAgainst;
    private ArrayList<BaseSingle> targetIterator;
    private int iteratorIndex;

    public GunInfantryUnit(double x, double y, double angle, int unitSize, PoliticalFaction faction, UnitStats unitStats,
                           SingleStats singleStats, int unitWidth, HitscanHasher hitscanHasher, GameEnvironment inputEnv) {
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

        // Create troops and set starting positions for each troop
        double topX = x - (width - 1) * unitStats.spacing * sideUnitX / 2;
        double topY = y - (width - 1) * unitStats.spacing * sideUnitY / 2;
        troops = new ArrayList<>();
        aliveTroopsMap = new HashMap<>();
        aliveTroopsFormation = new BaseSingle[depth][width];
        for (int i = 0; i < unitSize; i++) {
            int row = i / width;
            int col = i % width;
            double singleX = topX + col * unitStats.spacing * sideUnitX + row * unitStats.spacing * downUnitX;
            double singleY = topY + col * unitStats.spacing * sideUnitY + row * unitStats.spacing * downUnitY;
            BaseSingle single = new GunInfantrySingle(
                    singleX, singleY, politicalFaction, this, singleStats, hitscanHasher);
            troops.add(single);
            aliveTroopsFormation[row][col] = single;
            aliveTroopsMap.put(single, i);
        }

        // Post initialization
        postInitialization();
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
     * Pick a target for the gun unit. it will take advantage of the prebuilt iterator for randomness and efficiency
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
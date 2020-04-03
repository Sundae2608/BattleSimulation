package algorithms;

import settings.ExperimentSettings;
import singles.BaseSingle;
import singles.CavalrySingle;
import singles.SingleState;
import units.ArcherUnit;
import units.BaseUnit;
import utils.PhysicUtils;

import java.util.*;

/**
 * The TroopHasher groups each object into the hashMap based on its Position. This is to reduce the number of collusion
 * candidate and increase the efficiency of collision modifier
 * Written by: Son Pham
 */
public class TroopHasher {

    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<BaseSingle> activeTroops;
    private ArrayList<BaseUnit> units;
    private HashSet<BaseUnit> activeUnits;

    // Hash map containing
    private HashMap<Long, ArrayList<BaseSingle>> hashMap;

    // Experiment settings
    private ExperimentSettings experimentSettings;

    public TroopHasher(int xDivision, int yDivision, ExperimentSettings inputSettings) {
        xDiv = xDivision;
        yDiv = yDivision;
        activeTroops = new ArrayList<>();
        units = new ArrayList<>();
        hashMap = new HashMap<>();
        activeUnits = new HashSet<>();

        experimentSettings = inputSettings;
    }

    /**
     * Add a unit to the space hasher
     */
    public void addUnit(BaseUnit unit) {
        units.add(unit);
        for (BaseSingle troop : unit.getTroops()) {
            activeTroops.add(troop);
        }
    }

    /**
     * Return the list of potential collision candidates of a BaseSingle obj
     */
    public ArrayList<BaseSingle> getCollisionObjects(BaseSingle obj) {
        int xHash = (int)obj.getX() / xDiv;
        int yHash = (int)obj.getY() / yDiv;

        ArrayList<BaseSingle> collideList = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                long key = pairHash(xHash + i, yHash + j);
                if (!hashMap.containsKey(key)) continue;
                for (BaseSingle otherObj : hashMap.get(key)) {
                    if (otherObj != obj) collideList.add(otherObj);
                }
            }
        }
        return collideList;
    }

    /**
     * Return the list of potential collision candidates based on x, y positions
     */
    public ArrayList<BaseSingle> getCollisionObjects(double x, double y) {
        int xHash = (int)x / xDiv;
        int yHash = (int)y / yDiv;

        ArrayList<BaseSingle> collideList = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                long key = pairHash(xHash + i, yHash + j);
                if (!hashMap.containsKey(key)) continue;
                for (BaseSingle otherObj : hashMap.get(key)) {
                    collideList.add(otherObj);
                }
            }
        }
        return collideList;
    }

    /**
     * Hash all activeTroops into the internal hashMap of the space hasher. Each object will be appended to an array list at
     * a certain key based on its position.
     */
    public void hashObjects() {

        // First, filter out inactive units. We shall only hash objects that belong to an active unit.
        filterActiveUnits();

        // Clear the hashmap
        hashMap.clear();

        // Add activeTroops into hashmap with key according to their position
        ArrayList<BaseSingle> newTroops = new ArrayList<>();
        for (BaseSingle troop : activeTroops) {

            // Ignore dead soldiers, they have already fallen
            if (troop.getState() == SingleState.DEAD) continue;
            else newTroops.add(troop);

            // Don't hash non-active troops/
            // Experiment might allow the hashing of cavalry
            if (!activeUnits.contains(troop.getUnit()) &&
                    !(experimentSettings.isCavalryCollision() && (troop instanceof CavalrySingle))) continue;

            // Assign alive activeTroops to correct position
            int xHash = (int)troop.getX() / xDiv;
            int yHash = (int)troop.getY() / yDiv;
            long key = pairHash(xHash, yHash);
            if (!hashMap.containsKey(key)) {
                hashMap.put(key, new ArrayList<>());
            }
            hashMap.get(key).add(troop);
        }
        activeTroops = newTroops;
    }

    /**
     * Filter out only active units to hash. We shall consider a unit to be active if it fills out the following 3
     * criteria.
     * - Being under fire.
     * - Being in combat. Might be redundant since it should be convered by the bounding box.
     * - Being close to another enemy unit (according to bounding box)
     * - (Optional) Being close to a unit under fire.
     * - (Optional) Being close to another active units
     */
    private void filterActiveUnits() {

        // If a unit no longer has any troop, remove them from consideration.
        ArrayList<BaseUnit> remainingUnits = new ArrayList<>();

        activeUnits.clear();
        for (BaseUnit unit : units) {
            // Filter out dead unit
            if (unit.getNumAlives() == 0) continue;
            remainingUnits.add(unit);

            // Being under fire
            if (unit instanceof ArcherUnit && ((ArcherUnit) unit).getUnitFiredAgainst() != null) {
                activeUnits.add(((ArcherUnit) unit).getUnitFiredAgainst());
            }

            // Being in combat
            if (unit.isInContactWithEnemy() || unit.getUnitFoughtAgainst() != null) {
                activeUnits.add(unit);
            }
        }

        // Collide with an enemy bounding box.
        // Stick with O(n ^ 2) check for now, but should change it to an O(n log n) check later.
        for (BaseUnit unit1 : units) {
            for (BaseUnit unit2 : units) {
                // A unit can't collide with itself
                if (unit1 == unit2) continue;

                // A unit can't collide with their own faction if not in combat
                if (unit1.getPoliticalFaction() == unit2.getPoliticalFaction()) continue;

                // Check collision
                if (unitRotatedBoundingBoxCollide(unit1, unit2)) {
                    activeUnits.add(unit1);
                    activeUnits.add(unit2);
                }
            }
        }
        units = remainingUnits;
    }

    /**
     * Check if the two rotated bounding box collide
     * We will use rotated bounding box collision since it is a little more accurate
     * Reimplemented based on the code here.
     * https://stackoverflow.com/questions/10962379/how-to-check-intersection-between-2-rotated-rectangles
     */
    private boolean unitRotatedBoundingBoxCollide(BaseUnit unit1, BaseUnit unit2) {
        double[][] box1 = unit1.getBoundingBox();
        double[][] box2 = unit2.getBoundingBox();

        return PhysicUtils.rotatedBoundingBoxCollide(box1, box2);
    }

    /**
     * Check if the two units collide based on axis-aligned bounding box.
     */
    private boolean unitBoundingBoxCollided(BaseUnit unit1, BaseUnit unit2) {
        double[][] box1 = unit1.getBoundingBox();
        double[][] box2 = unit2.getBoundingBox();

        return PhysicUtils.axisAlignedBoundingBoxCollide(box1, box2);
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hashkey is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long)xHash << 32) | (yHash & 0XFFFFFFFFL);
    }

    /**
     * Getter and setters
     */
    public ArrayList<BaseSingle> getActiveTroops() {
        return activeTroops;
    }

    public HashSet<BaseUnit> getActiveUnits() {
        return activeUnits;
    }
}

package model.algorithms;

import model.constants.ObjectConstants;
import model.constants.UniversalConstants;
import javafx.util.Pair;
import model.enums.SingleState;
import model.objects.Arrow;
import model.objects.BaseObject;
import model.settings.GameSettings;
import model.singles.*;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.enums.UnitState;
import model.utils.MathUtils;
import model.utils.SingleUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public class UnitModifier {

    private ObjectHasher objectHasher;
    private TroopHasher troopHasher;
    private ArrayList<BaseSingle> deadContainer;
    private HashSet<BaseUnit> unitToBeRemoved;
    ArrayList<BaseUnit> unitList;
    GameSettings gameSettings;
    Terrain terrain;

    // Memoization of distance between model.units.
    private double[][] distanceMemo;

    public UnitModifier(ArrayList<BaseSingle> inputDeadContainer, Terrain inputTerrain, GameSettings inputSettings) {
        objectHasher = new ObjectHasher(UniversalConstants.X_HASH_DIV, UniversalConstants.Y_HASH_DIV);
        troopHasher = new TroopHasher(UniversalConstants.X_HASH_DIV, UniversalConstants.Y_HASH_DIV, inputSettings);
        deadContainer = inputDeadContainer;
        unitToBeRemoved = new HashSet<>();
        unitList = new ArrayList<>();
        gameSettings = inputSettings;
        terrain = inputTerrain;

        // Initialize distance memo
        distanceMemo = new double[8][8];
    }

    /**
     * Add a new unit to unit modifier
     */
    public void addUnit(BaseUnit unit) {
        unitList.add(unit);
        troopHasher.addUnit(unit);
    }

    /**
     * This methods will modify all objects in troopHasher based on their positions and political factions.
     * There are two helper modifiers.
     * - Collision modifier: Handle all modification related to troops position.
     * - Fighting modifier: Handle all modification related to troops fighting each other.
     * - Unit state modifier: If two unit touch each other for a while, set both
     */
    public void modifyObjects() {
        // First, update the hash based on current positions
        troopHasher.hashObjects();
        objectHasher.hashObjects();

        // Then apply modifiers
        modifyTroopSpeedByTerrain();
        modifyObjectsCollision();
        modifyTroopsCollision();
        modifyCombat();
        modifyUnitState();
    }

    /**
     * Modify the speed of the troop based on the slope of the terrain.
     */
    private void modifyTroopSpeedByTerrain() {
        for (BaseUnit unit : troopHasher.getActiveUnits()) {
            for (BaseSingle single : unit.getAliveTroopsSet()) {

            }
        }
    }

    /**
     * Modify collisions between objects and troops (arrow, spear tip)
     */
    private void modifyObjectsCollision() {

        // Check collision of every object in the collision modifier and then modify information about such object
        ArrayList<BaseObject> objects = objectHasher.getObjects();
        for (BaseObject obj : objects) {

            // Check if the object makes any potential impact.
            if (!obj.isImpactful()) continue;

            // Get the collision candidate
            // TODO(sonpham): Potentially do this with only objects inside the hashed area.
            // This way is potentially inaccurate but can save some resource.
            ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(obj.getX(), obj.getY());

            // Modify velocity information based on collision with each candidate
            BaseSingle closestCandidate = null;
            double closestDistance = Double.MAX_VALUE;
            for (BaseSingle candidate : candidates) {
                double dx = candidate.getX() - obj.getX();
                double dy = candidate.getY() - obj.getY();
                double squareDistance = dx * dx + dy * dy;
               if (squareDistance < closestDistance) {
                   closestDistance = squareDistance;
                   closestCandidate = candidate;
               }
            }

            // Process based on the type of objects
            if (closestCandidate == null) continue;
            if (obj instanceof Arrow) {
                if (closestDistance < MathUtils.square(closestCandidate.getSize()) / 4) {
                    // If distance to object is smaller than the diameter, count as an arrow hit
                    // Inflict some damage to the candidate
                    closestCandidate.receiveDamage(((Arrow) obj).getDamage());
                    // Once hit, the arrow becomes dead
                    obj.setAlive(false);
                    if (closestCandidate.getState() != SingleState.DEAD) {
                        // Cause extra delay if unit still alive
                        // TODO: Internalize this to the soldier, count it as "injury delay"
                        // closestCandidate.setCombatDelay(closestCandidate.getCombatDelay() + 5);

                        // Apply arrow force
                        double angle = obj.getAngle();
                        double dx = MathUtils.quickCos((float) angle) * ObjectConstants.ARROW_PUSH_DIST;
                        double dy = MathUtils.quickSin((float) angle) * ObjectConstants.ARROW_PUSH_DIST;
                        closestCandidate.setxVel(closestCandidate.getxVel() + dx);
                        closestCandidate.setyVel(closestCandidate.getyVel() + dy);

                        // The soldier will absorb the arrow and carry it for a while
                        closestCandidate.absorbObject(obj);
                    } else {
                        // Cause the unit to perform "deadMorph", which rearrange troops to match the frontline.
                        deadContainer.add(closestCandidate);
                        closestCandidate.getUnit().deadMorph(closestCandidate);
                    }
                }
            }
        }
    }

    /**
     * Modify unit position based on collision
     */
    private void modifyTroopsCollision() {

        if (gameSettings.isBorderInwardCollision()) {
            // Check collision of every active unit from outside border and then inward.
            for (BaseUnit unit : troopHasher.getActiveUnits()) {
                Pair<Queue<BaseSingle>, boolean[][]> borderTroopsAndMap = unit.getBorderTroops();
                Queue<BaseSingle> troopsQueue = borderTroopsAndMap.getKey();
                boolean[][] map = borderTroopsAndMap.getValue();

                while (!troopsQueue.isEmpty()) {

                    // Get a troops
                    BaseSingle troop = troopsQueue.poll();
                    long numCollisions = modifySingleTroopPosition(troop);
                    int numAllyCollisions = MathUtils.getFirst(numCollisions);
                    int numEnemyCollisions = MathUtils.getSecond(numCollisions);

                    // If collide, all surrounding troops will also be in danger of colliding and is added to the collision
                    // queue.
                    if (numEnemyCollisions > 0 || numAllyCollisions > 5) {
                        int row = troop.getSingleIndex() / unit.getWidth();
                        int col = troop.getSingleIndex() % unit.getWidth();
                        int rowCandidate;
                        int colCandidate;
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                rowCandidate = row + i;
                                colCandidate = col + j;
                                if (rowCandidate >= 0 && rowCandidate < map.length &&
                                    colCandidate >= 0 && colCandidate < map[0].length &&
                                    map[rowCandidate][colCandidate] == false) {
                                    map[rowCandidate][colCandidate] = true;
                                    troopsQueue.add(unit.getTroopsAtRowCol(rowCandidate, colCandidate));
                                }
                            }
                        }
                    }
                }

                unit.setInDanger(map);
            }
        } else {
            // Check collision of every object in the collision modifier and then modify information about such object
            ArrayList<BaseSingle> troops = troopHasher.getActiveTroops();
            for (BaseSingle troop : troops) {
                modifySingleTroopPosition(troop);
            }
        }
    }

    /**
     * Modify position of the troop due to collision with enemy and allies
     * @return the number of collisions processed.
     */
    private long modifySingleTroopPosition(BaseSingle troop) {

        // Boolean value as to whether the troop collide at all
        int numAllyCollisions = 0;
        int numEnemyCollisions = 0;

        // Get the collision candidate
        ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(troop);

        // Modify velocity information based on collision with each candidate
        double vxNew = troop.getxVel();
        double vyNew = troop.getyVel();
        double vxAlly = vxNew;
        double vyAlly = vyNew;
        double vxEnemy = vxNew;
        double vyEnemy = vyNew;
        for (BaseSingle candidate : candidates) {
            if (candidate.getPoliticalFaction() == troop.getPoliticalFaction() &&
                    !troop.getUnit().isInContactWithEnemy() &&
                    !(gameSettings.isCavalryCollision() && (candidate instanceof CavalrySingle))) continue;
            double dx = candidate.getX() - troop.getX();
            double dy = candidate.getY() - troop.getY();
            double squareDistance = dx*dx + dy*dy;
            double minDist = candidate.getCollisionRadius() + troop.getCollisionRadius();
            double minSquareDist = MathUtils.square(minDist);

            // Calculate the deflection based on minimum distance.
            if (squareDistance < minSquareDist) {

                // If distance < minDist, they are collided
                double angle = MathUtils.atan2(dy, dx);
                double targetX = troop.getX() + MathUtils.quickCos((float) angle) * minDist;
                double targetY = troop.getY() + MathUtils.quickSin((float) angle) * minDist;
                double ax;
                double ay;
                if (troop.getPoliticalFaction() == candidate.getPoliticalFaction()) {
                    numAllyCollisions += 1;
                    ax = (targetX - candidate.getX()) * UniversalConstants.PUSH_SPRING_ALLY;
                    ay = (targetY - candidate.getY()) * UniversalConstants.PUSH_SPRING_ALLY;
                } else {
                    // Collision is only directly danger if it is against the enemy
                    numEnemyCollisions += 1;
                    ax = (targetX - candidate.getX()) * UniversalConstants.PUSH_SPRING_ENEMY;
                    ay = (targetY - candidate.getY()) * UniversalConstants.PUSH_SPRING_ENEMY;
                }
                if (candidate instanceof CavalrySingle && !(troop instanceof  CavalrySingle))  {
                    ax *= 5;
                    ay *= 5;
                } else if (!(candidate instanceof CavalrySingle) && troop instanceof  CavalrySingle) {
                    ax /= 5;
                    ay /= 5;
                }
                vxNew -= ax;
                vyNew -= ay;
                if (troop.getPoliticalFaction() == candidate.getPoliticalFaction()) {
                    vxAlly -= ax;
                    vyAlly -= ay;
                }
                if (troop.getPoliticalFaction() != candidate.getPoliticalFaction()) {
                    vxEnemy -= ax;
                    vyEnemy -= ay;
                }
            }
        }
        // Set the new speed
        troop.setxVel(vxNew);
        troop.setyVel(vyNew);
        double pushedSpeed = MathUtils.quickRoot2((float)(vxNew * vxNew + vyNew * vyNew));
        if (pushedSpeed < troop.getSpeed()) {
            if (troop instanceof CavalrySingle) {
                troop.setSpeed(pushedSpeed + 0.1 * (troop.getSpeed() - pushedSpeed));
            } else {
                troop.setSpeed(pushedSpeed + 0.2 * (troop.getSpeed() - pushedSpeed));
            }
        }

        return MathUtils.pairInt(numAllyCollisions, numEnemyCollisions);
    }

    /**
     * Modify the state of the unit. Specifically, decrease the fight delay
     */
    private void modifyUnitState() {
        // Dictionary of each unit and whether they touched the enemy.
        HashMap<BaseUnit, BaseUnit> unitTouchEnemy = new HashMap<>();

        // Check each troop in the unit
        ArrayList<BaseSingle> objects = troopHasher.getActiveTroops();
        for (BaseSingle obj : objects) {
            ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(obj);
            for (BaseSingle candidate : candidates) {
                // No need to check if allies
                if (candidate.getPoliticalFaction() == obj.getPoliticalFaction()) continue;
                // No need to check if both already loses patience
                if (unitTouchEnemy.containsKey(candidate.getUnit()) && unitTouchEnemy.containsKey(obj.getUnit())) continue;
                // Check if opposing troop in attack range.
                double squareDist = SingleUtils.squareDistance(obj, candidate);
                double squareCombatRange = MathUtils.square(obj.getSize() / 2 + obj.getCombatRangeStat());
                if (squareDist < squareCombatRange) {
                    // In combat range, decrease patience from both sides
                    unitTouchEnemy.put(obj.getUnit(), candidate.getUnit());
                    unitTouchEnemy.put(candidate.getUnit(), obj.getUnit());
                }
            }
        }

        // Clear remove list and reset unit contact
        unitToBeRemoved.clear();
        for (BaseUnit unit : unitList) unit.setInContactWithEnemy(false);

        // Set any unit touching the enemy as in contact and decrease the patience of that unit.
        // Decrease the patience of any unit that already touches enemy
        for (BaseUnit unit : unitTouchEnemy.keySet()) {
            unit.setInContactWithEnemy(true);
            unit.setCurrUnitPatience(unit.getCurrUnitPatience() - 1);
            if (unit.getCurrUnitPatience() == 0 && unit.getState() != UnitState.FIGHTING) {
                unit.setState(UnitState.FIGHTING);
                unit.setUnitFoughtAgainst(unitTouchEnemy.get(unit));
                unitToBeRemoved.add(unit);
            }
        }

        // Remove already fighting unit from patience calculation
        for (BaseUnit unit : unitToBeRemoved) {
            unitTouchEnemy.remove(unit);
        }
    }

    /**
     * Modify unit stats based on combat
     */
    private void modifyCombat() {
        // Check collision of every object in the collision modifier and then modify information about such object
        ArrayList<BaseSingle> objects = troopHasher.getActiveTroops();

        // Shuffle to randomly settle tie between two model.units about to hit at the same time.
        // Collections.shuffle(objects);
        for (BaseSingle obj : objects) {

            // Ignore dead objects. They can't deal damage.
            if (obj.getState() == SingleState.DEAD) continue;

            // Single must recharge attack delay
            if (obj.getCombatDelay() > 0) {
                obj.setCombatDelay(obj.getCombatDelay() - 1);
                continue;
            }

            // If the single is ready to attack, then it will hit the closest candidate, and cause an extra delay to the
            // attacked candidate.
            ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(obj);
            double minSquareDist = MathUtils.MAX_DOUBLE;
            BaseSingle attackCandidate = null;
            for (BaseSingle candidate : candidates) {
                if (candidate.getPoliticalFaction() == obj.getPoliticalFaction()) continue;
                if (candidate.getState() == SingleState.DEAD) continue;
                double squareDist = SingleUtils.squareDistance(obj, candidate);
                double squareCombatRange = MathUtils.square(obj.getSize() / 2 + obj.getCombatRangeStat());
                if (squareDist < minSquareDist) {
                    minSquareDist = squareDist;
                    if (minSquareDist < squareCombatRange) {
                        attackCandidate = candidate;
                    }
                }
            }

            // If there is an attack candidate in range, inflict damage.
            if (attackCandidate != null) {

                // Inflict some damage and reset combat delay
                // TODO: Get this into the Base Single action would be nicer.
                attackCandidate.receiveDamage(obj.getAttackStat());
                obj.setCombatDelay(obj.getCombatDelayStat());

                if (attackCandidate.getState() != SingleState.DEAD) {
                    // Cause extra delay if unit still alive
                    // attackCandidate.setCombatDelay(attackCandidate.getCombatDelay() + 5);
                } else {
                    // Cause the unit to perform "deadMorph", which is to rearange troops to match the frontline.
                    deadContainer.add(attackCandidate);
                    attackCandidate.getUnit().deadMorph(attackCandidate);
                }
            }
        }
    }

    /**
     * Getter and setters
     */
    public TroopHasher getTroopHasher() {
        return troopHasher;
    }

    public ObjectHasher getObjectHasher() {
        return objectHasher;
    }
}

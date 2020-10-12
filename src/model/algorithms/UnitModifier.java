package model.algorithms;

import model.constants.GameplayConstants;
import model.constants.UniversalConstants;
import javafx.util.Pair;
import model.construct.Construct;
import model.enums.SingleState;
import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventType;
import model.monitor.Monitor;
import model.monitor.MonitorEnum;
import model.projectile_objects.*;
import model.settings.GameSettings;
import model.singles.*;
import model.surface.BaseSurface;
import model.map_objects.Tree;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.enums.UnitState;
import model.units.CavalryUnit;
import model.utils.MathUtils;
import model.utils.PhysicUtils;
import model.utils.SingleUtils;

import java.util.*;

public class UnitModifier {

    private ProjectileHasher projectileHasher;
    private HitscanHasher hitscanHasher;
    private TroopHasher troopHasher;
    private ConstructHasher constructHasher;
    private SurfaceHasher surfaceHasher;
    private ArrayList<BaseSingle> deadContainer;
    private HashSet<BaseUnit> unitToBeRemoved;
    private EventBroadcaster broadcaster;
    private Monitor monitor;

    private ArrayList<BaseUnit> unitList;
    private GameSettings gameSettings;
    private Terrain terrain;
    private ArrayList<Construct> constructs;
    private ArrayList<BaseSurface> surfaces;
    private HashMap<BaseUnit, Integer> recentlyChargedUnit;

    public UnitModifier(ArrayList<BaseSingle> inputDeadContainer, Terrain inputTerrain,
                        ArrayList<Construct> inputConstructs, ArrayList<BaseSurface> inputSurfaces,
                        GameSettings inputSettings, EventBroadcaster inputBroadcaster, Monitor inputMonitor) {
        broadcaster = inputBroadcaster;
        monitor = inputMonitor;
        projectileHasher = new ProjectileHasher();
        hitscanHasher = new HitscanHasher();
        troopHasher = new TroopHasher(UniversalConstants.X_HASH_DIV, UniversalConstants.Y_HASH_DIV, inputSettings);
        deadContainer = inputDeadContainer;
        unitToBeRemoved = new HashSet<>();
        unitList = new ArrayList<>();
        gameSettings = inputSettings;
        terrain = inputTerrain;
        constructs = inputConstructs;
        surfaces = inputSurfaces;
        constructHasher = new ConstructHasher(UniversalConstants.X_HASH_DIV, UniversalConstants.Y_HASH_DIV, constructs);
        surfaceHasher = new SurfaceHasher(UniversalConstants.X_HASH_DIV_SURFACE_TREES, UniversalConstants.Y_HASH_DIV_SURFACE_TREES, surfaces);

        // Initialize charged unit map.
        recentlyChargedUnit = new HashMap<>();
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
        projectileHasher.updateObjects();
        hitscanHasher.updateObjects();

        // Then apply modifiers
        modifyProjectilesCollision();
        modifyHitscanCollision();
        modifyTroopsCollisionWithSurfaces();
        modifyTroopsCollision();
        modifyCombat();
        modifyUnitState();
        modifyTroopsTreesCollision();
        modifyTroopsConstructsCollision();
    }

    /**
     * Modify collisions between troops and constructs.
     */
    private void modifyTroopsConstructsCollision() {
        for (BaseSingle single : troopHasher.getActiveTroops()) {
            ArrayList<Construct> candidateConstructs =
                    constructHasher.getCandidateConstructs(single.getX(), single.getY());
            for (Construct construct : candidateConstructs) {
                if (PhysicUtils.checkConstructAndTroopPositionCollision(construct, single)) {
                    PhysicUtils.constructPushSingle(construct, single);
                }
                monitor.count(MonitorEnum.COLLISION_TROOP_AND_CONSTRUCT);
            }
        }
    }

    /**
     * Modify collisions between troops and trees.
     */
    private void modifyTroopsTreesCollision() {
        for (BaseSingle single : troopHasher.getActiveTroops()) {
            ArrayList<Tree> trees =
                    surfaceHasher.getCandidateTrees(single.getX(), single.getY());
            if (trees == null) continue;
            for (Tree tree : trees) {
                if (PhysicUtils.checkPointCircleCollision(single.getX(), single.getY(), tree.getX(), tree.getY(), tree.getRadius())) {
                    PhysicUtils.treePushSingle(tree, single);
                }
                monitor.count(MonitorEnum.COLLISION_TROOP_AND_TREE);
            }
        }
    }

    /**
     * Modify collisions between objects and troops (arrow, spear tip)
     */
    private void modifyTroopsCollisionWithSurfaces() {
        for (BaseSingle single : troopHasher.getActiveTroops()) {
            ArrayList<BaseSurface> candidateSurfaces =
                    surfaceHasher.getCandidateSurfaces(single.getX(), single.getY());
            for (BaseSurface surface : candidateSurfaces) {
                if (gameSettings.isUseRoundedSurfaceCollision() ||
                        PhysicUtils.checkSurfaceAndTroopPositionCollision(surface, single)) {
                    surface.impactSingle(single);
                }
                monitor.count(MonitorEnum.COLLISION_TROOP_AND_TERRAIN);
            }
        }
    }

    /**
     * Modify collisions
     */
    private void modifyHitscanCollision() {
        // Go through each hitscan objects, and perform a hitscan collision, which is basically a line circle collision.
        ArrayList<HitscanObject> objects = hitscanHasher.getObjects();
        for (HitscanObject o : objects) {
            double unitX = MathUtils.quickCos((float) o.getTheta());
            double unitY = MathUtils.quickSin((float) o.getTheta());
            double x1 = o.getStartX() + unitX * o.getMinRange();
            double y1 = o.getStartY() + unitY * o.getMinRange();
            double x2 = o.getStartX() + unitX * o.getMaxRange();
            double y2 = o.getStartY() + unitY * o.getMaxRange();
            for (BaseSingle single : troopHasher.getCollisionObjectsFromLine(x1, y1, x2, y2)) {
                if (PhysicUtils.checkLineCircleCollision(x1, y1, x2, y2, single.getX(), single.getY(), single.getRadius())) {
                    single.receiveDamage(o.getDamage());
                    break;  // Break the loop because once the bullet hits, it is no long effective.
                    // TODO: Consider adding a Hitscan that allow "shoot through mechanics".
                }
            }
        }
    }

    /**
     * Modify collisions between objects and troops (arrow, spear tip)
     */
    private void modifyProjectilesCollision() {

        // Check collision of every object in the collision modifier and then modify information about such object
        ArrayList<BaseProjectile> objects = projectileHasher.getObjects();
        for (BaseProjectile obj : objects) {

            // Check if the object makes any potential impact.
            if (!obj.isImpactful()) continue;

            // Get the collision candidate
            // This way is potentially inaccurate but can save some resource.
            ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(obj.getX(), obj.getY());

            // Modify velocity information based on collision with each candidate
            // TODO(sonpham): Now that point-vs-circle collision is available. Modify the collision code to use that
            //  method instead.
            BaseSingle closestCandidate = null;
            double closestDistance = Double.MAX_VALUE;
            for (BaseSingle candidate : candidates) {
                monitor.count(MonitorEnum.COLLISION_OBJECT);
                double dx = candidate.getX() - obj.getX();
                double dy = candidate.getY() - obj.getY();
                double squareDistance = dx * dx + dy * dy;
                if (squareDistance < closestDistance) {
                    closestDistance = squareDistance;
                    closestCandidate = candidate;
                }
            }

            // Process based on the type of objects
            if (obj instanceof Arrow) {
                if (closestCandidate == null) continue;
                if (closestDistance < MathUtils.square(closestCandidate.getRadius()) / 4) {
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
                        double dx = MathUtils.quickCos((float) angle) * ((Arrow) obj).getPushDist();
                        double dy = MathUtils.quickSin((float) angle) * ((Arrow) obj).getPushDist();
                        closestCandidate.setxVel(closestCandidate.getxVel() + dx);
                        closestCandidate.setyVel(closestCandidate.getyVel() + dy);

                        // The soldier will absorb the arrow and carry it for a while
                        closestCandidate.absorbObject(obj);
                    } else {
                        // Cause the unit to perform "deadMorph", which rearrange troops to match the frontline.
                        deadContainer.add(closestCandidate);
                    }
                }
            } else if (obj instanceof Ballista) {
                if (closestCandidate == null) continue;
                boolean balistaHit = closestDistance < MathUtils.square(closestCandidate.getRadius()) / 4;

                if (balistaHit) {
                    broadcaster.broadcastEvent(new Event(
                            EventType.BALLISTA_HIT_FLESH, obj.getX(), obj.getY(),
                            terrain.getZFromPos(obj.getX(), obj.getY())));

                    // Inflict explosion damage damage
                    ArrayList<BaseSingle> explosionCandidates = troopHasher.getCollisionObjects(
                            obj.getX(), obj.getY(),
                            ((Ballista) obj).getExplosionRange());
                    double squareExplosionRange =
                            ((Ballista) obj).getExplosionRange() * ((Ballista) obj).getExplosionRange();
                    for (BaseSingle candidate : explosionCandidates) {
                        double dx = candidate.getX() - obj.getX();
                        double dy = candidate.getY() - obj.getY();
                        double squareDistance = dx * dx + dy * dy;
                        if (squareDistance < squareExplosionRange) {
                            // Apply damage and send the objects flying
                            double angle = MathUtils.atan2(dy, dx);
                            candidate.setxVel(candidate.getxVel() +
                                    MathUtils.quickCos((float) angle) * ((Ballista) obj).getExplosionPush());
                            candidate.setyVel(candidate.getyVel() +
                                    MathUtils.quickSin((float) angle) * ((Ballista) obj).getExplosionPush());
                            candidate.switchState(SingleState.SLIDING);

                            candidate.receiveDamage(((Ballista) obj).getExplosionDamage());
                            if (candidate.getState() == SingleState.DEAD) {
                                deadContainer.add(candidate);
                            }
                        }
                    }

                    // If distance to object is smaller than the diameter, count as an arrow hit
                    // Inflict some damage to the candidate
                    closestCandidate.receiveDamage(((Ballista) obj).getDamage());

                    // Once hit, the arrow becomes dead
                    obj.setAlive(false);
                    if (closestCandidate.getState() != SingleState.DEAD) {
                        // Apply arrow force
                        double angle = obj.getAngle();
                        double dx = MathUtils.quickCos((float) angle) * ((Ballista) obj).getPushForce();
                        double dy = MathUtils.quickSin((float) angle) * ((Ballista) obj).getPushForce();
                        closestCandidate.setxVel(closestCandidate.getxVel() + dx);
                        closestCandidate.setyVel(closestCandidate.getyVel() + dy);

                        // The soldier will absorb the arrow and carry it for a while
                        closestCandidate.absorbObject(obj);
                    } else {
                        // Cause the unit to perform "deadMorph", which rearrange troops to match the frontline.
                        deadContainer.add(closestCandidate);
                    }
                } else if (((Ballista) obj).isTouchGround()) {
                    broadcaster.broadcastEvent(new Event(
                            EventType.BALLISTA_HIT_GROUND, obj.getX(), obj.getY(),
                            terrain.getZFromPos(obj.getX(), obj.getY())));
                }
            } else if (obj instanceof Stone) {
                if (((Stone) obj).isTouchGround()) {
                    // Inflict explosion damage damage
                    broadcaster.broadcastEvent(new Event(
                            EventType.EXPLOSION, obj.getX(), obj.getY(),
                            terrain.getZFromPos(obj.getX(), obj.getY())));
                    ArrayList<BaseSingle> explosionCandidates = troopHasher.getCollisionObjects(
                            obj.getX(), obj.getY(),
                            ((Stone) obj).getExplosionRange());
                    double squareExplosionRange =
                            ((Stone) obj).getExplosionRange() * ((Stone) obj).getExplosionRange();
                    for (BaseSingle candidate : explosionCandidates) {
                        double dx = candidate.getX() - obj.getX();
                        double dy = candidate.getY() - obj.getY();
                        double squareDistance = dx * dx + dy * dy;
                        if (squareDistance < squareExplosionRange) {
                            // Apply damage and send the objects flying
                            double angle = MathUtils.atan2(dy, dx);
                            candidate.setxVel(candidate.getxVel() +
                                    MathUtils.quickCos((float) angle) * ((Stone) obj).getExplosionPush());
                            candidate.setyVel(candidate.getyVel() +
                                    MathUtils.quickSin((float) angle) * ((Stone) obj).getExplosionPush());
                            candidate.switchState(SingleState.SLIDING);

                            candidate.receiveDamage(((Stone) obj).getExplosionDamage());
                            if (candidate.getState() == SingleState.DEAD) {
                                // Cause the unit to perform "deadMorph", which rearrange troops to match the frontline.
                                deadContainer.add(candidate);
                            }
                        }
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
                        int row = unit.getTroopIndex(troop) / unit.getWidth();
                        int col = unit.getTroopIndex(troop) % unit.getWidth();
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
            if ((!gameSettings.isAllyCollision() && candidate.getPoliticalFaction() == troop.getPoliticalFaction()) &&
                    !troop.getUnit().isInContactWithEnemy() &&
                    !(gameSettings.isCavalryCollision() && (candidate instanceof CavalrySingle))) continue;
            double dx = candidate.getX() - troop.getX();
            double dy = candidate.getY() - troop.getY();
            double squareDistance = dx*dx + dy*dy;
            double minDist = candidate.getCollisionRadius() + troop.getCollisionRadius();
            if (candidate.getPoliticalFaction() == troop.getPoliticalFaction()) {
                switch (candidate.getUnit().getState()) {
                    case FIGHTING:
                        minDist *= GameplayConstants.ALLY_COLLISION_RATIO_FIGHTING;
                        break;
                    case MOVING:
                        minDist *= GameplayConstants.ALLY_COLLISION_RATIO_MOVING;
                        break;
                    case STANDING:
                        minDist *= GameplayConstants.ALLY_COLLISION_RATIO_STANDING;
                        break;
                }
            }
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
                    numEnemyCollisions += 1;
                    ax = (targetX - candidate.getX()) * UniversalConstants.PUSH_SPRING_ENEMY;
                    ay = (targetY - candidate.getY()) * UniversalConstants.PUSH_SPRING_ENEMY;
                }
                ax *= candidate.getSingleStats().mass / troop.getSingleStats().mass;
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
            monitor.count(MonitorEnum.COLLISION_TROOPS);
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
     * Modify the state of each unit.
     */
    private void modifyUnitState() {

        // Update the charge buffer maps
        ArrayList<BaseUnit> unitsToBeMovedFromBuffer = new ArrayList<>();
        for (BaseUnit unit : recentlyChargedUnit.keySet()) {
            recentlyChargedUnit.put(unit, recentlyChargedUnit.get(unit) - 1);
            if (recentlyChargedUnit.get(unit) == 0) {
                unitsToBeMovedFromBuffer.add(unit);
            }
        }
        for (BaseUnit unit : unitsToBeMovedFromBuffer) {
            if (recentlyChargedUnit.get(unit) == 0) {
                recentlyChargedUnit.remove(unit);
            }
        }

        // Check the vision of each unit.
        for (BaseUnit unit : unitList) {
            if (gameSettings.isProcessUnitVision()) {
                unit.setVisibleUnits(PhysicUtils.checkUnitVision(unit, unitList, terrain));
            } else {
                unit.setVisibleUnits(unitList);
            }
        }

        // Dictionary of each unit and whether they touched the enemy.
        HashMap<BaseUnit, BaseUnit> unitTouchEnemy = new HashMap<>();

        // Check each troop in the unit
        ArrayList<BaseSingle> objects = troopHasher.getActiveTroops();
        for (BaseSingle obj : objects) {
            ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(obj);
            for (BaseSingle candidate : candidates) {
                // Skip collision check for ally if game setting allows.
                if (candidate.getPoliticalFaction() == obj.getPoliticalFaction()) continue;
                // No need to check if both already loses patience
                if (unitTouchEnemy.containsKey(candidate.getUnit()) && unitTouchEnemy.containsKey(obj.getUnit())) continue;
                // Check if opposing troop in attack range.
                double squareDist = SingleUtils.squareDistBetweenSingles(obj, candidate);
                double squareCombatRange = MathUtils.square(obj.getRadius() / 2 + obj.getCombatRangeStat());
                if (squareDist < squareCombatRange) {
                    // In combat range, decrease patience from both sides
                    unitTouchEnemy.put(obj.getUnit(), candidate.getUnit());
                    unitTouchEnemy.put(candidate.getUnit(), obj.getUnit());
                }
            }
        }

        // Clear remove list and reset unit contact
        unitToBeRemoved.clear();
        HashSet<BaseUnit> previouslyEngagedUnits = new HashSet<>();
        for (BaseUnit unit : unitList) {
            if (unit.isInContactWithEnemy()) {
                previouslyEngagedUnits.add(unit);
            }
            unit.setInContactWithEnemy(false);
        }

        // Set any unit touching the enemy as in contact and decrease the patience of that unit.
        // Decrease the patience of any unit that already touches enemy
        for (BaseUnit unit : unitTouchEnemy.keySet()) {
            // If the unit is previously now engaged, but is now engaging and neither unit is currently routing,
            // consider it a charge.
            // TODO(sonpham): Needs to be much more careful about what constitute a charge here. Unit colliding is not
            // the only reason to be called a charge.
            if (!previouslyEngagedUnits.contains(unit)) {
                if (recentlyChargedUnit.containsKey(unit) ||
                        unit.getState() == UnitState.ROUTING ||
                        unit.getState() == UnitState.FIGHTING) {
                    // If unit simply touches another unit but doesn't fulfill a condition of being a charge. Simply
                    // reset the charged unit buffer.
                    recentlyChargedUnit.put(unit, GameplayConstants.CHARGE_BUFFER);
                } else {
                    if (unit instanceof CavalryUnit) {
                        broadcaster.broadcastEvent(new Event(
                                EventType.CAVALRY_CHARGE, unit.getAverageX(), unit.getAverageY(), unit.getAverageZ()));
                    } else {
                        broadcaster.broadcastEvent(new Event(
                                EventType.SOLDIER_CHARGE, unit.getAverageX(), unit.getAverageY(), unit.getAverageZ()));
                    }
                    recentlyChargedUnit.put(unit, GameplayConstants.CHARGE_BUFFER);
                }
            }
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
        ArrayList<BaseSingle> singles = troopHasher.getActiveTroops();

        // Shuffle to randomly settle tie between two singles about to hit at the same time.
        Collections.shuffle(singles);
        for (BaseSingle single : singles) {

            // Ignore dead or routing soldiers. They can't deal damage.
            if (single.getState() == SingleState.DEAD || single.getState() == SingleState.ROUTING) continue;


            // Single must recharge attack delay
            if (single.getCombatDelay() > 0) {
                continue;
            }

            // If the single is ready to attack, then it will hit the closest candidate, and cause an extra delay to the
            // attacked candidate.
            ArrayList<BaseSingle> candidates = troopHasher.getCollisionObjects(single);
            double minSquareDist = MathUtils.MAX_DOUBLE;
            BaseSingle attackCandidate = null;
            for (BaseSingle candidate : candidates) {
                if (candidate.getPoliticalFaction() == single.getPoliticalFaction()) continue;
                if (candidate.getState() == SingleState.DEAD) continue;
                double squareDist = SingleUtils.squareDistBetweenSingles(single, candidate);
                double squareCombatRange = MathUtils.square(single.getRadius() / 2 + single.getCombatRangeStat());
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
                single.attack(attackCandidate);
                single.resetCombatDelay();

                if (attackCandidate.getState() == SingleState.DEAD) {
                    // Cause the unit to perform "deadMorph", which is to rearange troops to match the frontline.
                    deadContainer.add(attackCandidate);
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

    public ProjectileHasher getProjectileHasher() {
        return projectileHasher;
    }

    public HitscanHasher getHitscanHasher() {
        return hitscanHasher;
    }
}

package model.singles;

import model.constants.GameplayConstants;
import model.constants.UniversalConstants;
import javafx.util.Pair;
import model.enums.SingleState;
import model.objects.BaseObject;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.enums.PoliticalFaction;
import model.enums.UnitState;
import model.utils.MathUtils;
import model.utils.MovementUtils;
import view.constants.DrawingConstants;

import java.util.HashMap;

public class BaseSingle {

    // Political attribute
    PoliticalFaction politicalFaction;
    BaseUnit unit;  // The unit the individual belong to

    // Single stats
    SingleStats singleStats;

    // Positional attributes
    double x;
    double y;
    double xVel;
    double yVel;
    double speed;
    double angle;
    double facingAngle;
    double height;  // z
    Terrain terrain;

    // Position goal
    double xGoal;
    double yGoal;
    double speedGoal;
    double angleGoal;
    double facingAngleGoal;

    // Distance from goal
    double tempSquaredDistanceFromGoal;

    // Currents state of the single
    private double hp;
    SingleState state;
    int decisionDelay;
    int combatDelay;
    int justHit;
    boolean screamDeath;
    double damageSustain;
    boolean inPosition;
    boolean inDanger;  // The troop is in contact with enemy or is crushed by too many collisions
    HashMap<BaseObject, Integer> carriedObjects;  // Stuff carried with the single (arrows)

    /**
     * Initialize a troop. Since this is the base objects. Attributed assign will be among the most basic shared stats
     * such as:
     * - damageSustain
     * Refactor BaseSingle to include more stuffs.
     */
    public BaseSingle(SingleStats singleStats, BaseUnit baseUnit) {
        unit = baseUnit;
        terrain = unit.getTerrain();

        damageSustain = 0.0;
        justHit = 0;
        screamDeath = false;
        inPosition = true;
        inDanger = false;
        carriedObjects = new HashMap<>();
        hp = singleStats.hp;
    }

    /**
     * Update intention of the troop. The intention represents the potential move of the soldier, if unopposed, through
     * xVel and yVel;
     */
    public void updateIntention() {

        // Can't have intention if already dead
        if (state == SingleState.DEAD) return;

        // Can't change intention if still sliding
        if (state == SingleState.SLIDING) return;

        // Calculate intended speed
        double variation = 1;
        if (speed != 0) variation = MathUtils.randDouble(0.99, 1.01);
        if (speed < speedGoal) speed = Math.min(speed + singleStats.acceleration, speedGoal);
        else if (speed > speedGoal) speed = Math.max(speed - singleStats.deceleration, speedGoal);
        speed *= variation;

        // Apply speed modifier by terrain
        double moveSpeedX = Math.cos(angle) * speed;
        double moveSpeedY = Math.sin(angle) * speed;
        double[] deltaVel = terrain.getDeltaVelFromPos(x, y);
        double speedModifier = MathUtils.ratioProjection(deltaVel[0], deltaVel[1], moveSpeedX, moveSpeedY);
        speedModifier = MathUtils.capMinMax(speedModifier,
                UniversalConstants.MINIMUM_TERRAIN_EFFECT,
                UniversalConstants.MAXIMUM_TERRAIN_EFFECT);;
        speed *= (1 + speedModifier);

        // Update based on states
        double distanceToGoal = MathUtils.quickDistance(x, y, xGoal, yGoal);
        double towardAngle = MathUtils.atan2(yGoal - y, xGoal - x);
        switch (state) {
            case MOVING:
                // Out of position if it takes more than 3 steps to reach
                speedGoal = singleStats.speed;
                if (distanceToGoal > singleStats.standingDist) {
                    if (distanceToGoal > singleStats.outOfReachDist) {
                        switchState(SingleState.CATCHING_UP);
                    }
                    if (!MathUtils.doubleEqual(angle, towardAngle, 1e-1) && distanceToGoal > singleStats.standingDist) {
                        switchState(SingleState.ROTATING);
                    }
                } else if (distanceToGoal < singleStats.standingDist && unit.getState() == UnitState.STANDING) {
                    switchState(SingleState.IN_POSITION);
                }
                break;
            case CATCHING_UP:
                speedGoal = singleStats.outOfReachSpeed;
                if (distanceToGoal > singleStats.standingDist) {
                    if (distanceToGoal < singleStats.outOfReachDist) {
                        switchState(SingleState.MOVING);
                    }
                    if (!MathUtils.doubleEqual(angle, towardAngle, 1e-1) && distanceToGoal > singleStats.standingDist) {
                        switchState(SingleState.ROTATING);
                    }
                } else if (distanceToGoal < singleStats.standingDist && unit.getState() == UnitState.STANDING) {
                    switchState(SingleState.IN_POSITION);
                }
                break;
            case ROTATING:
                angle = MovementUtils.rotate(angle, towardAngle, singleStats.rotationSpeed);
                speedGoal = 0;
                if (distanceToGoal > singleStats.standingDist) {
                    if (MathUtils.doubleEqual(angle, towardAngle, 1e-1)) {
                        switchState(SingleState.MOVING);
                    }
                } else if (distanceToGoal < singleStats.standingDist) {
                    switchState(SingleState.IN_POSITION);
                }
                break;
            case ROUTING:
                // Recalculate vx, vy
                speedGoal = singleStats.speed * GameplayConstants.ROUTING_SPEED_COEFFICIENT;
                angle = MovementUtils.rotate(angle, unit.getGoalAngle(), singleStats.rotationSpeed);
                angle += MathUtils.randDouble(-GameplayConstants.ROUTING_ANGLE_VARIATION, GameplayConstants.ROUTING_ANGLE_VARIATION);
                break;
            case IN_POSITION:
                speed = unit.isTurning() ? 0 : unit.getState() == UnitState.MOVING ? singleStats.speed : 0;
                speedGoal = unit.getState() == UnitState.MOVING ? singleStats.speed : 0;
                if (distanceToGoal > singleStats.standingDist) {
                    switchState(SingleState.MOVING);
                    speedGoal = singleStats.speed;
                }

                // Rotate the actor back to desired angle
                angle = MovementUtils.rotate(angle, angleGoal, singleStats.rotationSpeed);
                break;
            case FIRE_AT_WILL:
                speed = 0;
                if (distanceToGoal > singleStats.standingDist) {
                    switchState(SingleState.MOVING);
                    speedGoal = singleStats.speed;
                }

                // Rotate the actor back to desired angle
                angle = MovementUtils.rotate(angle, angleGoal, singleStats.rotationSpeed);
                break;
        }

        // If not too far from intended position, face the position of the army
        if (distanceToGoal < singleStats.nonRotationDist) {
            facingAngleGoal = angleGoal;
        } else {
            facingAngleGoal = angle;
        }

        // Calculate intended step
        xVel = MathUtils.quickCos((float) angle) * speed;
        yVel = MathUtils.quickSin((float) angle) * speed;
    }

    /**
     * Update the state of the troop
     */
    public void updateState() {
        // Update position and height
        if (state != SingleState.DEAD) {
            x += xVel;
            y += yVel;
            if (state == SingleState.SLIDING) {
                xVel *= UniversalConstants.SLIDING_FRICTION;
                yVel *= UniversalConstants.SLIDING_FRICTION;
                if (MathUtils.quickRoot2((float) (xVel * xVel + yVel * yVel)) < UniversalConstants.STOP_SLIDING_DIST) {
                    if (unit.getState() == UnitState.ROUTING) {
                        switchState(SingleState.ROUTING);
                    } else {
                        switchState(SingleState.MOVING);
                    }
                }
            }
        }
        height = terrain.getHeightFromPos(x, y);
        facingAngle = MovementUtils.rotate(facingAngle, facingAngleGoal, singleStats.rotationSpeed);

        // Update combat statistics
        damageSustain -= DrawingConstants.SUSTAIN_COOLDOWN;
        if (damageSustain < 0) damageSustain = 0;
        justHit -= 1;
        if (justHit < 0) justHit = 0;

        // Update in position
        inPosition = Math.abs(xGoal - x) < singleStats.standingDist &&
                Math.abs(yGoal - y) < singleStats.standingDist;

        // Update carried objects
        Object[] objects = carriedObjects.keySet().toArray();
        for (Object obj : objects) {
            int lifeTime = carriedObjects.get(obj) - 1;
            if (lifeTime > 0) carriedObjects.put((BaseObject) obj, lifeTime);
            else carriedObjects.remove(obj);
        }

        // Update combat delay
        combatDelay -= 1;
    }

    /**
     * Attack other single.
     */
    public void attack(BaseSingle other) {
        // Deal at least base damage
        double damage = singleStats.attack;
        double angleDiff;

        // Bonus damage if attacking the single from above.
        double heightDiff = this.height - other.height;
        double damageScaleByHeightDiff = heightDiff / GameplayConstants.HEIGHT_DIFF_BASE;
        damage *= (1 + MathUtils.capMinMax(damageScaleByHeightDiff,
                -GameplayConstants.HEIGHT_DIFF_MAX_PENALTY_SCALE,
                GameplayConstants.HEIGHT_DIFF_MAX_BONUS_SCALE));

        // Bonus damage if attacking the single from the flank or from behind.
        double angleFromSingle = MathUtils.atan2(y - other.y, x - other.x);
        angleDiff = MathUtils.signedAngleDifference(angleFromSingle, other.angle);
        if (angleDiff < GameplayConstants.FLANKING_ANGLE_SINGLE_THRESHOLD) {
            damage *= GameplayConstants.FLANKING_BONUS_SINGLE_SCALE;
        }

        // Bonus damage if attacking the unit from the flank or from behind, and that the attack single is hitting
        // from the outside.
        double angleFromUnit = MathUtils.atan2(y - other.unit.getAverageY(), x - other.unit.getAverageX());
        angleDiff = MathUtils.signedAngleDifference(angleFromUnit, other.unit.getAnchorAngle());
        double attackFromUnitDist = MathUtils.quickDistance(x, y,
                other.unit.getAverageX(), other.unit.getAverageY());
        double defenderFromUnitDist = MathUtils.quickDistance(other.x, other.y,
                other.unit.getAverageX(), other.unit.getAverageY());
        if (angleDiff < GameplayConstants.FLANKING_ANGLE_UNIT_THRESHOLD && attackFromUnitDist > defenderFromUnitDist) {
            damage *= GameplayConstants.FLANKING_BONUS_UNIT_SCALE;
        }

        // Other will receive the total damage
        other.receiveDamage(damage);
    }

    /**
     * Let the troop receive damage. Troop with health lower than 0 should die.
     * TODO: Add defense mechanism, and customize them according to the angle the attack is received from.
     */
    public void receiveDamage(double damage) {
        damage = Math.max(GameplayConstants.MINIMUM_DAMAGE_RECEIVED, damage - singleStats.defense);
        hp -= damage;
        damageSustain += damage;
        justHit = 2;
        if (hp < 0 && state != SingleState.DEAD) {
            switchState(SingleState.DEAD);
            this.unit.processDeadSingle(this);
        }
    }

    /**
     * Switch state function
     */
    public void switchState(SingleState newState) {
        // Can only switch state if the unit is not DEAD or not UNCONTROLLABLE
        if (state != SingleState.DEAD) {
            state = newState;
            if (state == SingleState.DEAD) screamDeath = true;
        }
    }

    /**
     * Absorb an object (such as an arrow)
     */
    public void absorbObject(BaseObject object) {
        // Recalculate position to be relative to the troop, standing at 0, 0 and angle 0
        double dx = object.getX() - x;
        double dy = object.getY() - y;
        double dAngle = (object.getAngle() - angle) % MathUtils.PIX2;

        // Rotate dx, dy vector to normalize object position to angle 0
        Pair<Double, Double> rotatedVector = MathUtils.rotate(dx, dy, -angle);
        dx = rotatedVector.getKey();
        dy = rotatedVector.getValue();

        // Absorb the object to the carry list
        object.setX(dx);
        object.setY(dy);
        object.setAngle(dAngle);
        carriedObjects.put(object, UniversalConstants.CARRIED_OBJECT_LIFETIME);
    }

    /** Getter and setter */

    public double getSpeedStat() {
        return singleStats.speed;
    }

    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getxVel() {
        return xVel;
    }
    public void setxVel(double xVel) {
        this.xVel = xVel;
    }

    public double getyVel() {
        return yVel;
    }
    public void setyVel(double yVel) {
        this.yVel = yVel;
    }

    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    public double getxGoal() {
        return xGoal;
    }
    public void setxGoal(double xGoal) {
        this.xGoal = xGoal;
    }

    public double getyGoal() {
        return yGoal;
    }
    public void setyGoal(double yGoal) {
        this.yGoal = yGoal;
    }


    public double getSpeedGoal() {
        return speedGoal;
    }
    public void setSpeedGoal(double speedGoal) {
        this.speedGoal = speedGoal;
    }


    public double getTempSquaredDistanceFromGoal() {
        return tempSquaredDistanceFromGoal;
    }
    public void setTempSquaredDistanceFromGoal(double tempSquaredDistanceFromGoal) {
        this.tempSquaredDistanceFromGoal = tempSquaredDistanceFromGoal;
    }

    public SingleState getState() {
        return state;
    }

    public double getSize() {
        return singleStats.radius;
    }

    public double getAngleGoal() {
        return angleGoal;
    }

    public void setAngleGoal(double angleGoal) {
        this.angleGoal = angleGoal;
    }

    public PoliticalFaction getPoliticalFaction() {
        return politicalFaction;
    }

    public int getDecisionDelay() {
        return decisionDelay;
    }

    public void setDecisionDelay(int decisionDelay) {
        this.decisionDelay = decisionDelay;
    }

    public int getCombatDelay() {
        return combatDelay;
    }

    public void resetCombatDelay() {
        this.combatDelay = singleStats.combatDelay;
    }

    public double getCombatRangeStat() {
        return singleStats.combatRange;
    }

    public double getAttackStat() {
        return singleStats.attack;
    }

    public int getCombatDelayStat() {
        return singleStats.combatDelay;
    }

    public SingleStats getSingleStats() {
        return singleStats;
    }

    public BaseUnit getUnit() {
        return unit;
    }

    public double getDamageSustain() {
        return damageSustain;
    }
    public void setDamageSustain(double damageSustain) {
        this.damageSustain = damageSustain;
    }

    public int getJustHit() {
        return justHit;
    }

    public boolean isScreamDeath() {
        return screamDeath;
    }
    public void setScreamDeath(boolean screamDeath) {
        this.screamDeath = screamDeath;
    }

    public double getFacingAngle() {
        return facingAngle;
    }

    public double getCollisionRadius() {
        return singleStats.collisionRadius;
    }

    public boolean isInPosition() {
        return inPosition;
    }

    public boolean isInDanger() {
        return inDanger;
    }

    public void setInDanger(boolean inDanger) {
        this.inDanger = inDanger;
    }

    public HashMap<BaseObject, Integer> getCarriedObjects() {
        return carriedObjects;
    }

    public boolean isDead() {
        return state == SingleState.DEAD;
    }
}

package model.singles;

import model.constants.UniversalConstants;
import javafx.util.Pair;
import model.enums.SingleState;
import model.objects.BaseObject;
import model.units.BaseUnit;
import model.enums.PoliticalFaction;
import model.enums.UnitState;
import model.utils.MathUtils;
import model.utils.MovementUtils;

import java.util.HashMap;

public class BaseSingle {

    // Political attribute
    PoliticalFaction politicalFaction;
    BaseUnit unit;  // The unit the individual belong to
    int singleIndex;  // The index of the troop in the array.

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
    double height;

    // Position goal
    double xGoal;
    double yGoal;
    double speedGoal;
    double angleGoal;
    double facingAngleGoal;

    // Distance from goal
    double tempSquaredDistanceFromGoal;

    // Currents state of the single
    double hp;
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
     */
    public BaseSingle(SingleStats singleStats) {
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

        // Calculate intended speed
        double variation = 1;
        if (speed != 0) variation = MathUtils.randDouble(0.95, 1.05);
        if (speed < speedGoal) speed = Math.min(speed + UniversalConstants.SPEED_ACC, speedGoal);
        else if (speed > speedGoal) speed = Math.max(speed - UniversalConstants.SPEED_ACC, speedGoal);
        speed *= variation;

        // Calculate intended step
        xVel = MathUtils.quickCos((float) angle) * speed;
        yVel = MathUtils.quickSin((float) angle) * speed;

        // Update based on states
        double distanceToGoal = MathUtils.quickRoot1((float)((x - xGoal) * (x - xGoal) + (y - yGoal) * (y - yGoal)));
        switch (state) {
            case MOVING:
                // Recalculate vx, vy
                angle = MathUtils.atan2(yGoal - y, xGoal - x);

                // Out of position if it takes more than 3 steps to reach
                if (distanceToGoal > singleStats.outOfReachDist) {
                    speedGoal = singleStats.outOfReachSpeed;
                } else if ((distanceToGoal > singleStats.standingDist) && (distanceToGoal < singleStats.outOfReachDist)) {
                    speedGoal = singleStats.speed;
                } else if (distanceToGoal < singleStats.standingDist) {
                    switchState(SingleState.IN_POSITION);
                }
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
    }

    /**
     * Update the state of the troop
     */
    public void updateState() {
        if (state != SingleState.DEAD) {
            x += xVel;
            y += yVel;
        }
        damageSustain -= UniversalConstants.SUSTAIN_COOLDOWN;
        if (damageSustain < 0) damageSustain = 0;
        justHit -= 1;
        if (justHit < 0) justHit = 0;
        facingAngle = MovementUtils.rotate(facingAngle, facingAngleGoal, singleStats.rotationSpeed);

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

    }

    /**
     * Let the troop receive damage. Troop with health lower than 0 should die.
     */
    public void receiveDamage(double damage) {
        hp -= damage;
        damageSustain += damage;
        justHit = 2;
        if (hp < 0) switchState(SingleState.DEAD);
    }

    /**
     * Switch state function
     */
    public void switchState(SingleState newState) {
        // Can only switch state if the unit is not DEAD or not UNCONTROLLABLE
        switch (state) {
            case IN_POSITION:
            case MOVING:
            case DECELERATING:
            case BRACING:
            case FIGHTING:
            case FIRE_AT_WILL:
                state = newState;
                if (state == SingleState.DEAD) screamDeath = true;
                break;
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

    /**
     * Get row number
     */
    public int getRow() {
        return singleIndex / unit.getWidth();
    }

    /**
     * Get col number
     */
    public int getCol() {
        return singleIndex % unit.getWidth();
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

    public void setCombatDelay(int combatDelay) {
        this.combatDelay = combatDelay;
    }

    public double getCombatRangeStat() {
        return singleStats.combatDelay;
    }

    public double getAttackStat() {
        return singleStats.attack;
    }

    public int getCombatDelayStat() {
        return singleStats.combatDelay;
    }

    public BaseUnit getUnit() {
        return unit;
    }

    public int getSingleIndex() {
        return singleIndex;
    }
    public void setSingleIndex(int singleIndex) { this.singleIndex = singleIndex; };

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
}

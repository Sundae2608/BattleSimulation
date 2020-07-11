package model.singles;

import model.algorithms.ObjectHasher;
import model.constants.GameplayConstants;
import model.constants.UniversalConstants;
import model.enums.SingleState;
import model.enums.PoliticalFaction;
import model.enums.UnitState;
import model.objects.Stone;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.units.CatapultUnit;
import model.utils.MathUtils;
import model.utils.MovementUtils;

public class CatapultSingle extends BaseSingle {

    // These two delays are unique to archer.
    // - Reload delay: time waited before archer can fire
    // - Bored delay: time waited before archer switch from standing to FIRE_AT_WILL state, because things are too
    // boring.
    protected int reloadDelay;
    protected int boredDelay;

    // Object hasher. Since archer produced arrows, arrows have to be fed in somehow.
    protected ObjectHasher hasher;

    // Shooting target
    BaseSingle shootingTarget;

    public CatapultSingle(double xInit,
                         double yInit,
                         PoliticalFaction faction,
                         BaseUnit inputUnit,
                         SingleStats inputSingleStats,
                         int index,
                         ObjectHasher inputHasher) {
        // Parent constructor
        super(inputSingleStats);

        // Assign hasher
        hasher = inputHasher;

        // Positional attributes
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;

        // Set up political faction
        politicalFaction = faction;

        // Set default stat up stats
        unit = inputUnit;
        singleStats = inputSingleStats;

        // Default constants
        speedGoal = 0;
        speed = 0;
        height = 0;
        state = SingleState.IN_POSITION;

        // Reload delay and bored delay
        reloadDelay = MathUtils.randint(0, singleStats.reloadDelay);
        boredDelay = singleStats.boredDelay;
    }

    @Override
    public void updateIntention(Terrain terrain) {

        // Can't have intention if already dead
        if (state == SingleState.DEAD) return;

        // Can't change intention if still sliding
        if (state == SingleState.SLIDING) return;

        // Calculate intended speed
        double variation = 1;
        if (speed != 0) variation = MathUtils.randDouble(0.9, 1.1);
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

        // Calculate intended step
        xVel = MathUtils.quickCos((float) angle) * speed;
        yVel = MathUtils.quickSin((float) angle) * speed;

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
                    state = SingleState.MOVING;
                    speedGoal = singleStats.speed;
                }
                // Constantly reload and fire if there is a target in sight.
                BaseUnit unitFiredAt = ((CatapultUnit)unit).getUnitFiredAgainst();
                if (unitFiredAt != null) {
                    reloadDelay -= 1;
                    if (reloadDelay == 0) {
                        // Pick a random target
                        shootingTarget = ((CatapultUnit) unit).pickNextTarget();

                        // Don't fire target if there is nothing to fire at.
                        if (shootingTarget == null) break;

                        // Shoot an arrow into the world
                        hasher.addObject(new Stone(x, y, shootingTarget.getX(), shootingTarget.getY(),
                                singleStats.catapultSpeed,
                                singleStats.catapultDamage,
                                singleStats.catapultExplosionDamage,
                                singleStats.catapultExplosionRange,
                                singleStats.catapultExplosionPush,
                                singleStats.catapultPushForce,
                                singleStats.angleVariation));

                        // Reload arrow
                        reloadDelay = singleStats.reloadDelay;
                    }
                }
        }

        // If not too far from intended position, face the position of the army
        if (state != SingleState.FIRE_AT_WILL) {
            if (distanceToGoal < singleStats.nonRotationDist) {
                facingAngleGoal = angleGoal;
            } else {
                facingAngleGoal = angle;
            }
        } else {
            // Recalculate vx, vy
            if (shootingTarget == null) {
                facingAngleGoal = angleGoal;
            } else {
                facingAngleGoal = MathUtils.atan2(shootingTarget.getY() - y, shootingTarget.getX() - x);
            }
        }
    }
}

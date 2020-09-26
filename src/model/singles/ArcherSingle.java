package model.singles;

import model.algorithms.ObjectHasher;
import model.constants.UniversalConstants;
import model.enums.SingleState;
import model.enums.PoliticalFaction;
import model.enums.UnitState;
import model.objects.Arrow;
import model.terrain.Terrain;
import model.units.ArcherUnit;
import model.units.BaseUnit;
import model.utils.MathUtils;
import model.utils.MovementUtils;

public class ArcherSingle extends BaseSingle {

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

    public ArcherSingle(double xInit,
                        double yInit,
                        PoliticalFaction faction,
                        BaseUnit inputUnit,
                        SingleStats inputSingleStats,
                        ObjectHasher inputHasher) {
        // Parent constructor
        super(inputSingleStats, inputUnit);

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
        double distanceToGoal = MathUtils.quickRoot1((float)((x - xGoal) * (x - xGoal) + (y - yGoal) * (y - yGoal)));
        switch (state) {
            case MOVING:
                // Recalculate vx, vy
                angle = MathUtils.atan2(yGoal - y, xGoal - x);

                // Out of position if it takes more than 3 steps to reach
                if (distanceToGoal > singleStats.outOfReachDist) {
                    speedGoal = singleStats.outOfReachSpeed;
                } else if ((distanceToGoal > singleStats.standingDist) && (distanceToGoal < 10 * singleStats.speed)) {
                    speedGoal = singleStats.speed;
                } else if (distanceToGoal < singleStats.standingDist) {
                    state = SingleState.IN_POSITION;
                }
                boredDelay = singleStats.boredDelay;
                break;
            case IN_POSITION:
                speed = unit.isTurning() ? 0 : unit.getState() == UnitState.MOVING ? singleStats.speed : 0;
                speedGoal = unit.getState() == UnitState.MOVING ? singleStats.speed : 0;
                if (distanceToGoal > singleStats.standingDist) {
                    state = SingleState.MOVING;
                    speedGoal = singleStats.speed;
                }

                // Rotate the actor back to desired angle
                angle = MovementUtils.rotate(angle, angleGoal, singleStats.rotationSpeed);

                // If archer becomes too bored, he shall switch to fire at will mode.
                boredDelay -= 1;
                if (boredDelay == 0) state = SingleState.FIRE_AT_WILL;
                break;
            case FIRE_AT_WILL:
                speed = 0;
                if (distanceToGoal > singleStats.standingDist) {
                    state = SingleState.MOVING;
                    speedGoal = singleStats.speed;
                }
                // Constantly reload and fire if there is a target in sight.
                BaseUnit unitFiredAt = ((ArcherUnit)unit).getUnitFiredAgainst();
                if (unitFiredAt != null) {
                    reloadDelay -= 1;
                    if (reloadDelay == 0) {
                        // Pick a random target
                        shootingTarget = ((ArcherUnit) unit).pickNextTarget();

                        // Don't fire target if there is nothing to fire at.
                        if (shootingTarget == null) break;

                        // Shoot an arrow into the world
                        hasher.addObject(new Arrow(x, y, shootingTarget.getX(), shootingTarget.getY(),
                                singleStats.arrowSpeed,
                                singleStats.arrowDamage,
                                singleStats.arrowPushDist,
                                singleStats.angleVariation,
                                singleStats.impactLifetime));

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

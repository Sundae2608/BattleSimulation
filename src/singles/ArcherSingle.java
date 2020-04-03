package singles;

import algorithms.ObjectHasher;
import constants.ArcherConstants;
import constants.UniversalConstants;
import units.ArcherUnit;
import units.BaseUnit;
import units.PoliticalFaction;
import utils.MathUtils;
import utils.MovementUtils;

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

    public ArcherSingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index,
                        ObjectHasher inputHasher) {
        // Parent constructor
        super();

        // Assign hasher
        hasher = inputHasher;

        // Positional attributes
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;
        singleIndex = index;

        // Set up political faction
        politicalFaction = faction;
        // Set default stat up stats
        unit = inputUnit;

        speedStat = ArcherConstants.SPEED_STAT;
        deceleratingDistStat = ArcherConstants.DECELERATING_DIST;
        combatDelayStat = ArcherConstants.COMBAT_DELAY_STAT;
        combatRangeStat = ArcherConstants.COMBAT_RANGE;
        healthStat = ArcherConstants.HEALTH_STAT;
        attackStat = ArcherConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = ArcherConstants.SINGLE_SIZE;

        //
        reloadDelay = MathUtils.randint(0, ArcherConstants.RELOAD_DELAY);
        boredDelay = ArcherConstants.BORED_DELAY;

        // Preprocessing some internal stats
        preprocessStats();
    }

    public void updateIntention() {

        // Calculate intended speed
        double variation = 1;
        if (speed != 0) variation = MathUtils.randDouble(0.9, 1.1);
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
                if (distanceToGoal > 20 * speedStat) {
                    speedGoal = 1.5 * speedStat;
                } else if ((distanceToGoal > deceleratingDistStat) && (distanceToGoal < 10 * speedStat)) {
                    speedGoal = speedStat;
                } else if (distanceToGoal < deceleratingDistStat) {
                    state = SingleState.DECELERATING;
                    speedGoal = 0;
                }
                break;
            case DECELERATING:
                if (distanceToGoal < UniversalConstants.STANDING_DIST_RATIO) {
                    state = SingleState.IN_POSITION;
                    boredDelay = ArcherConstants.BORED_DELAY;
                }
            case IN_POSITION:
                speed = 0;
                if (distanceToGoal > UniversalConstants.STANDING_DIST_RATIO) {
                    state = SingleState.MOVING;
                    speedGoal = speedStat;
                }

                // Rotate the actor back to desired angle
                angle = MovementUtils.rotate(angle, angleGoal, UniversalConstants.ANGLE_ROTATION_SPEED);

                // If archer becomes too bored, he shall switch to fire at will mode.
                boredDelay -= 1;
                if (boredDelay == 0) state = SingleState.FIRE_AT_WILL;
                break;
            case FIRE_AT_WILL:
                speed = 0;
                if (distanceToGoal > UniversalConstants.STANDING_DIST_RATIO) {
                    state = SingleState.MOVING;
                    speedGoal = speedStat;
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
                                ArcherConstants.ARROW_SPEED + MathUtils.randDouble(
                                        -ArcherConstants.ANGLE_VARIATION,
                                        ArcherConstants.ANGLE_VARIATION)));

                        // Reload arrow
                        reloadDelay = ArcherConstants.RELOAD_DELAY;
                    }
                }
        }

        // If not too far from intended position, face the position of the army
        if (state != SingleState.FIRE_AT_WILL) {
            if (distanceToGoal < UniversalConstants.NON_ROTATION_DIST) {
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

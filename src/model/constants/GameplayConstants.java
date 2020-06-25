package model.constants;

public class GameplayConstants {
    // Different morale threshold for different behaviors.
    public final static double BASE_MORALE = 1;
    public final static double PANIC_MORALE = 0.3;
    public final static double RECOVER_MORALE = 0.5;
    public final static double SHATTERED_MORALE = 0;

    // The amount of morale recovery by the unit after each frame.
    public final static double MORALE_RECOVERY = 0.001;

    // Routing angle variation.
    // When running in a rout, it is not possible to run in a straight line. There should be lots of chaos
    public final static double ROUTING_ANGLE_VARIATION = 0.1;

    // While running for their life, the soldier should be running a bit faster than usual.
    public final static double ROUTING_SPEED_COEFFICIENT = 1.1;

    // Charge buffer. This prevents charge event from firing too often.
    public final static int CHARGE_BUFFER = 200;

    // Minimum distance between allied troops should be reduced. This naturally makes sense as allied troops should
    // be able to stand closer to each other during fights, or let the other person pass during reforming.
    public final static double ALLY_COLLISION_RATIO_STANDING = 0.2;
    public final static double ALLY_COLLISION_RATIO_MOVING = 0.7;
    public final static double ALLY_COLLISION_RATIO_FIGHTING = 0.8;

    // How many frame without engaging the enemy before the front-liner turns into a flanker.
    public final static double FLANKER_PATIENT = 100;

    // Slow down by different environment
    public final static double SURFACE_BEACH_SLOWDOWN = 1.0;
}

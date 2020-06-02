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
    public final static double ROUTING_ANGLE_VARIATION = 0.001;

    // While running for their life, the soldier should be running a bit faster than usual.
    public final static double ROUTING_SPEED_COEFFICIENT = 1.1;
}

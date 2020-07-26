package model.constants;

public class GameplayConstants {
    // Different morale threshold for different behaviors.
    public final static double BASE_MORALE = 1;
    public final static double PANIC_MORALE = 0.3;
    public final static double RECOVER_MORALE = 0.5;
    public final static double SHATTERED_MORALE = 0;

    // The amount of morale recovery by the unit after each frame.
    public final static double MORALE_RECOVERY = 1.0;

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
    public final static double TREE_COLLISION_SLOWDOWN = 0.5;

    // Forward distance after command.
    // After a command has been given, we will set the anchor position to be a little more forward to fix several
    // issues regarding troops abruptly changing their direction.
    public final static double FORWARD_DISTANCE = 150;

    // Flanking constants
    public final static double FLANKING_BONUS_SINGLE_SCALE = 1.5;
    public final static double FLANKING_ANGLE_SINGLE_THRESHOLD = Math.PI * 2 / 3;
    public final static double FLANKING_BONUS_UNIT_SCALE = 1.5;
    public final static double FLANKING_ANGLE_UNIT_THRESHOLD = Math.PI * 2 / 3;
    public final static double FLANKING_POSITION_JIGGLING_RATIO = 0.2;
    public final static double FLANKING_SPACING_RATIO = 0.5;

    // Minimum damage received after shield has been applied.
    public final static double MINIMUM_DAMAGE_RECEIVED = 1;

    // When the user right click to change formation, this is the minimum number of troops that have to make up the
    // front.
    public final static double MINIMUM_WIDTH_SELECTION = 4;
}

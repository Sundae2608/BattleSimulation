package model.constants;

public class UniversalConstants {

    // Hashing div
    public final static int X_HASH_DIV = 25;
    public final static int Y_HASH_DIV = 25;
    public final static int X_HASH_DIV_SURFACE_TREES = 300;
    public final static int Y_HASH_DIV_SURFACE_TREES = 300;

    // ---------------------------
    // Universal physics constants
    // ---------------------------
    public final static int GAME_FPS = 60;
    public final static int SQUARED_GAME_FPS = GAME_FPS * GAME_FPS;
    public final static double DIST_UNIT_PER_M = 23;
    public final static double GRAVITATIONAL_CONSTANT_FPM2 = 9.81 * DIST_UNIT_PER_M / (GAME_FPS * GAME_FPS);

    // ---------------------------------------
    // Universal combat and movement constants
    // ---------------------------------------

    // Push constant
    public final static double PUSH_SPRING_ALLY = 0.17;
    public final static double PUSH_SPRING_ENEMY = 0.35;
    public final static double PUSH_SIZE_MULTIPIER = 1.24;

    // Standing tolerance
    // A troop is considered out of reach if the distance is this multiples of its speed.
    public final static double OUT_OF_REACH_NUM_STEP = 5;
    public final static double OUT_OF_REACH_SPEED_MULTIPLIER = 1.40;

    // Sliding tolerance
    public final static double STOP_SLIDING_DIST = 0.1;
    public final static double SLIDING_FRICTION = 0.7;

    // The size of the eye that indicates direction
    public final static double EYE_SIZE = 6.5;

    // Non u-turn angle. (If the army is ordered to rotate less than this angle, it will not perform U-Turn)
    public static final double NON_UTURN_ANGLE = Math.PI / 2;
    public static final double NON_REGROUP_ANGLE = Math.PI / 4;

    // Tendency toward the furthest troops forward during reorder
    public static final double TENDENCY_TOWARD_FURTHEST_TROOP_DURING_REORDER = 0.5;

    // Carried objects life time. The amount of time carried object is being drawned before it fades away
    public final static int CARRIED_OBJECT_LIFETIME = 300;
    public final static int CARRIED_OBJECT_FADEAWAY = 50;

    // Maximum speed modification effect incurred by terrain
    public final static double MINIMUM_TERRAIN_EFFECT = 0;  // - 0.1
    public final static double MAXIMUM_TERRAIN_EFFECT = 0;  // 0.4

    // ----------------
    // Drawing constant
    // Keep it here for now for oonvenience
    // ----------------
    // TODO: Consider moving this to drawer constant since there is already a file like that.
    public final static double DAMAGE_SUSTAIN_MAXIMUM_EFFECT = 15;

    // The amount of sustain damage recovering
    public final static double SUSTAIN_COOLDOWN = 0.5;

    public final static double SHADOW_ANGLE = 0;
    public final static double SHADOW_SIZE = 1.4;
    public final static double SHADOW_OFFSET = 3.5;
    public final static double UNIT_SHADOW_OFFSET = 15;
    public final static int[] SHADOW_COLOR = {0, 0, 0, 128};

    public final static double SIMPLIFIED_SQUARE_SIZE_RATIO = 0.886226925453;

    // ----------------
    // Monitor constant
    // ----------------
    public final static int FRAME_STORAGE = 10;
}

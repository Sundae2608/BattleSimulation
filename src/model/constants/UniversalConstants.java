package model.constants;

public class UniversalConstants {

    // Hashing div
    public final static int X_HASH_DIV = 25;
    public final static int Y_HASH_DIV = 25;

    // ---------------------------------------
    // Universal combat and movement model.constants
    // ---------------------------------------

    // Push constant
    public final static double PUSH_SPRING_ALLY = 0.17;
    public final static double PUSH_SPRING_ENEMY = 0.35;
    public final static double PUSH_SIZE_MULTIPIER = 1.24;

    // Standing tolerance
    // A troop is considered out of reach if the distance is this multiples of its speed.
    public final static double OUT_OF_REACH_NUM_STEP = 5;
    public final static double OUT_OF_REACH_SPEED_MULTIPLIER = 1.35;

    // The size of the eye that indicates direction
    public final static double EYE_SIZE = 6.5;

    // Acceleration of speed.
    public final static double SPEED_ACC = 0.1;

    // Standing distance
    public final static double STANDING_DIST_RATIO = 1.4;

    // Non u-turn angle. (If the army is ordered to rotate less than this angle, it will not perform U-Turn)
    public static final double NON_UTURN_ANGLE = Math.PI * 3 / 5;

    // Carried objects life time. The amount of time carried object is being drawned before it fades away
    public final static int CARRIED_OBJECT_LIFETIME = 300;
    public final static int CARRIED_OBJECT_FADEAWAY = 50;

    // ----------------
    // Camera Constants
    // ----------------

    // Camera rotation speed
    public final static double CAMERA_ROTATION_SPEED = 0.03;

    // Camera movement border and speed
    public final static double CAMERA_BORDER_ZONE = 40;
    public final static double CAMERA_SPEED = 20;

    // Minimum zoom and zoom speed
    public final static double MINIMUM_ZOOM = 0.05;
    public final static double ZOOM_PER_SCROLL = 1.1;

    // Zoom rendering level
    public final static double ZOOM_RENDER_LEVEL_DETAILS = 1.0;
    public final static double ZOOM_RENDER_LEVEL_NORMAL = 1.0;
    public final static double ZOOM_RENDER_LEVEL_ARROW_DETAIL = 0.7;
    public final static double ZOOM_RENDER_LEVEL_SIMPLIFY_TROOP_SHAPE = 0.40;
    public final static double ZOOM_RENDER_LEVEL_PERCEPTIVE = 0.3;
    public final static double ZOOM_RENDER_LEVEL_CAV_PERCEPTIVE = 0.20;
    public final static double ZOOM_RENDER_LEVEL_TROOP = 0.09;
    public final static double ZOOM_RENDER_SPEAR_DISAPPEAR = ZOOM_RENDER_LEVEL_TROOP;

    // Distance attack
    // If the mouse click on n enemy unit is smaller than this
    // TODO(sonpham): Potentially change the icon to sword to indicate potentialldy attack as well.ds
    public final static double CLICK_ATTACK_DISTANCE = 150;
    public final static double SQUARE_CLICK_ATTACK_DISTANCE = CLICK_ATTACK_DISTANCE * CLICK_ATTACK_DISTANCE;

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
}

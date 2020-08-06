package view.camera_hex;

public class HexCameraConstants {
    // Camera rotation speed
    public final static double CAMERA_ROTATION_SPEED = 0.03;
    public final static int CAMERA_ROTATION_DECELERATION_STEPS = 20;
    public final static double CAMERA_ROTATION_DECELERATION = CAMERA_ROTATION_SPEED / CAMERA_ROTATION_DECELERATION_STEPS;
    public final static double CAMERA_ZOOM_DECELERATION_COEFFICIENT = 0.85;
    public final static double CAMERA_MOVEMENT_DECELERATION_COEFFICIENT = 0.85;

    // Camera movement border and speed
    public final static double CAMERA_BORDER_ZONE = 40;
    public final static double CAMERA_SPEED = 20;

    // Zoom constant
    public final static double MINIMUM_ZOOM = 0.03;
    public final static double MAXIMUM_ZOOM = 5.0;
    public final static double HEIGHT_AT_MAX_ZOOM = 20;
    public final static double ZOOM_PER_SCROLL = 1.1;
    public final static int ZOOM_SMOOTHEN_STEPS = 100;

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
    // TODO(sonpham): Potentially change the icon to indicate an attack
    public final static double CLICK_ATTACK_DISTANCE = 500;
    public final static double SQUARE_CLICK_ATTACK_DISTANCE = CLICK_ATTACK_DISTANCE * CLICK_ATTACK_DISTANCE;

    // Shake level
    public final static double SHAKE_LEVEL_EXPLOSION = 20;
    public final static double SHAKE_LEVEL_SOLDIER_CHARGE = 100;
    public final static double SHAKE_LEVEL_CAVALRY_CHARGE = 450;
    public final static double SHAKE_LEVEL_AT_BASE = 300;
    public final static double SHAKE_LEVEL_MAX = 300;
    public final static double CAMERA_SHAKE_BASE = 20;
    public final static double SHAKE_LEVEL_DROP = 1.5;
}

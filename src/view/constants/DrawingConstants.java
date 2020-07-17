package view.constants;

public class DrawingConstants {

    // Good black. When in doubt, use this color
    public final static int[] COLOR_GOOD_BLACK = {25, 25, 25, 255};

    // Faction colors
    public final static int[] COLOR_UNKNOWN = {128, 128, 128, 255};  // #808080
    public final static int[] COLOR_ROMAN = {191, 73, 68, 255};      // #BF4944
    public final static int[] COLOR_GAUL = {89, 191, 104, 255};      // #59BF68
    public final static int[] COLOR_ATHENS = {61, 108, 140, 255};    // #3D6C8C
    public final static int[] COLOR_SPARTA = {133, 21, 23, 255};     // #851517
    public final static int[] COLOR_THEBES = {232, 176, 67, 255};    // #E4F4FA

    // Single state colors
    public final static int[] COLOR_DEAD = {0, 0, 0, 76};                 // #000000, 76%
    public final static int[] COLOR_IN_DANGER = {226, 73, 255, 255};      // #E249FF
    public final static int[] COLOR_IN_POSITION = {192, 227, 75, 255};    // #C0E34B
    public final static int[] COLOR_GOAL_POSITION = {255, 226, 18, 255};  // #FFE212

    // Icon colors
    public final static int[] COLOR_HEALTH = {183, 62, 50, 255};    // #B73E32
    public final static int[] COLOR_MORALE = {209, 170, 47, 255};  // #D1AA2F

    // Terrain colors
    public final static int[] COLOR_TERRAIN_DOT = {0, 0, 0, 128};  // #000000, 12%
    public final static int[] COLOR_TERRAIN_LINE = {0, 0, 0};
    public final static int COLOR_TERRAIN_LINE_MAX_ALPHA = 160;
    public final static int COLOR_TERRAIN_LINE_MIN_ALPHA = 50;
    public final static int COLOR_TERRAIN_LINE_ALPHA_RANGE = COLOR_TERRAIN_LINE_MAX_ALPHA - COLOR_TERRAIN_LINE_MIN_ALPHA;
    public final static int COLOR_ALPHA_UNIT_SELECTION = 170;

    // Universal object color
    public final static int[] UNIVERSAL_OBJECT_COLOR = {50, 50, 50, 255};

    // Surface colors
    public final static int[] SURFACE_COLOR_DEFAULT = {25, 25, 25, 128};
    public final static int[] SURFACE_COLOR_BEACH = {235, 215, 109, 128};
    public final static int[] SURFACE_COLOR_DESERT = {235, 219, 169, 128};
    public final static int[] SURFACE_COLOR_FOREST = {97, 184, 168, 128};
    public final static int[] SURFACE_COLOR_MARSH = {99, 186, 121, 128};
    public final static int[] SURFACE_COLOR_RIVERSIDE = {196, 172, 130, 128};
    public final static int[] SURFACE_COLOR_SHADOW_RIVER = {157, 230, 246, 128};
    public final static int[] SURFACE_COLOR_SNOW = {225, 240, 240, 128};
    public final static int[] TREE_COLOR = {125, 95, 76, 255};

    // Unit size color
    public final static float[] UNIT_SIZE_COLOR = {0, 0, 0, 25};

    // Anchor arrow size
    public final static int ANCHOR_ARROW_SIZE = 100;

    // Grid scale
    public final static float GRID_SIZE = 500;
    public final static float[] GRID_COLOR = {0, 0, 0, 51};

    // Drawing boundary
    public final static double DRAWING_OUTER_BOUNDARY = 20;

    // Terrain scale
    public final static double TERRAIN_HEIGHT_SCALE = 150;

    // Plan arrow size
    public final static double ARROW_SIZE = 30;
    public final static double BALISTA_SIZE = 100;
    public final static double CATAPULT_SIZE = 20;

    // Height scale for airborne objects
    // Because our camera exaggerate the terrain features, arrow trails become hugely unrealistic. This variable softens
    // the height effect of arrows a little bit to make the arrow looks more palatable.
    public final static double AIRBORNE_OBJECT_HEIGHT_SCALE = 0.50;
}

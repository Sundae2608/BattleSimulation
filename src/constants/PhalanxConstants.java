package constants;

public class PhalanxConstants {

    // Formation attribute

    // Troop at these first rows will stand slightly to the right compared to the rest of the unit to make space for
    // spear.
    public final static int NUM_FIRST_ROWS = 5;
    public final static double OFF_ANGLE_FIRST_ROWS = - Math.PI / 30;  // Negative means slightly to the right

    // Physical attribute
    public final static double SINGLE_SIZE = 23;
    public final static double SPEED_STAT = 2;
    public final static double HEALTH_STAT = 500;
    public final static double ATTACK_STAT = 11;

    // Decelerating distance
    public final static double DECELERATING_DIST = 4;

    // Spacing
    public final static int SPACING = 30;

    // Combat constants
    public final static int UNIT_FIGHT_DELAY = 50;
    public final static int COMBAT_DELAY_STAT = 10;
    public final static double COMBAT_RANGE = 25;
}

package model.utils;

import model.constants.GameplayConstants;
import model.construct.Construct;
import model.units.BaseUnit;

import java.util.ArrayList;

public final class GameplayUtils {
    public final static double MORALE_LOSS_CONSTANT_K = 2.0;

    /**
     * Each dead soldier will contribute to morale loss equaling K / num_troops * 100, in which K is a constant.
     * A simple way to understand K is that if K equals 2, a loss of 1 man in a unit equaling a loss of 2 man for the
     * morale of that unit.
     * @param numTroops
     * @return
     */
    public static double moraleLossDueToDeadSoldier(int numTroops) {
        return MORALE_LOSS_CONSTANT_K / numTroops;
    }

    /**
     *
     */
    public static boolean checkIfUnitCanMoveTowards(double x, double y, ArrayList<Construct> constructs) {
        for (Construct construct : constructs) {
            if (PhysicUtils.checkPolygonPointCollision(construct.getBoundaryPoints(), x, y)) {
                return false;
            }
        }
        return true;
    }
}
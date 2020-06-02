package model.utils;

import model.constants.GameplayConstants;

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
}
package utils;

import singles.BaseSingle;
import units.BaseUnit;

import java.util.ArrayList;
import java.util.HashSet;

public final class TestUtils {

    /**
     * Check the formation of the unit.
     */
    public static boolean checkFormation(BaseUnit unit) {
        // Number of full rows
        int rows = unit.getNumAlives() / unit.getWidth();
        ArrayList<BaseSingle> troops = unit.getTroops();
        HashSet<BaseSingle> troopSet = unit.getAliveTroopsSet();

        // Examine full rows;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < unit.getWidth(); j++) {
                int index = i * unit.getWidth() + j;
                if (!troopSet.contains(troops.get(index))) return false;
            }
        }

        // Examine the last row;
        int rem = unit.getNumAlives() % unit.getWidth();
        if (rem == 0) return true; // If no remainder, that means the formation is already correct
        int groups = 0;
        boolean deadSwitch = true;
        for (int j = 0; j < unit.getWidth(); j++) {
            // Single state from left to right must not switch more than twice (implying more than two groups);
            int index = rows * unit.getWidth() + j;
            if (!troopSet.contains(troops.get(index)) != deadSwitch) {
                groups += 1;
                deadSwitch = !troopSet.contains(troops.get(index));
            }
        }
        return groups <= 2;
    }
}

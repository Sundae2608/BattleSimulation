package model.utils;

import model.singles.BaseSingle;
import model.units.BaseUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class TestUtils {

    /**
     * Check the formation of the unit.
     */
    public static boolean checkFormation(BaseUnit unit) {
        // Number of full rows
        int rows = unit.getNumAlives() / unit.getWidth();

        // Get the troops formation
        BaseSingle[][] formation = unit.getAliveTroopsFormation();

        // Examine full rows;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < unit.getWidth(); j++) {
                if (formation[i][j] == null) return false;
            }
        }

        // Examine the last row;
        int rem = unit.getNumAlives() % unit.getWidth();
        if (rem == 0) return true; // If no remainder, that means the formation is already correct
        int begin;
        for (begin = 0; begin < unit.getWidth(); begin++) {
            if (formation[rows][begin] != null) {
                break;
            }
        }
        int end;
        for (end = unit.getWidth() - 1; end >= 0; end--) {
            if (formation[rows][end] != null) {
                break;
            }
        }
        return end - begin + 1 == rem;
    }

    /**
     * String represents the formation of the unit. It looks something like:
     * o o o o o o o o o
     * o o o o o o o o o
     * - - - o o o - - -
     * Used for logging convenience
     */
    public static String formationString(BaseSingle[][] formation) {
        StringBuilder s = new StringBuilder();
        s.append("\n");
        for (int i = 0; i < formation.length; i++) {
            for (int j = 0; j < formation[0].length; j++) {
                if (formation[i][j] == null) {
                    s.append("- ");
                } else {
                    s.append("o ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}

package utils;

import model.enums.SingleState;
import model.units.BaseUnit;

public final class DebugUtils {
    public static String formationString(BaseUnit unit) {
        String s = "";
        for (int i = 0; i < unit.getTroops().size(); i++) {
            int row = i / unit.getWidth();
            int col = i % unit.getWidth();
            if (unit.getTroops().get(i).getState() == SingleState.DEAD) {
                s += " - ";
            } else {
                s += " O ";
            }
            if (col == unit.getWidth() - 1) {
                s += "\n";
            }
        }
        return s;
    }

    public static String formationStringWithAliveFormationMap(BaseUnit unit) {
        String s = "";
        for (int i = 0; i < unit.getAliveTroopsFormation().length; i++) {
            for (int j = 0; j < unit.getAliveTroopsFormation()[0].length; j++) {
                if (unit.getAliveTroopsFormation()[i][j] == null) {
                    s += " - ";
                } else {
                    s += " O ";
                }
                if (j == unit.getWidth() - 1) {
                    s += "\n";
                }
            }
        }
        return s;
    }
}

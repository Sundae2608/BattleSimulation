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
}

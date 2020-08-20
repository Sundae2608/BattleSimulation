package model.utils;

import model.singles.BaseSingle;

public final class SingleUtils {

    /**
     * Square distance between two singles
     */
    public static double squareDistBetweenSingles(BaseSingle single1, BaseSingle single2) {
        double dx = single1.getX() - single2.getX();
        double dy = single1.getY() - single2.getY();
        return dx * dx + dy * dy;
    }
}

package model.checker;

import model.GameEnvironment;
import model.singles.BaseSingle;
import model.terrain.Terrain;
import model.units.BaseUnit;

import java.util.ArrayList;

public final class EnvironmentChecker {

    private static boolean checkAllSoldiersStayInTerrain(ArrayList<BaseUnit> units, Terrain terrain) {
        for (BaseUnit unit : units) {
            for (BaseSingle single : unit.getAliveTroopsSet()) {
                if (!terrain.isWithinTerrain(single.getX(), single.getY())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkEnvironmentValid(GameEnvironment env) {
        if (!checkAllSoldiersStayInTerrain(env.getUnits(), env.getTerrain())) {
            // Check that all units stay within the boundary of the terrain
            return false;
        }
        return true;
    }
}

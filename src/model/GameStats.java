package model;

import javafx.util.Pair;
import model.enums.PoliticalFaction;
import model.enums.UnitType;
import model.singles.SingleStats;
import model.units.unit_stats.UnitStats;

import java.util.HashMap;

public final class GameStats {

    HashMap<Pair<UnitType, PoliticalFaction>, SingleStats> singleStatsMap;
    HashMap<Pair<UnitType, PoliticalFaction>, UnitStats> unitStatsMap;
    public GameStats() {
        singleStatsMap = new HashMap<>();
        unitStatsMap = new HashMap<>();
    }

    public void addSingleStats(UnitType unitType, PoliticalFaction faction, SingleStats singleStats) {
        singleStatsMap.put(new Pair<>(unitType, faction), singleStats);
    }

    public void addUnitStats(UnitType unitType, PoliticalFaction faction, UnitStats unitStats) {
        unitStatsMap.put(new Pair<>(unitType, faction), unitStats);
    }

    public SingleStats getSingleStats(UnitType unitType, PoliticalFaction faction) {
        if (!singleStatsMap.containsKey(faction)) {
            return singleStatsMap.get(new Pair<>(unitType, PoliticalFaction.DEFAULT));
        }
        return singleStatsMap.get(new Pair<>(unitType, faction));
    }

    public UnitStats getUnitStats(UnitType unitType, PoliticalFaction faction) {
        if (!unitStatsMap.containsKey(faction)) {
            return unitStatsMap.get(new Pair<>(unitType, PoliticalFaction.DEFAULT));
        }
        return unitStatsMap.get(new Pair<>(unitType, faction));
    }
}

package model;

import model.algorithms.UnitModifier;
import model.enums.UnitState;
import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventType;
import model.objects.Balista;
import model.singles.BaseSingle;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.units.CavalryUnit;
import utils.ConfigUtils;
import model.settings.GameSettings;

import java.io.IOException;
import java.util.ArrayList;

public class GameEnvironment {
    /**
     * The main game model that contains every information about the game backend.
     */

    // Contain all units and troops
    ArrayList<BaseUnit> units;
    UnitModifier unitModifier;
    ArrayList<BaseSingle> deadContainer;

    // Terrain
    Terrain terrain;

    // Game settings
    GameSettings gameSettings;
    GameStats gameStats;

    // Event broadcaster, so that the view and the outside API can interact with the game
    EventBroadcaster broadcaster;

    /**
     *
     * @param battleConfig Path to the txt file that contains all the game information
     */
    public GameEnvironment(String gameConfig, String terrainConfig, String battleConfig,
                           GameSettings inputGameSettings) {
        broadcaster = new EventBroadcaster();
        gameSettings = inputGameSettings;
        deadContainer = new ArrayList<>();
        try {
            terrain = ConfigUtils.createTerrainFromConfig(terrainConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        unitModifier = new UnitModifier(deadContainer, terrain, gameSettings, broadcaster);
        // Read game stats
        try {
            gameStats = ConfigUtils.readGameStats(gameConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Read battle configuration
        try {
            units = ConfigUtils.readBattleConfigs(
                    battleConfig, gameStats, unitModifier.getObjectHasher(), terrain, broadcaster);
        } catch (IOException e){
            e.printStackTrace();
        }
        for (BaseUnit unit : units) {
            unitModifier.addUnit(unit);
        }
    }

    /**
     * Loop the game by one step
     */
    public void step() {
        // Update intentions of all units
        unitModifier.getObjectHasher().updateObjects();
        for (BaseUnit unit : units) {
            unit.updateIntention();
        }
        // Update the states of all units
        unitModifier.modifyObjects();
        for (BaseUnit unit : units) {
            unit.updateState();
        }
        // Broadcast running and marching events
        for (BaseUnit unit : units) {
            switch (unit.getState()) {
                case MOVING:
                    if (unit instanceof CavalryUnit) {
                        broadcaster.broadcastEvent(
                                new Event(EventType.CAVALRY_RUNNING,
                                        unit.getAverageX(),
                                        unit.getAverageY(),
                                        unit.getAverageZ()));
                    } else {
                        broadcaster.broadcastEvent(
                                new Event(EventType.CAVALRY_RUNNING,
                                        unit.getAverageX(),
                                        unit.getAverageY(),
                                        unit.getAverageZ()));
                    }
                    break;
                case FIGHTING:
                    broadcaster.broadcastEvent(
                            new Event(EventType.SOLDIER_FIGHTING,
                                    unit.getAverageX(),
                                    unit.getAverageY(),
                                    unit.getAverageZ()));
            }
        }
    }

    /**
     * Getter and setters
     */
    public ArrayList<BaseUnit> getUnits() {
        return units;
    }

    public void setUnits(ArrayList<BaseUnit> units) {
        this.units = units;
    }

    public UnitModifier getUnitModifier() {
        return unitModifier;
    }

    public ArrayList<BaseSingle> getDeadContainer() {
        return deadContainer;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }
    public GameStats getGameStats() {
        return gameStats;
    }
    public EventBroadcaster getBroadcaster() {
        return broadcaster;
    }
}

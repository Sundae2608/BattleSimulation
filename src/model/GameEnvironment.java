package model;

import javafx.util.Pair;
import model.algorithms.UnitModifier;
import model.algorithms.pathfinding.Graph;
import model.constants.UniversalConstants;
import model.construct.Construct;
import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventType;
import model.events.custom_events.CavalryMarchingEvent;
import model.events.custom_events.SoldierMarchingEvent;
import model.monitor.Monitor;
import model.singles.BaseSingle;
import model.surface.BaseSurface;
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
    ArrayList<BaseSurface> surfaces;
    UnitModifier unitModifier;
    ArrayList<BaseSingle> deadContainer;

    // Terrain
    Terrain terrain;
    ArrayList<Construct> constructs;
    Graph graph;

    // Game settings
    GameSettings gameSettings;
    GameStats gameStats;

    // Event broadcaster, so that the view and the outside API can interact with the game
    EventBroadcaster broadcaster;

    // Monitor, to keep track of things in the game
    Monitor monitor;

    /**
     *
     * @param battleConfig Path to the txt file that contains all the game information
     */
    public GameEnvironment(String gameConfig, String terrainConfig, String constructsConfig, String surfaceConfig,
                           String battleConfig, GameSettings inputGameSettings) {
        broadcaster = new EventBroadcaster();
        monitor = new Monitor(UniversalConstants.FRAME_STORAGE);
        gameSettings = inputGameSettings;
        deadContainer = new ArrayList<>();
        // Read terrain configuration.
        try {
            terrain = ConfigUtils.createTerrainFromConfig(terrainConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Read construct configuration.
        try {
            Pair<Graph, ArrayList<Construct>> pair = ConfigUtils.createConstructsAndGraphsFromConfig(constructsConfig);
            graph = pair.getKey();
            constructs = pair.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Read surface configuration.
        try {
            surfaces = ConfigUtils.createSurfacesFromConfig(surfaceConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        unitModifier = new UnitModifier(
                deadContainer, terrain, constructs, surfaces, gameSettings, broadcaster, monitor);
        // Read game stats.
        try {
            gameStats = ConfigUtils.readGameStats(gameConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Read battle configuration.
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
        // Reset counters
        monitor.clockTheData();

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
                case STANDING:
                    int numMovings = unit.getNumMoving();
                    if (numMovings > 0) {
                        if (unit instanceof CavalryUnit) {
                            broadcaster.broadcastEvent(
                                    new CavalryMarchingEvent(unit.getAverageX(), unit.getAverageY(), unit.getAverageZ(),
                                            numMovings));
                        } else {
                            broadcaster.broadcastEvent(
                                    new SoldierMarchingEvent(unit.getAverageX(), unit.getAverageY(), unit.getAverageZ(),
                                            numMovings));
                        }
                    }
                    break;
                case MOVING:
                case ROUTING:
                    numMovings = unit.getNumMoving();
                    if (unit instanceof CavalryUnit) {
                        broadcaster.broadcastEvent(
                                new CavalryMarchingEvent(unit.getAverageX(), unit.getAverageY(), unit.getAverageZ(),
                                        numMovings));
                    } else {
                        broadcaster.broadcastEvent(
                                new SoldierMarchingEvent(unit.getAverageX(), unit.getAverageY(), unit.getAverageZ(),
                                        numMovings));
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

    public Graph getGraph() {
        return graph;
    }

    public ArrayList<Construct> getConstructs() {
        return constructs;
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

    public Monitor getMonitor() {
        return monitor;
    }

    public ArrayList<BaseSurface> getSurfaces() {
        return surfaces;
    }


}

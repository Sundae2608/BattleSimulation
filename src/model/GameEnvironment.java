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
import model.sound.SoundSource;
import model.surface.BaseSurface;
import model.terrain.Terrain;
import model.units.ArcherUnit;
import model.units.BaseUnit;
import model.units.CavalryUnit;
import utils.ConfigUtils;
import model.settings.GameSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class GameEnvironment {
    /**
     * The main game model that contains every information about the game backend.
     */

    // Contain all units and troops
    ArrayList<BaseUnit> units;
    HashSet<BaseUnit> deadUnits;
    HashSet<BaseUnit> aliveUnits;
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

    // Contains all sound sources in the environment
    ArrayList<SoundSource> soundSources;

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
        deadUnits = new HashSet<>();
        aliveUnits = new HashSet<>();
        soundSources = new ArrayList<>();

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
                    battleConfig, gameStats, unitModifier.getObjectHasher(), terrain, broadcaster, gameSettings, this);
        } catch (IOException e){
            e.printStackTrace();
        }
        for (BaseUnit unit : units) {
            unitModifier.addUnit(unit);
            aliveUnits.add(unit);
        }

        for (BaseUnit unit : units){
            soundSources.add(unit.getSoundSource());
        }

        // TODO: Another for loop around terrain for new sound source
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
            if (unit.getNumAlives() == 0) {
                deadUnits.add(unit);
                aliveUnits.remove(unit);
            }
        }

        // Update sound source for all units
        for (BaseUnit unit : units) {
            unit.updateSoundSource();
        }

        // Update sound sinks for all units
        for (BaseUnit unit : units) {
            // Notice that each unit is a source for its sink as well. If it is too noisy, it might not be able to perceive the surrounding.
            unit.updateSoundSink(soundSources, terrain, surfaces, units);
            unit.updatePerceivedSoundSink();
        }

        // Broadcast running, marching and arrow fire event events
        for (BaseUnit unit : units) {
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
            switch (unit.getState()) {
                case STANDING:
                case MOVING:
                case ROUTING:
                    break;
                case FIGHTING:
                    broadcaster.broadcastEvent(
                            new Event(EventType.SOLDIER_FIGHTING,
                                    unit.getAverageX(),
                                    unit.getAverageY(),
                                    unit.getAverageZ()));
            }
            if (unit instanceof ArcherUnit && ((ArcherUnit) unit).getUnitFiredAgainst() != null) {
                BaseUnit firedUnit = ((ArcherUnit) unit).getUnitFiredAgainst();
                broadcaster.broadcastEvent(
                        new Event(EventType.ARROW_CROWD_HIT,
                                firedUnit.getAverageX(),
                                firedUnit.getAverageY(),
                                firedUnit.getAverageZ()));
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

    public HashSet<BaseUnit> getDeadUnits() {
        return deadUnits;
    }

    public HashSet<BaseUnit> getAliveUnits() {
        return aliveUnits;
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

package utils;

import javafx.util.Pair;
import model.GameEnvironment;
import model.GameStats;
import model.algorithms.ObjectHasher;
import model.algorithms.pathfinding.Graph;
import model.constants.UniversalConstants;
import model.construct.Construct;
import model.enums.PoliticalFaction;
import model.enums.SurfaceType;
import model.enums.UnitType;
import model.events.EventBroadcaster;
import model.settings.GameSettings;
import model.singles.SingleStats;
import model.surface.*;
import model.terrain.Terrain;
import model.units.*;
import model.units.unit_stats.UnitStats;
import model.utils.MathUtils;
import processing.core.PApplet;
import processing.core.PImage;
import utils.json.*;
import view.audio.*;
import view.camera.BaseCamera;
import view.video.VideoElementPlayer;
import view.video.VideoElementType;
import view.video.VideoTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigUtils {
    /**
     * Read the battle config, which defines the position and size of each unit that participate in the games.
     */
    public static ArrayList<BaseUnit> readBattleConfigs(
            String filePath, GameStats gameStats, ObjectHasher hasher, Terrain terrain, EventBroadcaster broadcaster,
            GameSettings gameSettings, GameEnvironment env) throws IOException {
        JsonIO jsonIO = new BattleUnitsIO(gameStats, hasher, terrain, broadcaster, gameSettings, env);
        return (ArrayList<BaseUnit>) jsonIO.read(filePath);
    }

    /**
     * Read the game statistics, which defines statistics of each unit that participates in the game.
     */
    public static GameStats readGameStats(String filePath) throws IOException {
        JsonIO jsonIO = new GameStatsIO();
        return (GameStats) jsonIO.read(filePath);
    }

    /**
     * Read video element config, which contains image footage that creates high quality in-game video elements.
     * @throws IOException
     */
    public static VideoElementPlayer readVideoElementConfig(String filePath, BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) throws IOException {
        JsonIO jsonIO = new VideoElementPlayerIO(camera, applet, eventBroadcaster);
        return (VideoElementPlayer) jsonIO.read(filePath);
    }

    /**
     * Create constructs from config file.
     */
    public static Pair<Graph, ArrayList<Construct>> createConstructsAndGraphsFromConfig(String filePath) throws IOException {
        JsonIO jsonIO = new ConstructsAndGraphIO();
        return (Pair<Graph, ArrayList<Construct>>) jsonIO.read(filePath);
    }

    /**
     * Create constructs from config file.
     */
    public static ArrayList<BaseSurface> createSurfacesFromConfig(String filePath) throws IOException {
        JsonIO jsonIO = new SurfaceIO();
        return (ArrayList<BaseSurface>) jsonIO.read(filePath);
    }

    /**
     * Read audio configs
     * @return An audio broadcaster with the configs.
     */
    public static AudioSpeaker readAudioConfig(String filePath, BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) throws IOException {
        JsonIO jsonIO = new AudioSpeakerIO(camera, applet, eventBroadcaster);
        return (AudioSpeaker) jsonIO.read(filePath);
    }

    /**
     * Create a terrain based on configs input from a file.
     * @param filePath path that contains the input configs.
     * @return a terrain generated from the config.
     * @throws IOException if the file is unavailable.
     */
    public static Terrain createTerrainFromConfig(String filePath) throws IOException {
        JsonIO jsonIO = new TerrainIO();
        return (Terrain) jsonIO.read(filePath);
    }
}

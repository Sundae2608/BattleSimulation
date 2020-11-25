package utils;

import city_gen_model.CityObjects;
import javafx.util.Pair;
import model.GameEnvironment;
import model.GameStats;
import model.algorithms.HitscanHasher;
import model.algorithms.ProjectileHasher;
import model.algorithms.pathfinding.Graph;
import model.construct.Construct;
import model.enums.PoliticalFaction;
import model.events.EventBroadcaster;
import model.surface.*;
import model.terrain.Terrain;
import model.units.*;
import processing.core.PApplet;
import processing.sound.SoundFile;
import utils.json.*;
import view.audio.*;
import view.camera.BaseCamera;
import view.video.StaticElementPlayer;
import view.video.VideoElementPlayer;
import java.io.IOException;
import java.util.ArrayList;

public final class ConfigUtils {
    /**
     * Read the battle config, which defines the position and size of each unit that participate in the games.
     */
    public static ArrayList<BaseUnit> readBattleConfigs(
            String filePath, GameStats gameStats, ProjectileHasher projectileHasher, HitscanHasher hitscanHasher,
            GameEnvironment env) throws IOException {
        JsonIO jsonIO = new BattleUnitsIO(gameStats, projectileHasher, hitscanHasher, env);
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
     */
    public static VideoElementPlayer readVideoElementConfig(String filePath, BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) throws IOException {
        JsonIO jsonIO = new VideoElementPlayerIO(camera, applet, eventBroadcaster);
        return (VideoElementPlayer) jsonIO.read(filePath);
    }

    /**
     * Read video element config, which contains image footage that creates high quality in-game video elements.
     */
    public static StaticElementPlayer readStaticElementConfig(String filePath, BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) throws IOException {
        JsonIO jsonIO = new StaticElementPlayerIO(camera, applet, eventBroadcaster);
        return (StaticElementPlayer) jsonIO.read(filePath);
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
     */
    public static AudioSpeaker readAudioConfig(String filePath, BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) throws IOException {
        JsonIO jsonIO = new AudioSpeakerIO(camera, applet, eventBroadcaster);
        return (AudioSpeaker) jsonIO.read(filePath);
    }

    /**
     * Create a terrain based on configs input from a file.
     */
    public static Terrain createTerrainFromConfig(String filePath) throws IOException {
        JsonIO jsonIO = new TerrainIO();
        return (Terrain) jsonIO.read(filePath);
    }

    /**
     * Read background music config
     */
    public static SoundFile createBackgroundMusicFromConfig(String filePath, PApplet applet) throws IOException {
        JsonIO jsonIO = new BackgroundMusicIO(applet);
        return (SoundFile) jsonIO.read(filePath);
    }

    /**
     * Read AI config
     */
    public static PoliticalFaction readPoliticalFactionFromConfig(String filePath) throws IOException {
        JsonIO jsonIO = new AIAgentIO();
        return (PoliticalFaction) jsonIO.read(filePath);
    }

    /**
     * Read city state parameter
     */
    public static CityObjects readCityStateParameters(String filePath) throws IOException {
        JsonIO jsonIO = new CityStateParamsIO();
        return (CityObjects) jsonIO.read(filePath);
    }
}

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
        // Get all text from file location
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String s = new String(encoded, StandardCharsets.UTF_8);
        // The first object will be the map creation parameter.
        String[] objects = s.split(",");

        // Each of the next object will be a construct with a boundary.
        ArrayList<Construct> constructs = new ArrayList<>();
        Graph graph = null;
        for (int i = 0; i < objects.length; i++) {
            String[] infoLines = objects[i].split("\n");
            String type = "";
            String name = "";
            ArrayList<double[]> pts = new ArrayList<>();
            HashMap<Integer, double[]> nodes = new HashMap<>();
            ArrayList<int[]> edges = new ArrayList<>();
            for (int j = 0; j < infoLines.length; j++) {
                String line = infoLines[j];
                String[] data = line.split(":");
                String fieldName = data[0].trim();
                if (data.length < 2) continue;
                if (fieldName.equals("type")) {
                    type = data[1].trim();
                }
                if (type.equals("construct")) {
                    if (fieldName.equals("name")) {
                        name = data[1].trim();
                    } else if (fieldName.equals("boundary_points")) {
                        String[] ptData = data[1].trim().split(" ");
                        double[] pt = new double[]{Double.valueOf(ptData[0]), Double.valueOf(ptData[1])};
                        pts.add(pt);
                    }
                } else if (type.equals("graph")) {
                    if (fieldName.equals("node")) {
                        String[] nodeData = data[1].trim().split(" ");
                        int nodeId = Integer.valueOf(nodeData[0].trim());
                        double[] pt = new double[]{Double.valueOf(nodeData[1].trim()), Double.valueOf(nodeData[2].trim())};
                        nodes.put(nodeId, pt);
                    } else if (fieldName.equals("edge")) {
                        String[] edgeData = data[1].trim().split(" ");
                        int[] edge = new int[]{Integer.valueOf(edgeData[0]), Integer.valueOf(edgeData[1])};
                        edges.add(edge);
                    }
                }
            }
            if (type.equals("construct")) {
                constructs.add(new Construct(name, pts));
            } else if (type.equals("graph")) {
                graph = new Graph(nodes, edges);
            }
        }
        return new Pair<>(graph, constructs);
    }

    /**
     * Create constructs from config file.
     */
    public static ArrayList<BaseSurface> createSurfacesFromConfig(String filePath) throws IOException {
        ArrayList<BaseSurface> surfaces = new ArrayList<>();

        // Get all text from file location
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String s = new String(encoded, StandardCharsets.UTF_8);
        if (s.equals("")) {
            return surfaces;
        }

        // The first object will be the map creation parameter.
        String[] objects = s.split(",");

        // Each of the next object will be a construct with a boundary.
        for (int i = 0; i < objects.length; i++) {
            String[] infoLines = objects[i].split("\n");
            SurfaceType type = null;
            ArrayList<double[]> pts = new ArrayList<>();
            double averageTreeRadius = 0.0;
            double sizeWiggling = 0.0;
            double averageDistance = 0.0;
            double distanceWiggling = 0.0;
            for (int j = 0; j < infoLines.length; j++) {
                String line = infoLines[j];
                String[] data = line.split(":");
                String fieldName = data[0].trim();
                if (data.length < 2) continue;
                if (fieldName.equals("type")) {
                    type = SurfaceType.valueOf(data[1].trim());
                } else if (fieldName.equals("boundary_points")) {
                    String[] ptData = data[1].trim().split(" ");
                    double[] pt = new double[]{Double.valueOf(ptData[0]), Double.valueOf(ptData[1])};
                    pts.add(pt);
                } else if (fieldName.equals("average_tree_radius")) {
                    averageTreeRadius = Double.valueOf(data[1].trim());
                } else if (fieldName.equals("size_wiggling")) {
                    sizeWiggling = Double.valueOf(data[1].trim());
                } else if (fieldName.equals("average_distance")) {
                    averageDistance = Double.valueOf(data[1].trim());
                } else if (fieldName.equals("distance_wiggling")) {
                    distanceWiggling = Double.valueOf(data[1].trim());
                }
            }

            // Based on the surface type, create the surface and add to the surface array.
            BaseSurface surface = null;
            switch (type) {
                case SNOW:
                    surface = new SnowSurface(type, pts);
                    break;
                case BEACH:
                    surface = new BeachSurface(type, pts);
                    break;
                case MARSH:
                    surface = new MarshSurface(type, pts);
                    break;
                case DESERT:
                    surface = new DesertSurface(type, pts);
                    break;
                case FOREST:
                    surface = new ForestSurface(
                            type, pts, averageTreeRadius, sizeWiggling, averageDistance, distanceWiggling);
                    break;
                case RIVERSIDE:
                    surface = new RiversideSurface(type, pts);
                    break;
                case SHALLOW_RIVER:
                    surface = new ShallowRiverSurface(type, pts);
                    break;
                default:
                    break;
            }
            if (surface != null) surfaces.add(surface);
        }
        return surfaces;
    }

    /**
     * Read audio configs
     * @param filePath
     * @param camera
     * @param applet
     * @param eventBroadcaster
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

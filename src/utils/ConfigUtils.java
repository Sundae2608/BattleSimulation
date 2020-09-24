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
import utils.json.AudioSpeakerIO;
import utils.json.JsonIO;
import utils.json.TerrainIO;
import utils.json.VideoElementPlayerIO;
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
     * This helper function parse a string proto that is in the following form:
     * {
     *     key: value
     * }
     * @param s the input string.
     * @return a hash map containing key and value
     */
    private static HashMap<String, String> parseProtoString(String s) {
        // Read all data of the audio config first
        String[] infoLines = s.split("\n");
        HashMap<String, String> d = new HashMap<>();
        for (String line : infoLines) {
            line = line.trim();
            String[] data = line.split(":");
            if (data.length < 2) continue;
            String key = data[0].trim();
            String value = data[1].trim();
            d.put(key, value);
        }
        return d;
    }

    /**
     * Read the battle config, which defines the position and size of each unit that participate in the games.
     * @param filePath Path leading to the config
     * @param hasher ObjectHasher object. This is required so that certain units such as Archer or HorseArcher can have
     *               arrows interact with the environment.
     * @return A list of units that participates in the battle.
     * @throws IOException if the read fails.
     */
    public static ArrayList<BaseUnit> readBattleConfigs(String filePath, GameStats gameStats, ObjectHasher hasher, Terrain terrain, EventBroadcaster broadcaster, GameSettings gameSettings, GameEnvironment env) throws IOException {

        // Get all text from file location
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String s = new String(encoded, StandardCharsets.UTF_8);
        String[] unitsInfo = s.split(",");

        // Read objects from string
        ArrayList<BaseUnit> units = new ArrayList<>();
        for (String info : unitsInfo) {

            // Read all data of the unit first
            String[] infoLines = info.split("\n");
            HashMap<String, String> d = new HashMap<>();
            for (String line : infoLines) {
                line = line.trim();
                String[] data = line.split(":");
                if (data.length < 2) continue;
                String key = data[0].trim();
                String value = data[1].trim();
                d.put(key, value);
            }

            // Now, create objects with those value
            double x = Double.valueOf(d.get("x"));
            double y = Double.valueOf(d.get("y"));
            double angle = MathUtils.toRadians(Double.valueOf(d.get("angle")));
            int unitSize = Integer.valueOf(d.get("size"));
            PoliticalFaction faction = PoliticalFaction.valueOf(d.get("faction"));
            int unitWidth = Integer.valueOf(d.get("width"));
            UnitType unitType = UnitType.valueOf(d.get("type"));
            SingleStats singleStats = gameStats.getSingleStats(unitType, faction);
            UnitStats unitStats = gameStats.getUnitStats(unitType, faction);
            BaseUnit unit = null;
            switch (unitType) {
                case PHALANX:
                    unit = new PhalanxUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, env);
                    break;
                case SKIRMISHER:
                    unit = new SkirmisherUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, env);
                    break;
                case ARCHER:
                    unit = new ArcherUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, env);
                    break;
                case BALLISTA:
                    unit = new BallistaUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, env);
                    break;
                case CATAPULT:
                    unit = new CatapultUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, env);
                    break;
                case SLINGER:
                    unit = new SlingerUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, env);
                    break;
                case CAVALRY:
                    unit = new CavalryUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, env);
                    break;
                case SWORDMAN:
                    unit = new SwordmenUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, env);
                    break;
            }
            if (unit != null) {
                units.add(unit);
            }
        }
        return units;
    }

    /**
     * Read the game statistics, which defines statistics of each unit that participates in the game.
     * @param filePath path to file that contains the battle config.
     * @return GameStats object which contains the unit statistics
     * @throws IOException if the read fails.
     */
    public static GameStats readGameStats(String filePath) throws IOException {
        // Get all text from file location
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String s = new String(encoded, StandardCharsets.UTF_8);
        String[] unitsInfo = s.split(",");

        // Read each information
        GameStats gameStats = new GameStats();
        for (String info : unitsInfo) {

            // Read all data of the unit first
            String[] infoLines = info.split("\n");
            HashMap<String, String> d = new HashMap<>();
            for (String line : infoLines) {
                line = line.trim();
                String[] data = line.split(":");
                if (data.length < 2) continue;
                String key = data[0].trim();
                String value = data[1].trim();
                d.put(key, value);
            }

            // Create the stats for each unit type based on read data.
            PoliticalFaction faction = PoliticalFaction.valueOf(d.get("faction"));
            UnitType unitType = UnitType.valueOf(d.get("unit_type"));

            SingleStats singleStats = new SingleStats();
            singleStats.mass = Double.parseDouble(d.get("mass"));
            singleStats.radius = Double.parseDouble(d.get("radius"));
            singleStats.collisionRadius = UniversalConstants.PUSH_SIZE_MULTIPIER * singleStats.radius / 2;
            singleStats.speed = Double.parseDouble(d.get("speed"));
            singleStats.hp = Double.parseDouble(d.get("hp"));
            singleStats.deceleration = Double.parseDouble(d.get("deceleration"));
            singleStats.acceleration = Double.parseDouble(d.get("acceleration"));
            singleStats.rotationSpeed = Double.parseDouble(d.get("rotation_speed"));
            singleStats.outOfReachDist = UniversalConstants.OUT_OF_REACH_NUM_STEP * singleStats.speed;
            singleStats.outOfReachSpeed = UniversalConstants.OUT_OF_REACH_SPEED_MULTIPLIER * singleStats.speed;
            singleStats.standingDist = Double.parseDouble(d.get("standing_dist"));
            singleStats.nonRotationDist = Double.parseDouble(d.get("non_rotation_dist"));
            singleStats.attack = Double.parseDouble(d.get("attack"));
            singleStats.defense = Double.parseDouble(d.get("defense"));
            singleStats.combatRange = Double.parseDouble(d.get("combat_range"));
            singleStats.combatDelay = Integer.parseInt(d.get("combat_delay"));
            singleStats.sustainRecovery = Double.parseDouble(d.get("sustain_recovery"));
            switch (unitType) {
                case ARCHER:
                    singleStats.reloadDelay = Integer.parseInt(d.get("reload_delay"));
                    singleStats.boredDelay = Integer.parseInt(d.get("bored_delay"));
                    singleStats.angleVariation = Double.parseDouble(d.get("angle_variation"));
                    singleStats.firingRange = Double.parseDouble(d.get("firing_range"));
                    singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                    singleStats.impactLifetime = Integer.parseInt(d.get("impact_lifetime"));
                    singleStats.arrowSpeed = Double.parseDouble(d.get("arrow_speed"));
                    singleStats.arrowSize = Double.parseDouble(d.get("arrow_size"));
                    singleStats.arrowDamage = Double.parseDouble(d.get("arrow_damage"));
                    singleStats.arrowPushDist = Double.parseDouble(d.get("arrow_push_dist"));
                    break;
                case BALLISTA:
                    singleStats.reloadDelay = Integer.parseInt(d.get("reload_delay"));
                    singleStats.boredDelay = Integer.parseInt(d.get("bored_delay"));
                    singleStats.angleVariation = Double.parseDouble(d.get("angle_variation"));
                    singleStats.firingRange = Double.parseDouble(d.get("firing_range"));
                    singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                    singleStats.impactLifetime = Integer.parseInt(d.get("impact_lifetime"));
                    singleStats.ballistaSpeed = Double.parseDouble(d.get("ballista_speed"));
                    singleStats.ballistaDamage = Double.parseDouble(d.get("ballista_damage"));
                    singleStats.ballistaExplosionDamage = Double.parseDouble(d.get("ballista_explosion_damage"));
                    singleStats.ballistaExplosionPush = Double.parseDouble(d.get("ballista_explosion_push"));
                    singleStats.ballistaExplosionRange = Double.parseDouble(d.get("ballista_explosion_range"));
                    singleStats.ballistaPushForce = Double.parseDouble(d.get("ballista_push_force"));
                    break;
                case CATAPULT:
                    singleStats.reloadDelay = Integer.parseInt(d.get("reload_delay"));
                    singleStats.boredDelay = Integer.parseInt(d.get("bored_delay"));
                    singleStats.angleVariation = Double.parseDouble(d.get("angle_variation"));
                    singleStats.firingRange = Double.parseDouble(d.get("firing_range"));
                    singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                    singleStats.catapultSpeed = Double.parseDouble(d.get("catapult_speed"));
                    singleStats.catapultDamage = Double.parseDouble(d.get("catapult_damage"));
                    singleStats.catapultExplosionDamage = Double.parseDouble(d.get("catapult_explosion_damage"));
                    singleStats.catapultExplosionPush = Double.parseDouble(d.get("catapult_explosion_push"));
                    singleStats.catapultExplosionRange = Double.parseDouble(d.get("catapult_explosion_range"));
                    break;
                case CAVALRY:
                case HORSE_ARCHER:
                case PHALANX:
                case SKIRMISHER:
                case SLINGER:
                case SWORDMAN:
                    break;
            }

            // Additional stats based on the unit type.
            UnitStats unitStats = new UnitStats();
            unitStats.unitType = unitType;
            switch (unitType) {
                case SLINGER:
                case ARCHER:
                    unitStats.widthVariation = Double.parseDouble(d.get("width_variation"));
                    unitStats.depthVariation = Double.parseDouble(d.get("depth_variation"));
                    break;
                case CAVALRY:
                case HORSE_ARCHER:
                case SKIRMISHER:
                case SWORDMAN:
                    break;
                case PHALANX:
                    unitStats.numFirstRows = Integer.parseInt(d.get("num_first_rows"));
                    unitStats.offAngleFirstRow = Double.parseDouble(d.get("off_angle_first_row"));
                    break;
            }

            unitStats.spacing = Double.parseDouble(d.get("unit_spacing"));
            unitStats.speed = Double.parseDouble(d.get("unit_speed"));
            unitStats.rotationSpeed = Double.parseDouble(d.get("unit_rotation_speed"));
            unitStats.patience = Integer.parseInt(d.get("unit_patience"));
            if (d.containsKey("unit_max_stamina")) {
                unitStats.staminaStats.maxStamina = Double.parseDouble(d.get("unit_max_stamina"));
            }
            if (d.containsKey("unit_stamina_decelerating_rate")) {
                unitStats.staminaStats.staminaDeceleratingChangeRate = Double.parseDouble(d.get("unit_stamina_decelerating_rate"));
            }
            if (d.containsKey("unit_stamina_fighting_rate")) {
                unitStats.staminaStats.staminaFightingChangeRate = Double.parseDouble(d.get("unit_stamina_fighting_rate"));
            }
            if (d.containsKey("unit_stamina_moving_rate")) {
                unitStats.staminaStats.staminaMovingChangeRate = Double.parseDouble(d.get("unit_stamina_moving_rate"));
            }
            if (d.containsKey("unit_stamina_routing_rate")) {
                unitStats.staminaStats.staminaRoutingChangeRate = Double.parseDouble(d.get("unit_stamina_routing_rate"));
            }
            if (d.containsKey("unit_stamina_standing_rate")) {
                unitStats.staminaStats.staminaStandingChangeRate = Double.parseDouble(d.get("unit_stamina_standing_rate"));
            }

            // Add SingleStats and UnitStats to GameStats
            gameStats.addSingleStats(unitType, faction, singleStats);
            gameStats.addUnitStats(unitType, faction, unitStats);
        }
        return gameStats;
    }

    /**
     * Read audio configs
     * @param filePath
     * @param applet
     * @return An audio broadcaster with the configs.
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

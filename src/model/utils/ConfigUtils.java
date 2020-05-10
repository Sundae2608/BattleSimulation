package model.utils;

import model.GameStats;
import model.algorithms.ObjectHasher;
import model.constants.UniversalConstants;
import model.enums.PoliticalFaction;
import model.enums.UnitType;
import model.singles.SingleStats;
import model.terrain.Terrain;
import model.units.*;
import model.units.unit_stats.UnitStats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public final class ConfigUtils {
    /**
     * Read the battle config, which defines the position and size of each unit that participate in the games.
     * @param filePath Path leading to the config
     * @param hasher ObjectHasher object. This is required so that certain units such as Archer or HorseArcher can have
     *               arrows interact with the environment.
     * @return A list of units that participates in the battle.
     * @throws IOException if the read fails.
     */
    public static ArrayList<BaseUnit> readBattleConfigs(String filePath, GameStats gameStats, ObjectHasher hasher, Terrain terrain) throws IOException {

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
            BaseUnit unit;
            switch (unitType) {
                case PHALANX:
                    unit = new PhalanxUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, terrain);
                    break;
                case SKIRMISHER:
                    unit = new SkirmisherUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, terrain);
                    break;
                case ARCHER:
                    unit = new ArcherUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, terrain);
                    break;
                case BALISTA:
                    unit = new BalistaUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, terrain);
                    break;
                case SLINGER:
                    unit = new SlingerUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, terrain);
                    break;
                case CAVALRY:
                    unit = new CavalryUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, terrain);
                    break;
                case SWORDMAN:
                default:
                    unit = new SwordmenUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, terrain);
                    break;
            }
            units.add(unit);
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
            singleStats.combatRange = Double.parseDouble(d.get("combat_range"));
            singleStats.combatDelay = Integer.parseInt(d.get("combat_delay"));
            singleStats.sustainRecovery = Double.parseDouble(d.get("sustain_recovery"));
            switch (unitType) {
                case ARCHER:
                    singleStats.reloadDelay = Integer.parseInt(d.get("reload_delay"));
                    singleStats.boredDelay = Integer.parseInt(d.get("bored_delay"));
                    singleStats.arrowSpeed = Double.parseDouble(d.get("arrow_speed"));
                    singleStats.angleVariation = Double.parseDouble(d.get("angle_variation"));
                    singleStats.firingRange = Double.parseDouble(d.get("firing_range"));
                    singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                    singleStats.impactLifetime = Integer.parseInt(d.get("impact_lifetime"));
                    singleStats.arrowSize = Double.parseDouble(d.get("arrow_size"));
                    singleStats.arrowDamage = Double.parseDouble(d.get("arrow_damage"));
                    break;
                case BALISTA:
                    singleStats.reloadDelay = Integer.parseInt(d.get("reload_delay"));
                    singleStats.boredDelay = Integer.parseInt(d.get("bored_delay"));
                    singleStats.angleVariation = Double.parseDouble(d.get("angle_variation"));
                    singleStats.firingRange = Double.parseDouble(d.get("firing_range"));
                    singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                    singleStats.impactLifetime = Integer.parseInt(d.get("impact_lifetime"));
                    singleStats.balistaSpeed = Double.parseDouble(d.get("balista_speed"));
                    singleStats.balistaDamage = Double.parseDouble(d.get("balista_damage"));
                    singleStats.explosionDamage = Double.parseDouble(d.get("explosion_damage"));
                    singleStats.explosionPush = Double.parseDouble(d.get("explosion_push"));
                    singleStats.explosionRange = Double.parseDouble(d.get("explosion_range"));
                    break;
                case CAVALRY:
                case HORSE_ARCHER:
                case PHALANX:
                case SKIRMISHER:
                case SLINGER:
                case SWORDMAN:
                default:
                    break;
            }

            // Additional stats based on the unit type.
            UnitStats unitStats = new UnitStats();
            switch (unitType) {
                case SLINGER:
                case ARCHER:
                    unitStats.widthVariation = Double.parseDouble(d.get("width_variation"));
                    unitStats.depthVariation = Double.parseDouble(d.get("depth_variation"));
                    break;
                case CAVALRY:
                case HORSE_ARCHER:
                    break;
                case PHALANX:
                    unitStats.numFirstRows = Integer.parseInt(d.get("num_first_rows"));
                    unitStats.offAngleFirstRow = Double.parseDouble(d.get("off_angle_first_row"));
                    break;
                case SKIRMISHER:
                case SWORDMAN:
                default:
                    break;
            }
            unitStats.spacing = Double.parseDouble(d.get("unit_spacing"));
            unitStats.speed = Double.parseDouble(d.get("unit_speed"));
            unitStats.rotationSpeed = Double.parseDouble(d.get("unit_rotation_speed"));
            unitStats.patience = Integer.parseInt(d.get("unit_patience"));

            // Add SingleStats and UnitStats to GameStats
            gameStats.addSingleStats(unitType, faction, singleStats);
            gameStats.addUnitStats(unitType, faction, unitStats);
        }
        return gameStats;
    }

    /**
     * Create a terrain based on configs input from a file.
     * @param filePath path that contains the input configs.
     * @return a terrain generated from the config.
     * @throws IOException if the file is unavailable.
     */
    public static Terrain createTerrainFromConfig(String filePath) throws IOException {
        // Get all text from file location
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String s = new String(encoded, StandardCharsets.UTF_8);

        // Read all data of the config first
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

        // Extract the input configs
        double topX = Double.parseDouble(d.get("top_x"));
        double topY = Double.parseDouble(d.get("top_x"));
        double div = Double.parseDouble(d.get("div"));
        int numX = Integer.parseInt(d.get("num_x"));
        int numY = Integer.parseInt(d.get("num_y"));
        int taper = Integer.parseInt(d.get("taper"));
        double minHeight = Double.parseDouble(d.get("min_height"));
        double maxHeight = Double.parseDouble(d.get("max_height"));

        // Return terrain from the input configs
        return new Terrain(
            topX, topY, div, numX, numY, taper, minHeight, maxHeight
        );
    }
}

package utils;

import algorithms.ObjectHasher;
import units.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public final class ConfigUtils {
    /**
     * Create battle oonfiguration from config file
     * @param filePath Path leading to the config
     * @param hasher ObjectHasher object. This is required so that certain units such as Archer or HorseArcher can shoot
     *               arrows.
     */
    public static ArrayList<BaseUnit> readConfigs(String filePath, ObjectHasher hasher) throws IOException {

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

            BaseUnit unit;
            switch (d.get("type")) {
                case "swordmen":
                    unit = new SwordmenUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
                case "phalanx":
                    unit = new PhalanxUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
                case "skirmisher":
                    unit = new SkirmisherUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
                case "archer":
                    unit = new ArcherUnit(x, y, angle, unitSize, faction, unitWidth, hasher);
                    break;
                case "slinger":
                    unit = new SlingerUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
                case "cavalry":
                    unit = new CavalryUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
                case "horsearcher":
                    unit = new HorseArcherUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
                default:
                    unit = new SwordmenUnit(x, y, angle, unitSize, faction, unitWidth);
                    break;
            }
            units.add(unit);
        }
        return units;
    }
}

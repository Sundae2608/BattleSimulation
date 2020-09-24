package utils.json;

import model.GameEnvironment;
import model.GameStats;
import model.algorithms.ObjectHasher;
import model.enums.PoliticalFaction;
import model.enums.UnitType;
import model.events.EventBroadcaster;
import model.settings.GameSettings;
import model.singles.SingleStats;
import model.terrain.Terrain;
import model.units.*;
import model.units.unit_stats.UnitStats;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BattleUnitsIO implements JsonIO<ArrayList<BaseUnit>> {

    private GameStats gameStats;
    private ObjectHasher hasher;
    private Terrain terrain;
    private EventBroadcaster broadcaster;
    private GameSettings gameSettings;
    private GameEnvironment environment;

    public BattleUnitsIO(GameStats gameStats, ObjectHasher hasher, Terrain terrain, EventBroadcaster broadcaster,
                         GameSettings gameSettings, GameEnvironment environment) {
        this.gameStats = gameStats;
        this.hasher = hasher;
        this.terrain = terrain;
        this.broadcaster = broadcaster;
        this.gameSettings = gameSettings;
        this.environment = environment;
    }

    @Override
    public ArrayList<BaseUnit> read(String filePath) {

        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("battle_config");

        // Read each unit config and create the unit
        ArrayList<BaseUnit> units = new ArrayList<>();
        for(Object obj : jsonArray){
            JSONObject unitObject;
            if (obj instanceof JSONObject) {
                unitObject = (JSONObject)obj;
                double x = (Double) unitObject.get("x");
                double y = (Double) unitObject.get("y");
                double angle = (Double) unitObject.get("angle");
                int unitSize = (Integer) unitObject.get("size");
                PoliticalFaction faction = PoliticalFaction.valueOf((String) unitObject.get("faction"));
                int unitWidth = (Integer) unitObject.get("width");
                UnitType unitType = UnitType.valueOf((String) unitObject.get("type"));

                SingleStats singleStats = gameStats.getSingleStats(unitType, faction);
                UnitStats unitStats = gameStats.getUnitStats(unitType, faction);

                BaseUnit unit = null;
                switch (unitType) {
                    case PHALANX:
                        unit = new PhalanxUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, environment);
                        break;
                    case SKIRMISHER:
                        unit = new SkirmisherUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, environment);
                        break;
                    case ARCHER:
                        unit = new ArcherUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, environment);
                        break;
                    case BALLISTA:
                        unit = new BallistaUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, environment);
                        break;
                    case CATAPULT:
                        unit = new CatapultUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, hasher, environment);
                        break;
                    case SLINGER:
                        unit = new SlingerUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, environment);
                        break;
                    case CAVALRY:
                        unit = new CavalryUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, environment);
                        break;
                    case SWORDMAN:
                        unit = new SwordmenUnit(x, y, angle, unitSize, faction, unitStats, singleStats, unitWidth, environment);
                        break;
                }
                if (unit != null) {
                    units.add(unit);
                }
            }
        }
        return units;
    }

    @Override
    public void save(ArrayList<BaseUnit> data, String filePath) throws IOException {

    }
}

package utils.json;

import model.GameStats;
import model.constants.UniversalConstants;
import model.enums.PoliticalFaction;
import model.enums.UnitType;
import model.singles.SingleStats;
import model.units.unit_stats.UnitStats;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class GameStatsIO implements JsonIO<GameStats> {
    @Override
    public GameStats read(String filePath) throws IOException {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("game_config");

        // Read each unit config and create the unit
        GameStats gameStats = new GameStats();
        for(Object obj : jsonArray){
            JSONObject statsObject;
            if (obj instanceof JSONObject) {
                statsObject = (JSONObject)obj;

                // Create the stats for each unit type based on read data.
                PoliticalFaction faction = PoliticalFaction.valueOf((String) statsObject.get("faction"));
                UnitType unitType = UnitType.valueOf((String) statsObject.get("unit_type"));

                SingleStats singleStats = new SingleStats();
                singleStats.mass = Double.parseDouble((String) statsObject.get("mass"));
                singleStats.radius = Double.parseDouble((String) statsObject.get("radius"));
                singleStats.collisionRadius = UniversalConstants.PUSH_SIZE_MULTIPIER * singleStats.radius / 2;
                singleStats.speed = Double.parseDouble((String) statsObject.get("speed"));
                singleStats.hp = Double.parseDouble((String) statsObject.get("hp"));
                singleStats.deceleration = Double.parseDouble((String) statsObject.get("deceleration"));
                singleStats.acceleration = Double.parseDouble((String) statsObject.get("acceleration"));
                singleStats.rotationSpeed = Double.parseDouble((String) statsObject.get("rotation_speed"));
                singleStats.outOfReachDist = UniversalConstants.OUT_OF_REACH_NUM_STEP * singleStats.speed;
                singleStats.outOfReachSpeed = UniversalConstants.OUT_OF_REACH_SPEED_MULTIPLIER * singleStats.speed;
                singleStats.standingDist = Double.parseDouble((String) statsObject.get("standing_dist"));
                singleStats.nonRotationDist = Double.parseDouble((String) statsObject.get("non_rotation_dist"));
                singleStats.attack = Double.parseDouble((String) statsObject.get("attack"));
                singleStats.defense = Double.parseDouble((String) statsObject.get("defense"));
                singleStats.combatRange = Double.parseDouble((String) statsObject.get("combat_range"));
                singleStats.combatDelay = Integer.parseInt((String) statsObject.get("combat_delay"));
                singleStats.sustainRecovery = Double.parseDouble((String) statsObject.get("sustain_recovery"));
                switch (unitType) {
                    case ARCHER:
                        singleStats.reloadDelay = Integer.parseInt((String) statsObject.get("reload_delay"));
                        singleStats.boredDelay = Integer.parseInt((String) statsObject.get("bored_delay"));
                        singleStats.angleVariation = Double.parseDouble((String) statsObject.get("angle_variation"));
                        singleStats.firingRange = Double.parseDouble((String) statsObject.get("firing_range"));
                        singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                        singleStats.impactLifetime = Integer.parseInt((String) statsObject.get("impact_lifetime"));
                        singleStats.arrowSpeed = Double.parseDouble((String) statsObject.get("arrow_speed"));
                        singleStats.arrowSize = Double.parseDouble((String) statsObject.get("arrow_size"));
                        singleStats.arrowDamage = Double.parseDouble((String) statsObject.get("arrow_damage"));
                        singleStats.arrowPushDist = Double.parseDouble((String) statsObject.get("arrow_push_dist"));
                        break;
                    case BALLISTA:
                        singleStats.reloadDelay = Integer.parseInt((String) statsObject.get("reload_delay"));
                        singleStats.boredDelay = Integer.parseInt((String) statsObject.get("bored_delay"));
                        singleStats.angleVariation = Double.parseDouble((String) statsObject.get("angle_variation"));
                        singleStats.firingRange = Double.parseDouble((String) statsObject.get("firing_range"));
                        singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                        singleStats.impactLifetime = Integer.parseInt((String) statsObject.get("impact_lifetime"));
                        singleStats.ballistaSpeed = Double.parseDouble((String) statsObject.get("ballista_speed"));
                        singleStats.ballistaDamage = Double.parseDouble((String) statsObject.get("ballista_damage"));
                        singleStats.ballistaExplosionDamage = Double.parseDouble((String) statsObject.get("ballista_explosion_damage"));
                        singleStats.ballistaExplosionPush = Double.parseDouble((String) statsObject.get("ballista_explosion_push"));
                        singleStats.ballistaExplosionRange = Double.parseDouble((String) statsObject.get("ballista_explosion_range"));
                        singleStats.ballistaPushForce = Double.parseDouble((String) statsObject.get("ballista_push_force"));
                        break;
                    case CATAPULT:
                        singleStats.reloadDelay = Integer.parseInt((String) statsObject.get("reload_delay"));
                        singleStats.boredDelay = Integer.parseInt((String) statsObject.get("bored_delay"));
                        singleStats.angleVariation = Double.parseDouble((String) statsObject.get("angle_variation"));
                        singleStats.firingRange = Double.parseDouble((String) statsObject.get("firing_range"));
                        singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                        singleStats.catapultSpeed = Double.parseDouble((String) statsObject.get("catapult_speed"));
                        singleStats.catapultDamage = Double.parseDouble((String) statsObject.get("catapult_damage"));
                        singleStats.catapultExplosionDamage = Double.parseDouble((String) statsObject.get("catapult_explosion_damage"));
                        singleStats.catapultExplosionPush = Double.parseDouble((String) statsObject.get("catapult_explosion_push"));
                        singleStats.catapultExplosionRange = Double.parseDouble((String) statsObject.get("catapult_explosion_range"));
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
                        unitStats.widthVariation = Double.parseDouble((String) statsObject.get("width_variation"));
                        unitStats.depthVariation = Double.parseDouble((String) statsObject.get("depth_variation"));
                        break;
                    case CAVALRY:
                    case HORSE_ARCHER:
                    case SKIRMISHER:
                    case SWORDMAN:
                        break;
                    case PHALANX:
                        unitStats.numFirstRows = Integer.parseInt((String) statsObject.get("num_first_rows"));
                        unitStats.offAngleFirstRow = Double.parseDouble((String) statsObject.get("off_angle_first_row"));
                        break;
                }

                unitStats.spacing = Double.parseDouble((String) statsObject.get("unit_spacing"));
                unitStats.speed = Double.parseDouble((String) statsObject.get("unit_speed"));
                unitStats.rotationSpeed = Double.parseDouble((String) statsObject.get("unit_rotation_speed"));
                unitStats.patience = Integer.parseInt((String) statsObject.get("unit_patience"));
                if (statsObject.containsKey("unit_max_stamina")) {
                    unitStats.staminaStats.maxStamina = Double.parseDouble((String) statsObject.get("unit_max_stamina"));
                }
                if (statsObject.containsKey("unit_stamina_decelerating_rate")) {
                    unitStats.staminaStats.staminaDeceleratingChangeRate = Double.parseDouble((String) statsObject.get("unit_stamina_decelerating_rate"));
                }
                if (statsObject.containsKey("unit_stamina_fighting_rate")) {
                    unitStats.staminaStats.staminaFightingChangeRate = Double.parseDouble((String) statsObject.get("unit_stamina_fighting_rate"));
                }
                if (statsObject.containsKey("unit_stamina_moving_rate")) {
                    unitStats.staminaStats.staminaMovingChangeRate = Double.parseDouble((String) statsObject.get("unit_stamina_moving_rate"));
                }
                if (statsObject.containsKey("unit_stamina_routing_rate")) {
                    unitStats.staminaStats.staminaRoutingChangeRate = Double.parseDouble((String) statsObject.get("unit_stamina_routing_rate"));
                }
                if (statsObject.containsKey("unit_stamina_standing_rate")) {
                    unitStats.staminaStats.staminaStandingChangeRate = Double.parseDouble((String) statsObject.get("unit_stamina_standing_rate"));
                }

                // Add SingleStats and UnitStats to GameStats
                gameStats.addSingleStats(unitType, faction, singleStats);
                gameStats.addUnitStats(unitType, faction, unitStats);
            }
        }
        return gameStats;
    }

    @Override
    public void save(GameStats data, String filePath) throws IOException {

    }
}

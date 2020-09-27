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

public class GameStatsIO extends JsonIO<GameStats> {
    @Override
    public GameStats read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("game_config");

        // Read each unit config and create the unit
        GameStats gameStats = new GameStats();
        for (Object obj : jsonArray) {
            JSONObject statsObject;
            if (obj instanceof JSONObject) {
                statsObject = (JSONObject) obj;

                // Create the stats for each unit type based on read data.
                PoliticalFaction faction = PoliticalFaction.valueOf((String) statsObject.get("faction"));
                UnitType unitType = UnitType.valueOf((String) statsObject.get("unit_type"));

                SingleStats singleStats = new SingleStats();
                singleStats.mass = getDouble(statsObject.get("mass"));
                singleStats.radius = getDouble(statsObject.get("radius"));
                singleStats.collisionRadius = UniversalConstants.PUSH_SIZE_MULTIPIER * singleStats.radius / 2;
                singleStats.speed = getDouble(statsObject.get("speed"));
                singleStats.hp = getDouble(statsObject.get("hp"));
                singleStats.deceleration = getDouble(statsObject.get("deceleration"));
                singleStats.acceleration = getDouble(statsObject.get("acceleration"));
                singleStats.rotationSpeed = getDouble(statsObject.get("rotation_speed"));
                singleStats.outOfReachDist = UniversalConstants.OUT_OF_REACH_NUM_STEP * singleStats.speed;
                singleStats.outOfReachSpeed = UniversalConstants.OUT_OF_REACH_SPEED_MULTIPLIER * singleStats.speed;
                singleStats.standingDist = getDouble(statsObject.get("standing_dist"));
                singleStats.nonRotationDist = getDouble(statsObject.get("non_rotation_dist"));
                singleStats.attack = getDouble(statsObject.get("attack"));
                singleStats.defense = getDouble(statsObject.get("defense"));
                singleStats.combatRange = getDouble(statsObject.get("combat_range"));
                singleStats.combatDelay = getInt(statsObject.get("combat_delay"));
                singleStats.sustainRecovery = getDouble(statsObject.get("sustain_recovery"));
                switch (unitType) {
                    case ARCHER:
                        singleStats.reloadDelay = getInt(statsObject.get("reload_delay"));
                        singleStats.boredDelay = getInt(statsObject.get("bored_delay"));
                        singleStats.angleVariation = getDouble(statsObject.get("angle_variation"));
                        singleStats.firingRange = getDouble(statsObject.get("firing_range"));
                        singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                        singleStats.impactLifetime = getInt(statsObject.get("impact_lifetime"));
                        singleStats.arrowSpeed = getDouble(statsObject.get("arrow_speed"));
                        singleStats.arrowSize = getDouble(statsObject.get("arrow_size"));
                        singleStats.arrowDamage = getDouble(statsObject.get("arrow_damage"));
                        singleStats.arrowPushDist = getDouble(statsObject.get("arrow_push_dist"));
                        break;
                    case BALLISTA:
                        singleStats.reloadDelay = getInt(statsObject.get("reload_delay"));
                        singleStats.boredDelay = getInt(statsObject.get("bored_delay"));
                        singleStats.angleVariation = getDouble(statsObject.get("angle_variation"));
                        singleStats.firingRange = getDouble(statsObject.get("firing_range"));
                        singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                        singleStats.impactLifetime = getInt(statsObject.get("impact_lifetime"));
                        singleStats.ballistaSpeed = getDouble(statsObject.get("ballista_speed"));
                        singleStats.ballistaDamage = getDouble(statsObject.get("ballista_damage"));
                        singleStats.ballistaExplosionDamage = getDouble(statsObject.get("ballista_explosion_damage"));
                        singleStats.ballistaExplosionPush = getDouble(statsObject.get("ballista_explosion_push"));
                        singleStats.ballistaExplosionRange = getDouble(statsObject.get("ballista_explosion_range"));
                        singleStats.ballistaPushForce = getDouble(statsObject.get("ballista_push_force"));
                        break;
                    case CATAPULT:
                        singleStats.reloadDelay = getInt(statsObject.get("reload_delay"));
                        singleStats.boredDelay = getInt(statsObject.get("bored_delay"));
                        singleStats.angleVariation = getDouble(statsObject.get("angle_variation"));
                        singleStats.firingRange = getDouble(statsObject.get("firing_range"));
                        singleStats.squaredFiringRange = singleStats.firingRange * singleStats.firingRange;
                        singleStats.catapultSpeed = getDouble(statsObject.get("catapult_speed"));
                        singleStats.catapultDamage = getDouble(statsObject.get("catapult_damage"));
                        singleStats.catapultExplosionDamage = getDouble(statsObject.get("catapult_explosion_damage"));
                        singleStats.catapultExplosionPush = getDouble(statsObject.get("catapult_explosion_push"));
                        singleStats.catapultExplosionRange = getDouble(statsObject.get("catapult_explosion_range"));
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
                        unitStats.widthVariation = getDouble(statsObject.get("width_variation"));
                        unitStats.depthVariation = getDouble(statsObject.get("depth_variation"));
                        break;
                    case CAVALRY:
                    case HORSE_ARCHER:
                    case SKIRMISHER:
                    case SWORDMAN:
                        break;
                    case PHALANX:
                        unitStats.numFirstRows = getInt(statsObject.get("num_first_rows"));
                        unitStats.offAngleFirstRow = getDouble(statsObject.get("off_angle_first_row"));
                        break;
                }

                unitStats.spacing = getDouble(statsObject.get("unit_spacing"));
                unitStats.speed = getDouble(statsObject.get("unit_speed"));
                unitStats.rotationSpeed = getDouble(statsObject.get("unit_rotation_speed"));
                unitStats.patience = getInt(statsObject.get("unit_patience"));
                if (statsObject.containsKey("unit_max_stamina")) {
                    unitStats.staminaStats.maxStamina = getDouble(statsObject.get("unit_max_stamina"));
                }
                if (statsObject.containsKey("unit_stamina_decelerating_rate")) {
                    unitStats.staminaStats.staminaDeceleratingChangeRate = getDouble(statsObject.get("unit_stamina_decelerating_rate"));
                }
                if (statsObject.containsKey("unit_stamina_fighting_rate")) {
                    unitStats.staminaStats.staminaFightingChangeRate = getDouble(statsObject.get("unit_stamina_fighting_rate"));
                }
                if (statsObject.containsKey("unit_stamina_moving_rate")) {
                    unitStats.staminaStats.staminaMovingChangeRate = getDouble(statsObject.get("unit_stamina_moving_rate"));
                }
                if (statsObject.containsKey("unit_stamina_routing_rate")) {
                    unitStats.staminaStats.staminaRoutingChangeRate = getDouble(statsObject.get("unit_stamina_routing_rate"));
                }
                if (statsObject.containsKey("unit_stamina_standing_rate")) {
                    unitStats.staminaStats.staminaStandingChangeRate = getDouble(statsObject.get("unit_stamina_standing_rate"));
                }

                // Add SingleStats and UnitStats to GameStats
                gameStats.addSingleStats(unitType, faction, singleStats);
                gameStats.addUnitStats(unitType, faction, unitStats);
            }
        }
        return gameStats;
    }
}

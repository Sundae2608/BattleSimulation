package model;

import model.algorithms.UnitModifier;
import model.singles.BaseSingle;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.utils.ConfigUtils;
import model.settings.GameSettings;

import java.io.IOException;
import java.util.ArrayList;

public class GameEnvironment {
    /**
     * The main game model that contains every information about the game backend.
     */

    // Contain all units and troops
    ArrayList<BaseUnit> units;
    UnitModifier unitModifier;
    ArrayList<BaseSingle> deadContainer;

    // Terrain
    Terrain terrain;

    // Game model.settings
    GameSettings gameSettings;
    GameStats gameStats;

    /**
     *
     * @param battleConfig Path to the txt file that contains all the game information
     */
    public GameEnvironment(String gameConfig, String terrainConfig, String battleConfig,
                           GameSettings inputGameSettings) {
        gameSettings = inputGameSettings;
        deadContainer = new ArrayList<>();
        try {
            terrain = ConfigUtils.createTerrainFromConfig(terrainConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        unitModifier = new UnitModifier(deadContainer, terrain, gameSettings);
        // Read game stats
        try {
            gameStats = ConfigUtils.readGameStats(gameConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Read battle configuration
        try {
            units = ConfigUtils.readBattleConfigs(battleConfig, gameStats, unitModifier.getObjectHasher(), terrain);
        } catch (IOException e){
            e.printStackTrace();
        }
        for (BaseUnit unit : units) {
            unitModifier.addUnit(unit);
        }
    }

    /**
     * Loop the game by one step
     */
    public void step() {
        unitModifier.getObjectHasher().updateObjects();
        for (BaseUnit unit : units) {
            unit.updateIntention(terrain);
        }
        unitModifier.modifyObjects();
        for (BaseUnit unit : units) {
            unit.updateState();
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

    public UnitModifier getUnitModifier() {
        return unitModifier;
    }

    public ArrayList<BaseSingle> getDeadContainer() {
        return deadContainer;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }
    public GameStats getGameStats() {
        return gameStats;
    }
}

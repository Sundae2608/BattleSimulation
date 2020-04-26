package model;

import model.algorithms.UnitModifier;
import model.singles.BaseSingle;
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

    // Game model.settings
    GameSettings gameSettings;
    GameStats gameStats;

    /**
     *
     * @param configPath Path to the txt file that contains all the game information
     */
    public GameEnvironment(String gameConfig, String configPath) {
        gameSettings = new GameSettings();
        deadContainer = new ArrayList<>();
        unitModifier = new UnitModifier(deadContainer, gameSettings);
        // Read game stats
        try {
            gameStats = ConfigUtils.readGameStats(gameConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Read battle configuration
        try {
            units = ConfigUtils.readBattleConfigs(configPath, gameStats, unitModifier.getObjectHasher());
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
            unit.updateIntention();
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

    public void setUnitModifier(UnitModifier unitModifier) {
        this.unitModifier = unitModifier;
    }

    public ArrayList<BaseSingle> getDeadContainer() {
        return deadContainer;
    }

    public void setDeadContainer(ArrayList<BaseSingle> deadContainer) {
        this.deadContainer = deadContainer;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public void setGameStats(GameStats gameStats) {
        this.gameStats = gameStats;
    }
}

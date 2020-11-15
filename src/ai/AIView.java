package ai;

import java.util.ArrayList;

import model.GameEnvironment;
import model.units.BaseUnit;
import model.utils.PhysicUtils;

/**
 * GameState denotes the state in which the AI Agent perceives the games.
 */
public class AIView {

    // The agent in which the view belongs to.
    AIUnitAgent aiAgent;
    BaseUnit unit;

    // Environment that the AI is looking at.
    GameEnvironment env;

    // Things that an AI agent consider in its decision making process
    ArrayList<BaseUnit> visibleUnits;
    ArrayList<BaseUnit> allyUnits;
    ArrayList<BaseUnit> enemyUnits;

    public AIView(GameEnvironment env, AIUnitAgent aiAgent) {

        // Assign variables internally
        this.aiAgent = aiAgent;
        this.unit = aiAgent.getUnit();
        this.env = env;
        this.visibleUnits = new ArrayList<>();
        this.allyUnits = new ArrayList<>();
        this.enemyUnits = new ArrayList<>();
    }

    /**
     * Update the map connecting a position with a score
     */
    public void updateState() {
        // Update visible units.
        if (env.getGameSettings().isProcessUnitVision()) {
            visibleUnits = PhysicUtils.checkUnitVision(
                    aiAgent.getUnit(), new ArrayList<>(env.getAliveUnits()), env.getTerrain());
        } else {
            visibleUnits = new ArrayList<>(env.getAliveUnits());
        }
        allyUnits = new ArrayList<>(visibleUnits);
        allyUnits.removeIf(x -> x.getPoliticalFaction() != unit.getPoliticalFaction());
        enemyUnits = new ArrayList<>(visibleUnits);
        enemyUnits.removeIf(x -> x.getPoliticalFaction() == unit.getPoliticalFaction());
    }
}

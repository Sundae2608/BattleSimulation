package ai;

import model.enums.UnitState;

import java.util.ArrayList;

/**
 * This class describe the AI "army general". This agent is responsible for determining the overall strategy of the army
 * and then assign goals to each AI Agent. At worst, the AI General will simply do nothing and let each AI Agent
 * making actions for themselves. However, AI General can choose to prioritize certain actions, or sacrifices
 * certain units if it believes
 */
public class AIGeneral {

    private ArrayList<AIUnitAgent> agents;

    public AIGeneral(ArrayList<AIUnitAgent> agents) {
        this.agents = agents;
    }

    public ArrayList<AIUnitAgent> getAgents() {
        return agents;
    }

    /**
     * Command each agents to achieve certain goals.
     */
    public void commandAgents() {
        for (AIUnitAgent agent : agents) {
            UnitState state = agent.getUnit().getState();
            if (state == UnitState.STANDING) {
                agent.move();
            }
        }
    }
}

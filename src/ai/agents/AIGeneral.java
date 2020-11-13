package ai.agents;

import model.enums.UnitState;

import java.util.ArrayList;

/**
 * This class describe the AI "army general". This agent is responsible for determining the overall strategy of the army
 * and then assign goals to each AI Agent. At worst, the AI General will simply do nothing and let each AI Agent
 * making actions for themselves. However, AI General can choose to prioritize certain actions, or sacrifices
 * certain units if it believes
 */
public class AIGeneral {

    private ArrayList<AIAgent> agents;

    public AIGeneral(ArrayList<AIAgent> agents) {
        this.agents = agents;
    }

    public ArrayList<AIAgent> getAgents() {
        return agents;
    }

    /**
     * Command each agents to achieve certain goals.
     */
    public void commandAgents() {
        for (AIAgent agent : agents) {
            UnitState state = agent.getUnit().getState();
            if (state == UnitState.STANDING) {
                agent.move();
            }
        }
    }
}

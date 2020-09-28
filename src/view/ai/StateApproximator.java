package view.ai;

import java.util.ArrayList;

import model.enums.PoliticalFaction;

public class StateApproximator {

    private static StateApproximator instance = null;
    
    private StateApproximator(){

    }
    
    public static StateApproximator getInstance(){
        if(instance == null){
            instance = new StateApproximator();
        }
        return instance;
    }

    public double evaluate(GameState state, PoliticalFaction allyFaction){

        ArrayList<AIUnitView> enemyUnits = new ArrayList<AIUnitView>();
        ArrayList<AIUnitView> allyUnits = new ArrayList<AIUnitView>();
        for(AIUnitView unit : state.getAIUnits()){
            if(unit.getPoliticalFaction() == allyFaction){
                allyUnits.add(unit);
            }
            else{
                enemyUnits.add(unit);
            }
        }

        return Math.random();
    }
}

package ai;

import java.util.ArrayList;

import model.enums.PoliticalFaction;
import model.enums.UnitType;

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

    private boolean isMeleeType(UnitType type){
        switch(type){
            case CAVALRY:
            case SWORDMAN:
                return true;
            default:
                return false;
        }
    }

    private double evaluateUnitsDistance(AIUnitView currentUnit, AIUnitView enemyUnit){
        
        
        double distance = currentUnit.getDistance(enemyUnit);

        UnitType allyType = currentUnit.getBaseUnit().getUnitType();
        double score = distance;

        if(isMeleeType(allyType)) {
            return 1/score;
        }

        return score;

    }
    public double evaluate(GameState state, AIUnitView currentUnit){

        ArrayList<AIUnitView> enemyUnits = new ArrayList<AIUnitView>();
        //ArrayList<AIUnitView> allyUnits = new ArrayList<AIUnitView>();
        PoliticalFaction allyFaction = currentUnit.getPoliticalFaction();
        for(AIUnitView unit : state.getAIUnits()){
            if(unit != currentUnit && unit.getPoliticalFaction() != allyFaction) {
                enemyUnits.add(unit);
            }
        }
        double score = 0;
        for(AIUnitView enemyUnit: enemyUnits){
            score = Math.max(score, evaluateUnitsDistance(currentUnit, enemyUnit));
        }
        
        return score;
    }
}

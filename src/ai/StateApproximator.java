package ai;

import java.util.ArrayList;

import ai.view.AIView;
import ai.view.AIViewOfUnit;
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

    private double evaluateUnitsDistance(AIViewOfUnit currentUnit, AIViewOfUnit enemyUnit){
        
        
        double distance = currentUnit.getDistance(enemyUnit);

        UnitType allyType = currentUnit.getBaseUnit().getUnitType();
        double score = distance;

        if (isMeleeType(allyType)) {
            return 1.0 / score;
        }

        return score;

    }
    public double evaluate(AIView state, AIViewOfUnit currentUnit){

        ArrayList<AIViewOfUnit> enemyUnits = new ArrayList<AIViewOfUnit>();
        //ArrayList<AIUnitView> allyUnits = new ArrayList<AIUnitView>();
        PoliticalFaction allyFaction = currentUnit.getPoliticalFaction();
        for(AIViewOfUnit unit : state.getAIUnits()){
            if(unit != currentUnit && unit.getPoliticalFaction() != allyFaction) {
                enemyUnits.add(unit);
            }
        }
        double score = 0;
        for(AIViewOfUnit enemyUnit: enemyUnits){
            score = Math.max(score, evaluateUnitsDistance(currentUnit, enemyUnit));
        }
        
        return score;
    }
}

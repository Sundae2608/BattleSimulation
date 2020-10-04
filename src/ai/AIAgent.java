package ai;

import model.GameEnvironment;
import model.units.BaseUnit;
import model.utils.GameplayUtils;
import model.utils.MathUtils;

public class AIAgent {
    private BaseUnit unit;
    private GameEnvironment env;
    private GameState state;
    public AIAgent(BaseUnit unit, GameEnvironment env){
        this.env = env;
        this.unit = unit;
        // TODO: derive the size of grid from terrain and unit size
        this.state = new GameState(env, 500, 500); 
    }

    public void move(){
        state.updateState();
        
        AIUnitView aiUnit = state.getAIUnit(this.unit);
        
        int row = aiUnit.getRow();
        int col = aiUnit.getCol();
        
        int nextRow; 
        int nextCol;
        double nextStateScore;

        double bestScore = StateApproximator.getInstance().evaluate(state, aiUnit);
        int bestRow = row;
        int bestCol = col;
        
        for(int rowPadding = -1; rowPadding <= 1; rowPadding++){
            for(int colPadding = -1; colPadding <= 1; colPadding++){
                if(rowPadding == 0 && colPadding == 0){
                    continue;
                }
                nextRow = row + rowPadding;
                nextCol = col + colPadding;
                
                if(!state.isWithinBoundary(nextRow, nextCol)){
                    continue;
                }
                
                aiUnit.move(nextRow, nextCol);
                
                nextStateScore = StateApproximator.getInstance().evaluate(state, aiUnit);
                if(nextStateScore > bestScore){
                    bestRow = nextRow;
                    bestCol = nextCol;
                    bestScore = nextStateScore;
                }

                //move back to the original position
                aiUnit.move(row, col);

            }
        }
        
        double[] goalCoord = state.getCoordinate(bestRow, bestCol);
        
        double[] currentCoord = state.getCoordinate(row, col);
        //System.out.println("Current position " + row + " " + col + " current coordinate " + currentCoord[0] +  ' ' + currentCoord[1]);
        
        if(bestRow == row && bestCol == col){
            return;
        }
        
        //System.out.println("Goal coordinates "  + goalCoord[0] + " " + goalCoord[1] + " Position " + bestRow + " " + bestCol);
        if (GameplayUtils.checkIfUnitCanMoveTowards(
                goalCoord[0], goalCoord[1], env.getConstructs())) {
            double angle = MathUtils.atan2(goalCoord[1] - currentCoord[0], goalCoord[0] - currentCoord[1]);
            
            unit.moveFormationKeptTo(goalCoord[0], goalCoord[1], angle);
        }
    }

    public BaseUnit getUnit(){
        return unit;
    }
}

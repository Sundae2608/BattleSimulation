package ai;


import model.enums.PoliticalFaction;
import model.units.BaseUnit;

public class AIUnitView {
    private int row; 
    private int col; 
    private BaseUnit unit;
    private GameState gameState; 
    
    public AIUnitView(BaseUnit unit, GameState gameState){
        this.gameState = gameState;
        this.unit = unit;
        updateState();
    }
    
    public void updateState(){
        
        int[] position = gameState.getPosition(unit.getAverageX(), unit.getAverageY());
        this.row = position[0];
        this.col = position[1];
    }

    public void move(int row, int col){

        if(!gameState.isWithinBoundary(row, col)){
            return;
        }

        this.row = row;
        this.col = col;
    }
    
    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public PoliticalFaction getPoliticalFaction(){
        return unit.getPoliticalFaction();
    }

    public boolean isAlive(){
        return unit.getNumAlives() > 0;
    }

    public BaseUnit getBaseUnit(){
        return unit;
    }
}

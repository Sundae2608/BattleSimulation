package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.GameEnvironment;
import model.terrain.Terrain;
import model.units.BaseUnit;

/**
 * GameState denotes the state in which the AI Agent perceives the games.
 */
public class AIView {
    
    private HashMap<BaseUnit, AIUnitView> unitsMapping;
    private HashSet<AIUnitView> units;
    private GameEnvironment env;
    private Terrain terrain;
    private int numRows;
    private int numCols;
    private double divX;
    private double divY;

    public AIView(GameEnvironment env, int divX, int divY) {
        this.env = env;
        this.terrain = env.getTerrain();
        this.numRows = (int) Math.ceil((terrain.getBotY() - terrain.getTopY()) / divY);
        this.numCols = (int) Math.ceil((terrain.getBotX() - terrain.getTopX()) / divX);
        this.divX = divX;
        this.divY = divY;
        this.unitsMapping = new HashMap<>();
        this.units = new HashSet<>();

        for(BaseUnit unit : env.getAliveUnits()){
            AIUnitView aiUnit = new AIUnitView(unit, this);
            unitsMapping.put(unit, aiUnit);
            units.add(aiUnit);
        }
        updateState();
    }

    /**
     * Update the state of the AI views.
     */
    public void updateState(){
        units.removeIf(unit -> !unit.isAlive());
    }
    
    public AIUnitView getAIUnit(BaseUnit unit){
        if(!unitsMapping.containsKey(unit)){
            return null;
        }
        return unitsMapping.get(unit);
    }

    public HashSet<AIUnitView> getAIUnits(){
        return units;
    }

	public int[] getPosition(double averageX, double averageY) {

        int col = (int) Math.floor((averageX - terrain.getTopX()) / divX);
        int row = (int) Math.floor((averageY- terrain.getTopY()) / divY);
        
		return new int[]{row, col};
    } 

    public double[] getCoordinate(int row, int col){
        row = Math.max(row, 0);
        row = Math.min(row, numRows - 1);
        col = Math.max(col, 0);
        col = Math.min(col, numCols - 1);
        
        double x = Math.min(terrain.getTopX() + divX * col, terrain.getBotX());
        double y = Math.min(terrain.getTopY() + divY * row, terrain.getBotY());
        return new double[]{x, y};
    }

    public boolean isWithinBoundary(int row, int col){
        if(row < 0 || col < 0 || row >= numRows || col >= numCols){
            return false;
        }
        return true;
    }
}

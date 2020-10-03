package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.GameEnvironment;
import model.terrain.Terrain;
import model.units.BaseUnit;


public class GameState {
    
    private HashMap<BaseUnit, AIUnitView> unitsMapping;
    private HashSet<AIUnitView> units;
    private GameEnvironment env;
    private Terrain terrain;
    private int rowNums;
    private int colNums;
    private double divX;
    private double divY;
    public GameState(GameEnvironment env, int divX, int divY) {
        this.env = env;
        this.terrain = env.getTerrain();
        this.rowNums = (int) Math.ceil((terrain.getBotY() - terrain.getTopY()) / divY);
        this.colNums = (int) Math.ceil((terrain.getBotX() - terrain.getTopX()) / divX);
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
    
    public void updateState(){
        ArrayList<AIUnitView> deadUnits = new ArrayList<AIUnitView>();
        for(AIUnitView unit: units){
            if(!unit.isAlive()){
                deadUnits.add(unit);
                unitsMapping.remove(unit.getBaseUnit());
            }
            unit.updateState();
        }
        for(AIUnitView unit: deadUnits){
            units.remove(unit);
        }
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
        row = Math.min(row, rowNums - 1);
        col = Math.max(col, 0);
        col = Math.min(col, colNums - 1);
        
        double x = Math.min(terrain.getTopX() + divX * col, terrain.getBotX());
        double y = Math.min(terrain.getTopY() + divY * row, terrain.getBotY());
        return new double[]{x, y};
    }

    public boolean isWithinBoundary(int row, int col){
        if(row < 0 || col < 0 || row >= rowNums || col >= colNums){
            return false;
        }
        return true;
    }
}

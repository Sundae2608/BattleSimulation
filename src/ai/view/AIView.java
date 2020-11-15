package ai.view;

import java.util.HashMap;
import java.util.HashSet;

import model.GameEnvironment;
import model.terrain.Terrain;
import model.units.BaseUnit;

/**
 * GameState denotes the state in which the AI Agent perceives the games.
 */
public class AIView {

    private HashMap<BaseUnit, AIViewOfUnit> unitsMapping;
    private HashSet<AIViewOfUnit> units;
    private GameEnvironment env;
    private Terrain terrain;
    private int numRows;
    private int numCols;
    private double divX;
    private double divY;

    public AIView(GameEnvironment env, int divX, int divY) {

        // Assign variables internally
        this.env = env;
        this.terrain = env.getTerrain();
        this.numRows = (int) Math.ceil((terrain.getBotY() - terrain.getTopY()) / divY);
        this.numCols = (int) Math.ceil((terrain.getBotX() - terrain.getTopX()) / divX);
        this.divX = divX;
        this.divY = divY;
        this.unitsMapping = new HashMap<>();
        this.units = new HashSet<>();

        // For each unit in the environment, create an AI view of that unit.
        for(BaseUnit unit : env.getAliveUnits()){
            AIViewOfUnit aiUnit = new AIViewOfUnit(unit, this);
            unitsMapping.put(unit, aiUnit);
            units.add(aiUnit);
        }
        this.updateState();
    }

    /**
     * Update the state of the AI view. This will be called at every step.
     */
    public void updateState() {
        units.removeIf(unit -> !unit.isAlive());
    }


    public AIViewOfUnit getAIViewOfUnit(BaseUnit unit) {
        if (!unitsMapping.containsKey(unit)) {
            return null;
        }
        return unitsMapping.get(unit);
    }

    public HashSet<AIViewOfUnit> getAIUnits() {
        return units;
    }

	public int[] getPosition(double averageX, double averageY) {

        int col = (int) Math.floor((averageX - terrain.getTopX()) / divX);
        int row = (int) Math.floor((averageY- terrain.getTopY()) / divY);
        
		return new int[]{row, col};
    } 

    public double[] getCoordinate(int row, int col) {
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

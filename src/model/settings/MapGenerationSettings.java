package model.settings;

import city_gen_model.algorithms.geometry.house_generation.HouseGenerationSettings;
import city_gen_model.algorithms.geometry.tree_generation.TreeGenerationSettings;

public class MapGenerationSettings {

    boolean pointExtension;
    boolean straightenCurve;

    // An experimental feature to randomly divide polygons into two. We do this to test the polygon splitting
    // functionality.
    double dividePolygonProb;

    HouseGenerationSettings houseGenerationSettings;
    TreeGenerationSettings treeGenerationSettings;

    public boolean isPointExtension() {
        return pointExtension;
    }

    public void setPointExtension(boolean pointExtension) {
        this.pointExtension = pointExtension;
    }

    public boolean isStraightenCurve() {
        return straightenCurve;
    }

    public void setStraightenCurve(boolean straightenCurve) {
        this.straightenCurve = straightenCurve;
    }

    public HouseGenerationSettings getHouseGenerationSettings() {
        return houseGenerationSettings;
    }

    public void setHouseGenerationSettings(HouseGenerationSettings houseGenerationSettings) {
        this.houseGenerationSettings = houseGenerationSettings;
    }

    public TreeGenerationSettings getTreeGenerationSettings() {
        return treeGenerationSettings;
    }

    public void setTreeGenerationSettings(TreeGenerationSettings treeGenerationSettings) {
        this.treeGenerationSettings = treeGenerationSettings;
    }

    public double getDividePolygonProb() {
        return dividePolygonProb;
    }

    public void setDividePolygonProb(double dividePolygonProb) {
        this.dividePolygonProb = dividePolygonProb;
    }
}

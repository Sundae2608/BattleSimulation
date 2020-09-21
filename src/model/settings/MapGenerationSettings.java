package model.settings;

import model.algorithms.geometry.house_generation.HouseGenerationSettings;
import model.algorithms.geometry.tree_generation.TreeGenerationSettings;

public class MapGenerationSettings {

    boolean pointExtension;
    boolean straightenCurve;
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
}

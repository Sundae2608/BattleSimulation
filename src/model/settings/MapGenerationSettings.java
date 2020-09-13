package model.settings;

import model.algorithms.geometry.HouseGenerationSettings;

public class MapGenerationSettings {

    boolean pointExtension;
    boolean straightenCurve;
    HouseGenerationSettings houseGenerationSettings;

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
}

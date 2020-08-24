package model.settings;

public class MapGenerationSettings {

    boolean pointExtension;
    boolean straightenCurve;

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
}

package model.surface;

import model.enums.SurfaceType;
import model.singles.BaseSingle;

import java.util.ArrayList;

public abstract class BaseSurface {
    double[][] surfaceBoundary;
    SurfaceType type;

    public BaseSurface(SurfaceType inputType, ArrayList<double[]> points) {
        double[][] pts = new double[points.size()][2];
        for (int i = 0; i < points.size(); i++) {
            pts[i][0] = points.get(i)[0];
            pts[i][1] = points.get(i)[1];
        }
        type = inputType;
        surfaceBoundary = pts;
    }

    /**
     * Change the behavior of the single, based on the property of the surface.
     */
    public abstract void impactSingle(BaseSingle single);

    public double[][] getSurfaceBoundary() {
        return surfaceBoundary;
    }

    public SurfaceType getType() {
        return type;
    }
}

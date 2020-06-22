package model.surface;

import model.singles.BaseSingle;

public abstract class BaseSurface {
    double[][] surfaceBoundary;

    public BaseSurface(double[][] points) {
        surfaceBoundary = points;
    }

    /**
     * Change the behavior of the single, based on the property of the surface.
     */
    public abstract void impactSingle(BaseSingle single);
}

package model.surface;

import model.enums.SurfaceType;
import model.singles.BaseSingle;

import java.util.ArrayList;

public class SnowSurface extends BaseSurface {
    public SnowSurface(SurfaceType type, ArrayList<double[]> points) {
        super(type, points);
    }

    @Override
    public void impactSingle(BaseSingle single) {

    }
}
package model.surface;

import model.singles.BaseSingle;

import java.util.ArrayList;

public class SnowSurface extends BaseSurface {
    public SnowSurface(ArrayList<double[]> points) {
        super(points);
    }

    @Override
    public void impactSingle(BaseSingle single) {

    }
}
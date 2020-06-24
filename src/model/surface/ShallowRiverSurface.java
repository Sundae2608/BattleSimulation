package model.surface;

import model.singles.BaseSingle;

import java.util.ArrayList;

public class ShallowRiverSurface extends BaseSurface {
    public ShallowRiverSurface(ArrayList<double[]> points) {
        super(points);
    }

    @Override
    public void impactSingle(BaseSingle single) {

    }
}
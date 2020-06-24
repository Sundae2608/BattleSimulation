package model.surface;

import model.constants.GameplayConstants;
import model.singles.BaseSingle;

import java.util.ArrayList;

public class BeachSurface extends BaseSurface {

    public BeachSurface(ArrayList<double[]> points) {
        super(points);
    }

    @Override
    public void impactSingle(BaseSingle single) {
        single.setxVel(single.getxVel() * GameplayConstants.SURFACE_BEACH_SLOWDOWN);
        single.setyVel(single.getyVel() * GameplayConstants.SURFACE_BEACH_SLOWDOWN);
    }
}

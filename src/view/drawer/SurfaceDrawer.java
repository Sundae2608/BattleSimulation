package view.drawer;

import model.surface.BaseSurface;
import processing.core.PApplet;
import view.camera.BaseCamera;

import java.util.HashMap;

public class SurfaceDrawer extends BaseDrawer {

    // Dependency injections
    PApplet applet;
    BaseCamera camera;
    HashMap<BaseSurface, double[][]> surfacePts;

    public SurfaceDrawer(PApplet inputApplet, BaseCamera inputCamera) {
        // Inject the applet and the camera to the surface.
        applet = inputApplet;
        camera = inputCamera;
    }

    @Override
    public void preprocess() {

    }
}

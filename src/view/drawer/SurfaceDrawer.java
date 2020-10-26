package view.drawer;

import model.GameEnvironment;
import model.enums.SurfaceType;
import model.map_objects.Tree;
import model.surface.BaseSurface;
import model.surface.ForestSurface;
import processing.core.PApplet;
import view.camera.BaseCamera;
import view.constants.DrawingConstants;

import java.util.HashMap;

public class SurfaceDrawer extends BaseDrawer {

    // TODO: The hashmap converting base surface to the color of that surface can also be stored here.

    // Dependency injections
    PApplet applet;
    BaseCamera camera;
    GameEnvironment env;
    HashMap<BaseSurface, double[][]> surfacePts;

    public SurfaceDrawer(PApplet inputApplet, GameEnvironment inputEnv, BaseCamera inputCamera) {
        // Inject the applet and the camera to the surface.
        applet = inputApplet;
        camera = inputCamera;
        env = inputEnv;
        surfacePts = new HashMap<>();
    }

    public void drawSurface(BaseSurface surface) {
        int[] surfaceColor = DrawingUtils.getSurfaceColor(surface);
        applet.fill(surfaceColor[0], surfaceColor[1], surfaceColor[2], surfaceColor[3]);
        applet.beginShape();
        if (!surfacePts.containsKey(surface)) {
            double[][] pts = surface.getSurfaceBoundary();
            double[][] pts3d = new double[pts.length][3];
            for (int i = 0; i < pts.length; i++) {
                pts3d[i] = new double[] {
                        pts[i][0], pts[i][1], env.getTerrain().getZFromPos(pts[i][0], pts[i][1])
                };
            }
            surfacePts.put(surface, pts3d);
        }
        double[][] pts = surfacePts.get(surface);
        for (int i = 0; i < pts.length; i++) {
            double[] drawingPts = camera.getDrawingPosition(pts[i][0], pts[i][1], pts[i][2]);
            applet.vertex((float) drawingPts[0], (float) drawingPts[1]);
        }
        applet.endShape(PApplet.CLOSE);
        if (surface.getType() == SurfaceType.FOREST) {
            for (Tree tree : ((ForestSurface) surface).getTrees()) {
                int[] treeColor = DrawingConstants.TREE_COLOR;
                double height = env.getTerrain().getZFromPos(tree.getX(), tree.getY());
                applet.fill(treeColor[0], treeColor[1], treeColor[2], treeColor[3]);
                double[] drawingPosition = camera.getDrawingPosition(tree.getX(), tree.getY(),
                        height);
                applet.circle((float) drawingPosition[0], (float) drawingPosition[1],
                        (float) (tree.getRadius() * 2 * camera.getZoomAtHeight(height)));
            }
        }
    }

    @Override
    public void preprocess() {
        return;
    }
}

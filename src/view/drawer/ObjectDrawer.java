package view.drawer;

import javafx.util.Pair;
import model.constants.UniversalConstants;
import model.projectile_objects.Arrow;
import model.projectile_objects.Ballista;
import model.projectile_objects.BaseObject;
import model.projectile_objects.Stone;
import model.singles.BaseSingle;
import model.terrain.Terrain;
import model.utils.MathUtils;
import processing.core.PApplet;
import view.camera.BaseCamera;
import view.camera.TopDownCamera;
import view.constants.DrawingConstants;
import view.settings.DrawingSettings;

import java.util.HashMap;
import java.util.HashSet;

public class ObjectDrawer extends BaseDrawer {

    PApplet applet;
    BaseCamera camera;
    DrawingSettings drawingSettings;
    ShapeDrawer shapeDrawer;

    // TODO: If the object is an arrow, generate a probability between 0 and 1, and then store it here.
    HashMap<Arrow, Double> arrowDrawingProbs;
    HashSet<Arrow> arrowDrawn;

    public ObjectDrawer(PApplet inputApplet,
                        BaseCamera inputCamera,
                        ShapeDrawer inputShapeDrawer,
                        DrawingSettings inputDrawingSettings) {
        applet = inputApplet;
        shapeDrawer = inputShapeDrawer;
        camera = inputCamera;
        drawingSettings = inputDrawingSettings;

        // Initialize arrow optimizer.
        arrowDrawingProbs = new HashMap<>();
        arrowDrawn = new HashSet<>();
    }

    @Override
    public void preprocess() {
        // Remove all probability that does not exist within the arrowing drawing probabilities.
        arrowDrawingProbs.entrySet().removeIf(x -> !arrowDrawn.contains(x));
        arrowDrawn.clear();
        return;
    }

    /**
     * Draw the object
     */
    public void drawObject(BaseObject object, Terrain terrain) {

        // Recalculate position and shape based on the view.camera position
        double z = terrain.getZFromPos(object.getX(), object.getY());
        z += object.getHeight() * DrawingConstants.AIRBORNE_OBJECT_HEIGHT_SCALE;
        double[] position = camera.getDrawingPosition(
                object.getX(),
                object.getY(),
                z);
        double drawX = position[0];
        double drawY = position[1];

        // Check if the object is drawable.
        if (!DrawingUtils.drawable(drawX, drawY, camera.getWidth(), camera.getHeight())) return;

        // Draw the object
        double angle = camera.getCameraAngleFromActualAngle(object.getAngle());
        int[] color = DrawingConstants.UNIVERSAL_OBJECT_COLOR;
        applet.fill(color[0], color[1], color[2]);
        if (object instanceof Arrow) {
            if (camera instanceof TopDownCamera) {
                // Optimization logic only applies in TopDownCamera. For other camera we draw arrow as usual.
                if (!arrowDrawingProbs.containsKey(object)) {
                    arrowDrawingProbs.put((Arrow) object, MathUtils.randUniform());
                }
                double prob = arrowDrawingProbs.get(object);
                double arrowDist = ((TopDownCamera) camera).getHeightAtCurrentZoom() - z;
                double visibility =
                        MathUtils.capMinMax((arrowDist - prob * (
                                (DrawingConstants.ARROW_HEIGHT_VISIBLE_BEGIN_MAX
                                        - DrawingConstants.ARROW_HEIGHT_VISIBLE_BEGIN_MIN) +
                                DrawingConstants.ARROW_HEIGHT_VISIBLE_BEGIN_MIN
                        )) / DrawingConstants.ARROW_HEIGHT_VISIBLE_REDUCTION_RANGE, 0, 1);
                if (visibility > 0) {
                    applet.fill(color[0], color[1], color[2], (int) ((1.0 - visibility) * 255));
                }
            }
            shapeDrawer.arrow(
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.ARROW_SIZE);
        }
        if (object instanceof Ballista) {
            shapeDrawer.arrow(
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.BALISTA_SIZE);
        }
        if (object instanceof Stone) {
            shapeDrawer.circleShape(
                    (float) drawX, (float) drawY,
                    (float) (camera.getZoomAtHeight(z) * DrawingConstants.CATAPULT_SIZE));
        }
    }

    /**
     * Draw the object carried by the troop
     */
    public void drawObjectCarriedByTroop(int lifeTime, BaseObject object, BaseSingle single) {

        // Recalculate object actual position
        Pair<Double, Double> rotatedVector = MathUtils.rotate(object.getX(), object.getY(), single.getAngle());
        double z = single.getZ();

        // Recalculate position and shape based on the view.camera position
        double[] position = camera.getDrawingPosition(
                rotatedVector.getKey() + single.getX(),
                rotatedVector.getValue() + single.getY(), z);
        double drawX = position[0];
        double drawY = position[1];

        // Don't draw if the object is out of the camera
        if (!DrawingUtils.drawable(drawX, drawY, camera.getWidth(), camera.getHeight())) return;

        // Draw the object
        int opacity = (int) (255 * Math.min(1.0, 1.0 * lifeTime / UniversalConstants.CARRIED_OBJECT_FADEAWAY));
        double angle = camera.getCameraAngleFromActualAngle(object.getAngle() + single.getAngle());
        int[] color = DrawingConstants.UNIVERSAL_OBJECT_COLOR;
        applet.fill(color[0], color[1], color[2], opacity);
        if (object instanceof Arrow) {
            shapeDrawer.arrow(
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.ARROW_SIZE);
        } else if (object instanceof Ballista) {
            shapeDrawer.arrow(
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.BALISTA_SIZE);
        }
    }
}

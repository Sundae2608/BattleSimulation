package view.drawer;

import view.camera.Camera;
import view.constants.DrawingConstants;
import model.constants.UniversalConstants;
import processing.core.PApplet;
import processing.core.PGraphics;
import view.settings.DrawingSettings;

import java.util.HashMap;

/**
 * This class contains the drawer for all weapons in the games.
 */
public class ShapeDrawer extends PApplet {

    // Weapon optimizers
    HashMap<Double, DrawingVertices> swordMap;
    HashMap<Double, DrawingVertices> spearMap;
    HashMap<Double, DrawingVertices> bowShapeMap;
    HashMap<Double, DrawingVertices> arrowShapeMap;
    HashMap<Double, DrawingVertices> cavalryShapeMap;

    public ShapeDrawer() {
        swordMap = new HashMap<>();
        spearMap = new HashMap<>();
        bowShapeMap = new HashMap<>();
        arrowShapeMap = new HashMap<>();
        cavalryShapeMap = new HashMap<>();
    }

    /**
     * Draw a spear with a specific length;
     */
    public void spear(PGraphics g, float x, float y, float angle, float spearLength, float size, DrawingSettings settings) {
        if (!spearMap.containsKey(size)) {
            float[][] spearShape;
            if (size > UniversalConstants.ZOOM_RENDER_LEVEL_NORMAL) {
                spearShape = new float[][]{
                        {-spearLength / 4 * size, 0},
                        {-spearLength / 4 * size, -1 * size},
                        {spearLength * 3 / 4 * size, -1 * size},
                        {spearLength * 3 / 4 * size, -2 * size},
                        {(spearLength * 3 / 4 + 1) * size, -2 * size},
                        {(spearLength * 3 / 4 + 1) * size, -1.5f * size},
                        {(spearLength * 3 / 4 + 11) * size, -1.5f * size},
                        {(spearLength * 3 / 4 + 12.5f) * size, 0},
                        {(spearLength * 3 / 4 + 11) * size, 1.5f * size},
                        {(spearLength * 3 / 4 + 1) * size, 1.5f * size},
                        {(spearLength * 3 / 4 + 1) * size, 2 * size},
                        {(spearLength * 3 / 4) * size, 2 * size},
                        {spearLength * 3 / 4 * size, 1 * size},
                        {-spearLength / 4 * size, 1 * size}
                };
            } else if (size > UniversalConstants.ZOOM_RENDER_SPEAR_DISAPPEAR) {
                spearShape = new float[][]{
                        {-spearLength / 4 * size, -1 * size},
                        {(spearLength * 3 / 4 + 12.5f) * size, -1f * size},
                        {(spearLength * 3 / 4 + 12.5f) * size, 1f * size},
                        {-spearLength / 4 * size, 1 * size}
                };
            } else {
                spearShape = new float[][] {};
            }
            spearMap.put((double)size, new DrawingVertices(spearShape));
        }
        float[][] vertices = spearMap.get((double) size).getVertices();
        g.pushMatrix();
        g.translate(x, y);
        g.rotate(angle);
        g.beginShape();
        for (int i = 0; i < vertices.length; i++) {
            g.vertex(vertices[i][0], vertices[i][1]);
        }
        g.endShape(CLOSE);
        g.popMatrix();
    }

    /**
     * Draw a sword
     */
    public void sword(PGraphics g, double x, double y, double angle, double size, DrawingSettings settings) {
        if (!swordMap.containsKey(size)) {
            float[][] swordShape;
            if (size > UniversalConstants.ZOOM_RENDER_LEVEL_NORMAL) {
                swordShape = new float[][] {
                        {0, 0},
                        {0, (float) (-1.5f * size)},
                        {(float) (2 * size), (float) (-1.5f * size)},
                        {(float) (2 * size), (float) (-3 * size)},
                        {(float) (3 * size), (float) (-3 * size)},
                        {(float) (3 * size), (float) (-1.5f * size)},
                        {(float) (20 * size), (float) (-1.5f * size)},
                        {(float) (21.5f * size), 0},
                        {(float) (20 * size), (float) (1.5f * size)},
                        {(float) (3 * size), (float) (1.5f * size)},
                        {(float) (3 * size), (float) (3 * size)},
                        {(float) (2 * size), (float) (3 * size)},
                        {(float) (2 * size), (float) (1.5f * size)},
                        {(float) (0 * size), (float) (1.5f * size)}
                };
            } else if (size > UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE) {
                swordShape = new float[][] {
                        {0, (float) (-1.5f * size)},
                        {(float) (21.5f * size), (float) (-1.5f * size)},
                        {(float) (21.5f * size), (float) (1.5f * size)},
                        {(float) (0 * size), (float) (1.5f * size)}
                };
            } else {
                swordShape = new float[][] {};
            }
            swordMap.put( size, new DrawingVertices(swordShape));
        }
        float[][] vertices = swordMap.get(size).getVertices();
        g.pushMatrix();
        g.translate((float)x, (float)y);
        g.rotate((float) angle);
        g.beginShape();
        for (int i = 0; i < vertices.length; i++) {
            g.vertex(vertices[i][0], vertices[i][1]);
        }
        g.endShape(CLOSE);
        g.popMatrix();
    }

    /**
     * Draw a bow
     */
    public void bow(PGraphics g, float x, float y, float angle, float size, DrawingSettings settings) {
        if (!bowShapeMap.containsKey(size)) {
            float[][] bowShape;
            if (size > UniversalConstants.ZOOM_RENDER_LEVEL_NORMAL) {
                bowShape = new float[][]{
                        {0.3536f * size, 0.3536f * size},
                        {0.7071f * size, 0},
                        {0.3536f * size, -0.3536f * size},
                };
            } else {
                bowShape = new float[][] {};
            }
            bowShapeMap.put((double)size, new DrawingVertices(bowShape));
        }
        float[][] vertices = bowShapeMap.get((double) size).getVertices();
        g.pushMatrix();
        g.translate(x, y);
        g.rotate(angle);
        g.beginShape();
        for (int i = 0; i < vertices.length; i++) {
            g.vertex(vertices[i][0], vertices[i][1]);
        }
        g.endShape(CLOSE);
        g.popMatrix();
    }

    /**
     * Draw an arrow
     */
    public void arrow(PGraphics g, float x, float y, float angle, float zoom, DrawingSettings settings) {
        float arrowSize = (float) DrawingConstants.PLAN_ARROW_SIZE;
        if (!arrowShapeMap.containsKey(zoom)) {
            float[][] arrowShape;
            if (zoom > UniversalConstants.ZOOM_RENDER_LEVEL_ARROW_DETAIL) {
                arrowShape = new float[][]{
                        {0, 0},
                        {-0.16f * zoom * arrowSize, -0.06f * zoom * arrowSize},
                        {-0.12f * zoom * arrowSize, -0.02f * zoom * arrowSize},
                        {-0.84f * zoom * arrowSize, -0.02f * zoom * arrowSize},
                        {-0.88f * zoom * arrowSize, -0.06f * zoom * arrowSize},
                        {-zoom * arrowSize, -0.06f * zoom * arrowSize},
                        {-0.94f * zoom * arrowSize, 0},
                        {-zoom * arrowSize, 0.06f * zoom * arrowSize},
                        {-0.88f * zoom * arrowSize, 0.06f * zoom * arrowSize},
                        {-0.84f * zoom * arrowSize, 0.02f * zoom * arrowSize},
                        {-0.12f * zoom * arrowSize, 0.02f * zoom * arrowSize},
                        {-0.16f * zoom * arrowSize, 0.06f * zoom * arrowSize},
                };
            } else if (zoom > UniversalConstants.ZOOM_RENDER_LEVEL_TROOP) {
                arrowShape = new float[][]{
                        {0, -0.02f * zoom * arrowSize},
                        {0, 0.02f * zoom * arrowSize},
                        {-zoom * arrowSize, 0.02f * zoom * arrowSize},
                        {-zoom * arrowSize, -0.02f * zoom * arrowSize},
                };
            } else {
                arrowShape = new float[][] {};
            }
            arrowShapeMap.put((double)zoom, new DrawingVertices(arrowShape));
        }
        float[][] vertices = arrowShapeMap.get((double) zoom).getVertices();
        g.pushMatrix();
        g.translate(x, y);
        g.rotate(angle);
        g.beginShape();
        for (int i = 0; i < vertices.length; i++) {
            g.vertex(vertices[i][0], vertices[i][1]);
        }
        g.endShape(CLOSE);
        g.popMatrix();
    }

    /**
     * Draw the shape of infantry
     * @param g PGraphics object that draw the infantry shape
     * @param x Position x
     * @param y Position y
     * @param size Contains two size. One size is the when the infantry is drawn as circle, the other is the size when
     *             the infantry is drawn as a square.
     * @param camera The view.camera.
     */
    public void infantryShape(PGraphics g, double x, double y, double size, double sizeSimplfied, Camera camera) {
        if (camera.getZoom() < UniversalConstants.ZOOM_RENDER_LEVEL_SIMPLIFY_TROOP_SHAPE) {
            g.rect(
                    (float) x,
                    (float) y,
                    (float) sizeSimplfied,
                    (float) sizeSimplfied);
        } else {
            g.ellipse(
                    (float) x,
                    (float) y,
                    (float) size,
                    (float) size);
        }
    }

    /**
     * Draw the shape of calvary
     */
    public void cavalryShape(PGraphics g, double x, double y, double angle, double size, Camera camera) {
        float[][] cavShape;
        if (!cavalryShapeMap.containsKey(size)) {
            if (camera.getZoom() > UniversalConstants.ZOOM_RENDER_LEVEL_CAV_PERCEPTIVE) {
                cavShape = new float[][] {
                        {(float) (0.75f * size), 0},
                        {(float) (-0.375f * size), (float) (0.5f * size)},
                        {(float) (-0.625f * size), 0},
                        {(float) (-0.375f * size), (float) (-0.5f * size)},
                };
            } else {
                cavShape = new float[][] {
                        {(float) (0.75f * size), 0},
                        {(float) (-0.375f * size), (float) (0.5f * size)},
                        {(float) (-0.375f * size), (float) (-0.5f * size)},
                };
            }
            cavalryShapeMap.put(size, new DrawingVertices(cavShape));
        }
        float[][] vertices = cavalryShapeMap.get(size).getVertices();
        g.pushMatrix();
        g.translate((float) x, (float) y);
        g.rotate((float) angle);
        g.beginShape();
        for (int i = 0; i < vertices.length; i++) {
            g.vertex(vertices[i][0], vertices[i][1]);
        }
        g.endShape(CLOSE);
        g.popMatrix();
    }
}

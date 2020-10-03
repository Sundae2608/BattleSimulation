package view.drawer;

import model.terrain.Terrain;
import model.units.BaseUnit;
import model.utils.MathUtils;
import processing.core.PApplet;
import view.camera.BaseCamera;
import view.constants.DrawingConstants;
import view.settings.DrawingSettings;

public class BattleSignalDrawer extends BaseDrawer {

    PApplet applet;
    BaseCamera camera;
    DrawingSettings drawingSettings;

    double unitShadowDrawingOffsetx;
    double unitShadowDrawingOffsetY;

    public BattleSignalDrawer(PApplet inputApplet, BaseCamera inputCamera, DrawingSettings inputDrawingSettings) {
        applet = inputApplet;
        camera = inputCamera;
        drawingSettings = inputDrawingSettings;
    }

    @Override
    public void preprocess() {
        // Pre-calculate shadow, also for optimization
        if (drawingSettings.isDrawTroopShadow()) {
            unitShadowDrawingOffsetx = MathUtils.quickCos((float)
                    DrawingConstants.SHADOW_ANGLE) * DrawingConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
            unitShadowDrawingOffsetY = MathUtils.quickCos((float)
                    DrawingConstants.SHADOW_ANGLE) * DrawingConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
        }
    }

    /**
     * Draw unit arrow plan. This arrow indicates the potential position that the unit is moving to.
     */
    public void drawArrowPlan(double beginX, double beginY, double endX, double endY, Terrain terrain) {
        double angle = MathUtils.atan2(endY - beginY, endX - beginX);
        double rightAngle = angle + Math.PI / 2;

        double upUnitX = MathUtils.quickCos((float) angle) * DrawingConstants.ARROW_SIZE;
        double upUnitY = MathUtils.quickSin((float) angle) * DrawingConstants.ARROW_SIZE;
        double rightUnitX = MathUtils.quickCos((float) rightAngle) * DrawingConstants.ARROW_SIZE;
        double rightUnitY = MathUtils.quickSin((float) rightAngle) * DrawingConstants.ARROW_SIZE;

        double[][] arrow = {
                {beginX + rightUnitX * 0.8, beginY + rightUnitY * 0.8},
                {endX + rightUnitX * 0.66 - upUnitX * 2.4, endY + rightUnitY * 0.66 - upUnitY * 2.4},
                {endX + rightUnitX * 1.66 - upUnitX * 3, endY + rightUnitY * 1.66 - upUnitY * 3},
                {endX, endY},
                {endX - rightUnitX * 1.66 - upUnitX * 3, endY - rightUnitY * 1.66 - upUnitY * 3},
                {endX - rightUnitX * 0.66 - upUnitX * 2.4, endY - rightUnitY * 0.66 - upUnitY * 2.4},
                {beginX - rightUnitX * 0.8, beginY - rightUnitY * 0.8},
        };

        applet.beginShape();
        for (int i = 0; i < arrow.length; i++) {
            double[] drawingPosition = camera.getDrawingPosition(
                    arrow[i][0], arrow[i][1], terrain.getZFromPos(arrow[i][0], arrow[i][1]));
            applet.vertex((float) drawingPosition[0], (float) drawingPosition[1]);
        }
        applet.endShape(PApplet.CLOSE);
    }

    /**
     * Draw an arrow from (beginX, beginY) to (endX, endY) at certain height.
     */
    public void drawArrowAtHeight(double beginX, double beginY, double endX, double endY, double height) {
        double angle = MathUtils.atan2(endY - beginY, endX - beginX);
        double rightAngle = angle + Math.PI / 2;

        double upUnitX = MathUtils.quickCos((float) angle) * DrawingConstants.ARROW_SIZE;
        double upUnitY = MathUtils.quickSin((float) angle) * DrawingConstants.ARROW_SIZE;
        double rightUnitX = MathUtils.quickCos((float) rightAngle) * DrawingConstants.ARROW_SIZE;
        double rightUnitY = MathUtils.quickSin((float) rightAngle) * DrawingConstants.ARROW_SIZE;

        double[][] arrow = {
                {beginX + rightUnitX * 0.8, beginY + rightUnitY * 0.8},
                {endX + rightUnitX * 0.66 - upUnitX * 2.4, endY + rightUnitY * 0.66 - upUnitY * 2.4},
                {endX + rightUnitX * 1.66 - upUnitX * 3, endY + rightUnitY * 1.66 - upUnitY * 3},
                {endX, endY},
                {endX - rightUnitX * 1.66 - upUnitX * 3, endY - rightUnitY * 1.66 - upUnitY * 3},
                {endX - rightUnitX * 0.66 - upUnitX * 2.4, endY - rightUnitY * 0.66 - upUnitY * 2.4},
                {beginX - rightUnitX * 0.8, beginY - rightUnitY * 0.8},
        };

        applet.beginShape();
        for (int i = 0; i < arrow.length; i++) {
            double[] drawingPosition = camera.getDrawingPosition(arrow[i][0], arrow[i][1], height);
            applet.vertex((float) drawingPosition[0], (float) drawingPosition[1]);
        }
        applet.endShape(PApplet.CLOSE);
    }

    /**
     * Draw the block representing the entire unit.
     */
    public void drawUnitBlock(BaseUnit unit, Terrain terrain) {

        // draw the bounding box.
        double[][] aliveBoundingBox = unit.getAliveBoundingBox();
        double[][] boundingBox = unit.getBoundingBox();

        // Convert to drawer points
        double[] p1 = camera.getDrawingPosition(aliveBoundingBox[0][0], aliveBoundingBox[0][1],
                terrain.getZFromPos(aliveBoundingBox[0][0], aliveBoundingBox[0][1]));
        double[] p2 = camera.getDrawingPosition(aliveBoundingBox[1][0], aliveBoundingBox[1][1],
                terrain.getZFromPos(aliveBoundingBox[1][0], aliveBoundingBox[1][1]));
        double[] p3 = camera.getDrawingPosition(aliveBoundingBox[2][0], aliveBoundingBox[2][1],
                terrain.getZFromPos(aliveBoundingBox[2][0], aliveBoundingBox[2][1]));
        double[] p4 = camera.getDrawingPosition(aliveBoundingBox[3][0], aliveBoundingBox[3][1],
                terrain.getZFromPos(aliveBoundingBox[3][0], aliveBoundingBox[3][1]));

        double[] p5 = camera.getDrawingPosition(boundingBox[2][0], boundingBox[2][1],
                terrain.getZFromPos(boundingBox[2][0], boundingBox[2][1]));
        double[] p6 = camera.getDrawingPosition(boundingBox[3][0], boundingBox[3][1],
                terrain.getZFromPos(boundingBox[3][0], boundingBox[3][1]));

        // First, draw the box indicating the unit at full strength
        applet.fill(DrawingConstants.UNIT_SIZE_COLOR[0],
                DrawingConstants.UNIT_SIZE_COLOR[1],
                DrawingConstants.UNIT_SIZE_COLOR[2],
                DrawingConstants.UNIT_SIZE_COLOR[3]);
        applet.beginShape();
        applet.vertex((float) p1[0], (float) p1[1]);
        applet.vertex((float) p2[0], (float) p2[1]);
        applet.vertex((float) p5[0], (float) p5[1]);
        applet.vertex((float) p6[0], (float) p6[1]);
        applet.endShape(PApplet.CLOSE);

        // Then draw the box indicating the actual current scale of the the unit
        if (drawingSettings.isDrawTroopShadow()) {
            int[] shadowColor = DrawingConstants.SHADOW_COLOR;
            applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
            applet.beginShape();
            applet.vertex((float) (p1[0] + unitShadowDrawingOffsetx * 2), (float) (p1[1] + unitShadowDrawingOffsetY * 2));
            applet.vertex((float) (p2[0] + unitShadowDrawingOffsetx * 2), (float) (p2[1] + unitShadowDrawingOffsetY * 2));
            applet.vertex((float) (p3[0] + unitShadowDrawingOffsetx * 2), (float) (p3[1] + unitShadowDrawingOffsetY * 2));
            applet.vertex((float) (p4[0] + unitShadowDrawingOffsetx * 2), (float) (p4[1] + unitShadowDrawingOffsetY * 2));
            applet.endShape(PApplet.CLOSE);
        }

        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        applet.fill(color[0], color[1], color[2], 255);
        applet.beginShape();
        applet.vertex((float) p1[0], (float) p1[1]);
        applet.vertex((float) p2[0], (float) p2[1]);
        applet.vertex((float) p3[0], (float) p3[1]);
        applet.vertex((float) p4[0], (float) p4[1]);
        applet.endShape(PApplet.CLOSE);
    }
}

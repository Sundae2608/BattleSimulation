package view.drawer;

import model.constants.UniversalConstants;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.utils.MathUtils;
import processing.core.PApplet;
import view.camera.Camera;
import view.constants.DrawingConstants;
import view.settings.DrawingSettings;

public class BattleSignalDrawer extends BaseDrawer {

    PApplet applet;
    Camera camera;
    DrawingSettings drawingSettings;

    double unitShadowDrawingOffsetx;
    double unitShadowDrawingOffsetY;

    public BattleSignalDrawer(PApplet inputApplet, Camera inputCamera, DrawingSettings inputDrawingSettings) {
        applet = inputApplet;
        camera = inputCamera;
        drawingSettings = inputDrawingSettings;
    }

    @Override
    public void preprocess() {
        // Pre-calculate shadow, also for optimization
        if (drawingSettings.isDrawTroopShadow()) {
            unitShadowDrawingOffsetx = MathUtils.quickCos((float)
                    UniversalConstants.SHADOW_ANGLE) * UniversalConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
            unitShadowDrawingOffsetY = MathUtils.quickCos((float)
                    UniversalConstants.SHADOW_ANGLE) * UniversalConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
        }
    }

    /**
     * Draw the block representing the entire unit.
     */
    public void drawUnitBlock(BaseUnit unit, Terrain terrain) {

        // draw the bounding box.
        double[][] boundingBox = unit.getBoundingBox();

        // Convert to drawer points
        double[] p1 = camera.getDrawingPosition(boundingBox[0][0], boundingBox[0][1],
                terrain.getHeightFromPos(boundingBox[0][0], boundingBox[0][1]));
        double[] p2 = camera.getDrawingPosition(boundingBox[1][0], boundingBox[1][1],
                terrain.getHeightFromPos(boundingBox[1][0], boundingBox[1][1]));
        double[] p3 = camera.getDrawingPosition(boundingBox[2][0], boundingBox[2][1],
                terrain.getHeightFromPos(boundingBox[2][0], boundingBox[2][1]));
        double[] p4 = camera.getDrawingPosition(boundingBox[3][0], boundingBox[3][1],
                terrain.getHeightFromPos(boundingBox[3][0], boundingBox[3][1]));
        double[] p5 = camera.getDrawingPosition(boundingBox[4][0], boundingBox[4][1],
                terrain.getHeightFromPos(boundingBox[4][0], boundingBox[4][1]));
        double[] p6 = camera.getDrawingPosition(boundingBox[5][0], boundingBox[5][1],
                terrain.getHeightFromPos(boundingBox[5][0], boundingBox[5][1]));

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
            int[] shadowColor = UniversalConstants.SHADOW_COLOR;
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

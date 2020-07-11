package view.drawer;

import processing.core.PApplet;
import view.constants.MapMakerConstants;

/**
 * This class contains drawers for all UI elements in the game.
 */
public class UIDrawer {

    PApplet applet;

    public UIDrawer(PApplet inputApplet) {
        applet = inputApplet;
    }

    /**
     * Paint circle
     */
    public void paintCircle(float x, float y) {
        applet.noFill();
        applet.strokeWeight(3);
        applet.stroke(MapMakerConstants.PAINT_CIRCLE_COLOR[0], MapMakerConstants.PAINT_CIRCLE_COLOR[1],
                MapMakerConstants.PAINT_CIRCLE_COLOR[2], MapMakerConstants.PAINT_CIRCLE_COLOR[3]);
        applet.circle(x, y, MapMakerConstants.PAINT_CIRCLE_SIZE);
        applet.noStroke();
    }

    /**
     * Pause button
     */
    public void pauseButton(float x, float y, float size) {
        applet.fill(80, 80, 80);
        applet.pushMatrix();
        applet.translate(x, y);

        applet.beginShape();
        applet.vertex(-0.5f * size, -0.5f * size);
        applet.vertex(-0.1f * size, -0.5f * size);
        applet.vertex(-0.1f * size, 0.5f * size);
        applet.vertex(-0.5f * size, 0.5f * size);
        applet.endShape(PApplet.CLOSE);

        applet.beginShape();
        applet.vertex(0.1f * size, 0.5f * size);
        applet.vertex(0.5f * size, 0.5f * size);
        applet.vertex(0.5f * size, -0.5f * size);
        applet.vertex(0.1f * size, -0.5f * size);
        applet.endShape(PApplet.CLOSE);
        applet.popMatrix();
    }

    /**
     * Play button
     */
    public void playButton(float x, float y, float size) {
        applet.fill(80, 80, 80);
        applet.pushMatrix();
        applet.translate(x, y);

        applet.beginShape();
        applet.vertex(-0.5f * size, -0.5f * size);
        applet.vertex(-0.5f * size, 0.5f * size);
        applet.vertex(0.5f * size, 0);
        applet.endShape(PApplet.CLOSE);
        applet.popMatrix();
    }
}

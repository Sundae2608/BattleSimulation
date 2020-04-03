package drawer;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * This class contains drawers for all UI elements in the game.
 */
public class UIDrawer extends PApplet {

    /**
     * Pause button
     */
    public void pauseButton(PGraphics g, float x, float y, float size) {
        g.fill(80, 80, 80);
        g.pushMatrix();
        g.translate(x, y);

        g.beginShape();
        g.vertex(-0.5f * size, -0.5f * size);
        g.vertex(-0.1f * size, -0.5f * size);
        g.vertex(-0.1f * size, 0.5f * size);
        g.vertex(-0.5f * size, 0.5f * size);
        g.endShape(CLOSE);

        g.beginShape();
        g.vertex(0.1f * size, 0.5f * size);
        g.vertex(0.5f * size, 0.5f * size);
        g.vertex(0.5f * size, -0.5f * size);
        g.vertex(0.1f * size, -0.5f * size);
        g.endShape(CLOSE);
        g.popMatrix();
    }

    /**
     * Play button
     */
    public void playButton(PGraphics g, float x, float y, float size) {
        g.fill(80, 80, 80);
        g.pushMatrix();
        g.translate(x, y);

        g.beginShape();
        g.vertex(-0.5f * size, -0.5f * size);
        g.vertex(-0.5f * size, 0.5f * size);
        g.vertex(0.5f * size, 0);
        g.endShape(CLOSE);
        g.popMatrix();
    }
}

package view.drawer;

import processing.core.PApplet;
import processing.core.PFont;

public class InfoDrawer {

    PApplet applet;
    PFont font;

    public InfoDrawer(PApplet inputApplet) {
        applet = inputApplet;
        font = applet.createFont("Monospaced", 13);
    }

    /**
     * Helper function which draws a string s, with input position (x, y) as the bottom left anchor. This helper method
     * is used for writing a monitor on the bottom left of the screen to track important counter variables.
     * TODO: Add a fourth argument which allows anchoring in one of 4 modes:
     *  TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
     */
    public void drawTextBox(String s, double x, double y) {
        // Split the string into lines
        applet.textFont(font);
        applet.textAlign(PApplet.LEFT);
        String[] lines = s.split("\n");

        // Draw a white rectangle underneath to make the text pops out
        applet.fill(255, 255, 255, 100);
        applet.rectMode(PApplet.CORNER);
        applet.rect((float) x - 5, (float) y - 5 - 18 * lines.length, (float) 500, (float) 18 * lines.length + 10);
        applet.fill(0, 0, 0);
        for (int i = 0; i < lines.length; i++) {
            applet.text(lines[lines.length - 1 - i], (float) x, (float) (y - 5 - 18 * i));
        }
    }
}

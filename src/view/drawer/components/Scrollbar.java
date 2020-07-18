package view.drawer.components;

import processing.core.PApplet;

public class Scrollbar {

    float xPos, yPos;       // x and y position of bar
    int barWidth, barHeight;    // width and height of bar
    float sliderPos;    // x position of slider
    float sliderMinPos, sliderMaxPos; // max and min values of slider
    boolean mouseOver;
    boolean locked;

    PApplet pApplet;

    /**
     * Scrollbar used for parameter settings (modified from https://processing.org/examples/scrollbar.html)
     * @param applet
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Scrollbar(PApplet applet, float x, float y, int width, int height) {

        pApplet = applet;

        xPos = x;
        yPos = y;

        barWidth = width;
        barHeight = height;

        sliderMinPos = xPos;
        sliderMaxPos = xPos + barWidth - height;

        sliderPos = xPos + width/2;
    }

    public void update() {
        mouseOver = isMouseOver();
        if (pApplet.mousePressed && mouseOver) {
            locked = true;
        }
        if (!pApplet.mousePressed) {
            locked = false;
        }

        if (locked) {
            float newSliderPos = constrain(pApplet.mouseX-barHeight/2, sliderMinPos, sliderMaxPos);

            if (Math.abs(newSliderPos - sliderPos) > 1) {
                sliderPos = newSliderPos;
            }
        }
        display();
    }

    private float constrain(float val, float minv, float maxv) {
        return Math.min(Math.max(val, minv), maxv);
    }

    boolean isMouseOver() {
        if (pApplet.mouseX > xPos && pApplet.mouseX < xPos+sliderPos &&
                pApplet.mouseY > yPos && pApplet.mouseY < yPos+barHeight) {
            return true;
        } else {
            return false;
        }
    }

    public void display() {
        pApplet.noStroke();
        pApplet.fill(204);
        pApplet.rect(xPos+barWidth/2, yPos, barWidth, barHeight);
        if (mouseOver || locked) {
            pApplet.fill(0, 0, 0);
        } else {
            pApplet.fill(102, 102, 102);
        }
        pApplet.rect(sliderPos+barHeight/2, yPos, barHeight, barHeight);
    }

    public float getSliderPos() {
        return (sliderPos - xPos)/barWidth;
    }
}

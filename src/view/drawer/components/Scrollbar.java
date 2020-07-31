package view.drawer.components;

import processing.core.PApplet;

public class Scrollbar {

    float xPos, yPos;       // x and y position of bar
    int barWidth, barHeight;    // width and height of bar
    float sliderPos;    // x position of slider
    float sliderMinPos, sliderMaxPos; // max and min values of slider
    boolean mouseOver;
    boolean locked;

    int value;
    int minValue;
    int maxValue;

    String title;

    PApplet pApplet;

    /**
     * Scrollbar used for parameter settings (modified from https://processing.org/examples/scrollbar.html)
     * @param applet injected from main simulation, used to draw scrollbar
     * @param x x position of scrollbar
     * @param y y position of scrollbar
     * @param width bar width
     * @param height bar height
     */
    public Scrollbar(PApplet applet, String title, float x, float y, int width, int height, int value, int minValue, int maxValue) {

        pApplet = applet;


        xPos = x;
        yPos = y;

        barWidth = width;
        barHeight = height;

        sliderMinPos = xPos;
        sliderMaxPos = xPos + barWidth - height;

        sliderPos = xPos + (float) value / (maxValue - minValue);

        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;

        this.title = title;
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
        pApplet.text(title, xPos, yPos-15);
    }

    public float getSliderPos() {
        return (sliderPos - xPos)/barWidth;
    }

    public float getValue() {
        return ((sliderPos - xPos)/barWidth)*(maxValue-minValue);
    }
}

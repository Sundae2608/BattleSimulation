package view.drawer.components;

import controller.tunable.CustomAssigner;
import processing.core.PApplet;

public class Scrollbar {

    // Top left x, y position of the scrollbar.
    float xPos;
    float yPos;

    // Height and width of the scroll bar.
    float barWidth;
    float barHeight;

    // Minimum and maximum position of the scroll bar.
    float sliderPos;
    float sliderMinPos;
    float sliderMaxPos;
    boolean mouseOver;
    boolean locked;

    // Current value, min value and max value of the scroll bar.
    double value;
    double minValue;
    double maxValue;

    // Title of the scrollbar.
    String title;

    // The applet assigned to the scrollbar.
    PApplet pApplet;

    // Custom tuner which is called every time the scrollbar is moved to assign new variables to the system.
    CustomAssigner assigner;

    /**
     * Scrollbar used for parameter settings (modified from https://processing.org/examples/scrollbar.html)
     * @param applet injected from main simulation, used to draw scrollbar
     * @param x x position of scrollbar
     * @param y y position of scrollbar
     * @param width bar width
     * @param height bar height
     */
    public Scrollbar(String inputTitle, float x, float y, int width, int height,
                     double startingValue, double inputMinValue, double inputMaxValue, PApplet applet,
                     CustomAssigner customAssigner) {
        // Assign starting value
        value = startingValue;
        minValue = inputMinValue;
        maxValue = inputMaxValue;

        // Assign the applet
        pApplet = applet;

        // Set the graphical attributes of the scroll bar
        title = inputTitle;
        xPos = x;
        yPos = y;
        barWidth = width;
        barHeight = height;
        sliderMinPos = xPos;
        sliderMaxPos = xPos + barWidth;
        sliderPos = xPos + (float) (value / (inputMaxValue - inputMinValue));

        // Assigner
        assigner = customAssigner;
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
        if (pApplet.mouseX > xPos && pApplet.mouseX < xPos + sliderPos &&
                pApplet.mouseY > yPos && pApplet.mouseY < yPos + barHeight) {
            return true;
        } else {
            return false;
        }
    }

    public void display() {
        pApplet.noStroke();
        pApplet.fill(204);
        pApplet.rect(xPos, yPos, barWidth, barHeight);
        if (mouseOver || locked) {
            pApplet.fill(0, 0, 0);
        } else {
            pApplet.fill(102, 102, 102);
        }
        pApplet.rect(sliderPos + barHeight / 2, yPos, barHeight, barHeight);
        pApplet.text(title, xPos, yPos - 15);
    }

    public double getValue() {
        double sliderFraction = (sliderPos - xPos)/barWidth;
        return sliderFraction * (maxValue - minValue) + minValue;
    }
}

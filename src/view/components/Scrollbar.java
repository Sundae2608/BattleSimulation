package view.components;

import processing.core.PApplet;

public class Scrollbar {

    // Some graphical constants
    private final float SLIDER_BAR_WIDTH = 20;
    private final float VALUE_BAR_WIDTH = 60;
    private final float VALUE_BAR_HEIGHT = 22;

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
        sliderMaxPos = xPos + barWidth - SLIDER_BAR_WIDTH;
        sliderPos = xPos + (float) (value / (inputMaxValue - inputMinValue) * (sliderMaxPos - sliderMinPos));

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
            float newSliderPos = constrain(pApplet.mouseX, sliderMinPos, sliderMaxPos);
            if (Math.abs(newSliderPos - sliderPos) > 1) {
                sliderPos = newSliderPos;
            }
        }
        assigner.updateValue(getValue());
    }

    protected float constrain(float val, float minv, float maxv) {
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
        pApplet.fill(255, 255, 255, 200);
        pApplet.rect(xPos - 10, yPos - 25, barWidth + 25, barHeight + 32);
        pApplet.fill(204);
        pApplet.rect(xPos, yPos, barWidth, barHeight);
        if (mouseOver || locked) {
            pApplet.fill(0, 0, 0);
        } else {
            pApplet.fill(102, 102, 102);
        }
        pApplet.rect(sliderPos, yPos, SLIDER_BAR_WIDTH, barHeight);
        pApplet.text(title, xPos, yPos - 8);
        if (locked) {
            pApplet.fill(0, 0, 0);
            pApplet.rect(sliderPos - VALUE_BAR_WIDTH / 2 + SLIDER_BAR_WIDTH / 2, yPos + barHeight + 5,
                    VALUE_BAR_WIDTH, VALUE_BAR_HEIGHT);
            pApplet.fill(255, 255, 255);
            pApplet.textAlign(PApplet.CENTER);
            pApplet.text(String.format("%.2f", getValue()), sliderPos + SLIDER_BAR_WIDTH / 2 + 2, yPos + barHeight + 18);
            pApplet.textAlign(PApplet.LEFT);
        }
    }

    public double getValue() {
        double sliderFraction = (sliderPos - xPos) / barWidth;
        return sliderFraction * (maxValue - minValue) + minValue;
    }
}

package view.components;

import processing.core.PApplet;

public class AsynchronousScrollbar extends Scrollbar {

    public AsynchronousScrollbar(String inputTitle,
                                 float x, float y, int width, int height,
                                 double startingValue, double inputMinValue, double inputMaxValue,
                                 PApplet applet, CustomAssigner customAssigner) {
        super(inputTitle, x, y, width, height, startingValue, inputMinValue, inputMaxValue, applet, customAssigner);
    }

    public void update() {
        mouseOver = isMouseOver();
        if (pApplet.mousePressed && mouseOver) {
            locked = true;
        }
        if (!pApplet.mousePressed) {
            if (locked) {
                locked = false;
                assigner.updateValue(getValue());
            }
        }
        if (locked) {
            float newSliderPos = constrain(pApplet.mouseX, sliderMinPos, sliderMaxPos);
            if (Math.abs(newSliderPos - sliderPos) > 1) {
                sliderPos = newSliderPos;
            }
        }
    }
}

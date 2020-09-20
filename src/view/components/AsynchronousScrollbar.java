package view.components;

import processing.core.PApplet;

public class AsynchronousScrollbar extends Scrollbar {

    public AsynchronousScrollbar(String inputTitle,
                                 float x, float y, int width, int height,
                                 double startingValue, double inputMinValue, double inputMaxValue,
                                 ScrollbarMode inputScrollbarMode,
                                 PApplet applet, CustomAssigner customAssigner) {
        super(inputTitle, x, y, width, height, startingValue, inputMinValue, inputMaxValue, inputScrollbarMode, applet,
                customAssigner);
    }

    public void update() {
        mouseOver = isMouseOver();
        if (pApplet.mousePressed && mouseOver) {
            locked = true;
        }
        if (!pApplet.mousePressed) {
            if (locked) {
                locked = false;
                assigner.updateValue(value);
            }
        }
        if (locked) {
            double tempValue = value;
            double tempSliderPos = constrain(pApplet.mouseX, sliderMinPos, sliderMaxPos);
            tempValue = getValueFromSliderPos(tempSliderPos);
            if (scrollbarMode == ScrollbarMode.INTEGER) {
                tempValue = Math.round(tempValue);
            }
            sliderPos = (float) getSliderPosFromValue(tempValue);
            value = tempValue;
        }
    }
}

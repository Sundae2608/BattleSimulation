package view.components;

import processing.core.PApplet;

public class CheckBox {

    // Top left x, y position of the scrollbar.
    float xPos;
    float yPos;

    // Height and width of the button
    float checkBoxWidth;
    float checkBoxHeight;

    // Is the mouse over the button
    boolean locked;
    boolean mouseOver;
    boolean check;

    // Button text
    String text;

    // The applet assigned to the button
    PApplet pApplet;

    // Custom procedure which is called every time the button is pressed in the system
    CustomProcedure checkProcedure;
    CustomProcedure uncheckProcedure;

    public CheckBox(String checkboxText, boolean initialValue,
                  float x, float y, int width, int height,
                  PApplet applet, CustomProcedure inputCheckProcedure, CustomProcedure inputUncheckProcedure) {
        text = checkboxText;
        check = initialValue;
        xPos = x;
        yPos = y;
        checkBoxWidth = width;
        checkBoxHeight = height;
        pApplet = applet;
        checkProcedure = inputCheckProcedure;
        uncheckProcedure = inputUncheckProcedure;
    }

    public void display() {
        pApplet.noStroke();
        pApplet.fill(255, 255, 255, 200);
        pApplet.rect(xPos - 7, yPos - 7, checkBoxWidth + 14, checkBoxHeight + 14);
        pApplet.strokeWeight(2);
        if (mouseOver || locked) {
            pApplet.stroke(50, 50, 50);
        } else {
            pApplet.stroke(102, 102, 102);
        }
        if (check) {
            if (mouseOver ||locked) {
                pApplet.fill(50, 50, 50);
            } else {
                pApplet.fill(102, 102, 102);
            }
        } else {
            pApplet.fill(233, 233, 233);
        }
        pApplet.rect(xPos, yPos, checkBoxHeight, checkBoxHeight);
        if (mouseOver || locked) {
            pApplet.fill(0, 0, 0);
        } else {
            pApplet.fill(102, 102, 102);
        }
        pApplet.textAlign(PApplet.LEFT, PApplet.CENTER);
        pApplet.text(text, xPos + checkBoxHeight + 10, yPos + checkBoxHeight / 2 - 2);
        pApplet.noStroke();
        pApplet.textAlign(PApplet.LEFT, PApplet.TOP);
    }

    public void update() {
        mouseOver = overRect(pApplet.mouseX, pApplet.mouseY);
        if (pApplet.mousePressed && mouseOver) {
            locked = true;
        }
        if (!pApplet.mousePressed) {
            if (locked) {
                locked = false;
                if (check) {
                    check = false;
                    uncheckProcedure.proc();
                } else {
                    check = true;
                    checkProcedure.proc();
                }
            }
        }
    }

    private boolean overRect(int mouseX, int mouseY)  {
        if (mouseX >= xPos && mouseX <= xPos + checkBoxWidth &&
                mouseY >= yPos && mouseY <= yPos + checkBoxHeight) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}

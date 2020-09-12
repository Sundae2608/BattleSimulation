package view.components;

import processing.core.PApplet;

public class CheckBox {

    // Top left x, y position of the scrollbar.
    float xPos;
    float yPos;

    // Height and width of the button
    float buttonWidth;
    float buttonHeight;

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

    public CheckBox(String buttonText,
                  float x, float y, int width, int height,
                  PApplet applet, CustomProcedure inputCheckProcedure, CustomProcedure inputUncheckProcedure) {
        text = buttonText;
        xPos = x;
        yPos = y;
        buttonWidth = width;
        buttonHeight = height;
        pApplet = applet;
        checkProcedure = inputCheckProcedure;
        uncheckProcedure = inputUncheckProcedure;
    }

    public void display() {
        if (mouseOver) {
            if (pApplet.mousePressed) {
                pApplet.fill(175, 175, 175);
                pApplet.strokeWeight(1);
                pApplet.stroke(50, 50, 50);
                pApplet.rect(xPos, yPos, buttonWidth, buttonHeight);

                pApplet.fill(0, 0, 0);
                pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
                pApplet.text(text, xPos + buttonWidth / 2, yPos + buttonHeight / 2 + 1);
            } else {
                pApplet.fill(200, 200, 200);
                pApplet.strokeWeight(1);
                pApplet.stroke(50, 50, 50);
                pApplet.rect(xPos, yPos, buttonWidth, buttonHeight);

                pApplet.fill(0, 0, 0);
                pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
                pApplet.text(text, xPos + buttonWidth / 2, yPos + buttonHeight / 2 - 2);
            }
        } else {
            pApplet.fill(150, 150, 150);
            pApplet.strokeWeight(1);
            pApplet.stroke(50, 50, 50);
            pApplet.rect(xPos, yPos, buttonWidth, buttonHeight);

            pApplet.fill(0, 0, 0);
            pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
            pApplet.text(text, xPos + buttonWidth / 2, yPos + buttonHeight / 2 - 2);
        }
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
                procedure.proc();
            }
        }
    }

    private boolean overRect(int mouseX, int mouseY)  {
        if (mouseX >= xPos && mouseX <= xPos + buttonWidth &&
                mouseY >= yPos && mouseY <= yPos + buttonHeight) {
            return true;
        } else {
            return false;
        }
    }
}

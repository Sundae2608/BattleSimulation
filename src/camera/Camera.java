package camera;

import singles.BaseSingle;
import utils.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class Camera {

    // Boundary extension
    // Extend the boundary by a small amount is a good practice to ensure no "sudden apperance" at the boundary
    private final static double EXTENSION = 20;

    // Position
    private double x;
    private double y;

    // Zoom (1.0 = original scale)
    private double zoom;
    private double resize;
    private double angle;

    // Screen size
    private double height;
    private double width;

    // List of troops and buildings to look at
    ArrayList<BaseSingle> troops;
    HashSet<BaseSingle> drawableTroops;

    /**
     * Initialize camera
     */
    public Camera(double inputX, double inputY, double inputWidth, double inputHeight) {
        x = inputX;
        y = inputY;
        height = inputHeight;
        width = inputWidth;
        angle = 0;
        zoom = 1.0;
        resize = 1.0;
        troops = new ArrayList<>();
    }

    /**
     * Move camera position a certain amount.
     * Positive value for dx is to move to the left
     * Positive value for dy is to move downward
     */
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /**
     * Get drawer position
     * Base on the position of the single and the camera, get the drawer position.
     */
    public double[] getDrawingPosition(double inputX, double inputY) {
        float cosAngle = MathUtils.quickCos((float) angle);
        float sinAngle = MathUtils.quickSin((float) angle);
        double drawX90  = ((inputX - x)) * zoom;
        double drawY90  = ((inputY - y)) * zoom;
        double drawX = drawX90 * cosAngle + drawY90 * sinAngle + width / 2;
        double drawY = -drawX90 * sinAngle + drawY90 * cosAngle + height / 2;
        return new double[] {drawX, drawY};
    }

    /**
     * Get drawer angle
     */
    public double getDrawingAngle(double inputAngle) {
        return inputAngle - angle;
    }

    /**
     * Get click position
     * Transform from mouse click position to actual backend position
     */
    public double[] getActualPositionFromScreenPosition(int screenX, int screenY) {
        float cosAngle = MathUtils.quickCos((float) angle);
        float sinAngle = MathUtils.quickSin((float) angle);
        double posX = screenX - width / 2;
        double posY = screenY - height / 2;
        double posX2 = (posX * cosAngle - posY * sinAngle) / zoom + x;
        double posY2 = (posX * sinAngle + posY * cosAngle) / zoom + y;
        return new double[] {posX2, posY2};
    }

    /**
     * Getter and setter
     */
    public HashSet<BaseSingle> getDrawableTroops() {
        return drawableTroops;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
        this.resize = 1 / zoom;
    }

    public double getResize() {
        return resize;
    }

    public void setResize(double resize) {
        this.resize = resize;
        this.zoom = 1 / resize;
    }

    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }
}

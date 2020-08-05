package view.camera_hex;

import model.constants.UniversalConstants;
import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventListener;
import model.singles.BaseSingle;
import model.utils.MathUtils;
import model.utils.MovementUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class HexCamera extends EventListener {

    // Boundary extension
    // Extend the boundary by a small amount is a good practice to ensure no "sudden appearance" at the boundary
    private final static double EXTENSION = 20;

    // Position
    private double x;
    private double y;
    private double xVariation;
    private double yVariation;
    private double cameraShakeLevel;

    // Zoom (1.0 = original scale)
    private double zoom;
    private double resize;
    private double angle;

    // Screen size
    private double height;
    private double width;

    /**
     * Initialize camera with no broadcaster
     */
    public HexCamera(double inputX, double inputY, double inputWidth, double inputHeight) {
        super(new EventBroadcaster());
        x = inputX;
        y = inputY;

        width = inputWidth;
        height = inputHeight;
        angle = 0;
        zoom = 1.0;
        resize = 1.0;
    }

    /**
     * Initialize view.camera
     */
    public HexCamera(double inputX, double inputY, double inputWidth, double inputHeight, EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);
        x = inputX;
        y = inputY;

        width = inputWidth;
        height = inputHeight;
        angle = 0;
        zoom = 1.0;
        resize = 1.0;
    }

    @Override
    protected void listenEvent(Event e) {
        switch (e.getEventType()) {
            case SOLDIER_CHARGE:
                cameraShakeLevel += CameraConstants.SHAKE_LEVEL_SOLDIER_CHARGE;
                break;
            case CAVALRY_CHARGE:
                cameraShakeLevel += CameraConstants.SHAKE_LEVEL_CAVALRY_CHARGE;
                break;
            case EXPLOSION:
                cameraShakeLevel += CameraConstants.SHAKE_LEVEL_EXPLOSION;
                break;
        }
        if (cameraShakeLevel > CameraConstants.SHAKE_LEVEL_MAX) {
            cameraShakeLevel = CameraConstants.SHAKE_LEVEL_MAX;
        }
    }

    /**
     * Update the stats of the camera (mainly for overtime effect such as charge)
     */
    public void update() {
        if (cameraShakeLevel > 0) {
            cameraShakeLevel -= CameraConstants.SHAKE_LEVEL_DROP;
        }
        xVariation = 1.0 * cameraShakeLevel /
                CameraConstants.SHAKE_LEVEL_AT_BASE * CameraConstants.CAMERA_SHAKE_BASE;
        xVariation = MathUtils.randDouble(-xVariation, xVariation);
        yVariation = 1.0 * cameraShakeLevel /
                CameraConstants.SHAKE_LEVEL_AT_BASE * CameraConstants.CAMERA_SHAKE_BASE;
        yVariation = MathUtils.randDouble(-yVariation, yVariation);
    }

    /**
     * Move view.camera position a certain amount.
     * Positive value for dx is to move to the left
     * Positive value for dy is to move downward
     */
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /**
     * Get drawer position
     * Base on the position of the single and the view.camera, get the drawer position.
     */
    public double[] getDrawingPosition(double inputX, double inputY) {
    }

    public double[] getDrawingPosition(double inputX, double inputY, double inputZ) {
    }

    /**
     * Return whether the input position is visible in the camera.
     * @param inputX
     * @param inputY
     * @param inputZ
     * @return
     */
    public boolean positionIsVisible(double inputX, double inputY, double inputZ) {
        double[] drawingPos = getDrawingPosition(inputX, inputY, inputZ);
        return drawingPos[0] > 0 && drawingPos[0] < width && drawingPos[1] > 0 && drawingPos[1] < height;
    }

    public boolean positionIsVisible(double inputX, double inputY) {
        double[] drawingPos = getDrawingPosition(inputX, inputY, 0.0);
        return drawingPos[0] > 0 && drawingPos[0] < width && drawingPos[1] > 0 && drawingPos[1] < height;
    }

    /*
     * Get camera z
     */
    public double getZ() {
        return (CameraConstants.MAXIMUM_ZOOM / zoom) * CameraConstants.HEIGHT_AT_MAX_ZOOM;
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
    public double[] getActualPositionFromScreenPosition(double screenX, double screenY) {
    }

    public double[] getActualPositionFromScreenPosition(double screenX, double screenY, double posZ) {
    }

    public double getActualAngleFromCameraAngle(double angleInCamera) {
        double newAngle = angleInCamera - angle;
        if (newAngle > Math.PI) newAngle -= MathUtils.PIX2;
        else if (newAngle < - Math.PI) newAngle +=MathUtils.PIX2;
        return newAngle;
    }

    public double getCameraAngleFromActualAngle(double actualAngle) {
        double newAngle = actualAngle + angle;
        if (newAngle > Math.PI) newAngle -= MathUtils.PIX2;
        else if (newAngle < - Math.PI) newAngle +=MathUtils.PIX2;
        return newAngle;
    }

    /**
     * Getter and setter
     */
    public double getX() {
        return x + xVariation;
    }

    public double getY() {
        return y + yVariation;
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

    public double getCameraShakeLevel() {
        return cameraShakeLevel;
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

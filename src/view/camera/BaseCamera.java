package view.camera;

import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventListener;
import model.utils.MathUtils;

public abstract class BaseCamera extends EventListener {

    // Boundary extension
    // Extend the boundary by a small amount is a good practice to ensure no "sudden appearance" at the boundary
    final static double EXTENSION = 20;

    // Position
    protected double x;
    protected double y;
    protected double xVariation;
    protected double yVariation;
    protected double cameraShakeLevel;

    // Zoom (1.0 = original scale)
    protected double zoom;
    protected double resize;
    protected double angle;

    // Screen size
    protected double height;
    protected double width;

    public BaseCamera(EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);
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
            case MATCHLOCK_FIRE:
                cameraShakeLevel += CameraConstants.SHAKE_LEVEL_MATCHLOCK_FIRE;
        }
        if (cameraShakeLevel > CameraConstants.SHAKE_LEVEL_MAX) {
            cameraShakeLevel = CameraConstants.SHAKE_LEVEL_MAX;
        }
    }

    /**
     * Abstract methods to get drawing positions from the actual position of the object.
     * Each camera needs to implement these methods on their own.
     */
    public abstract double[] getDrawingPosition(double inputX, double inputY);
    public abstract double[] getDrawingPosition(double inputX, double inputY, double inputZ);

    /**
     * Check whether the position is visible on camera.
     */
    public boolean positionIsVisible(double inputX, double inputY, double inputZ) {
        double[] drawingPos = getDrawingPosition(inputX, inputY, inputZ);
        return drawingPos[0] > 0 && drawingPos[0] < width && drawingPos[1] > 0 && drawingPos[1] < height;
    }

    public boolean positionIsVisible(double inputX, double inputY) {
        double[] drawingPos = getDrawingPosition(inputX, inputY, 0.0);
        return drawingPos[0] > 0 && drawingPos[0] < width && drawingPos[1] > 0 && drawingPos[1] < height;
    }

    /**
     * Check whether all the given points are visible on camera.
     * The input must be a 2D array of doubles and can either be of dimension (n x 2) or (n x 3)
     */
    public boolean boundaryPointsAreVisible(double[][] boundaryPts) {
        if (boundaryPts.length <= 0 || (boundaryPts[0].length != 2 && boundaryPts[0].length != 3)) {
            throw new RuntimeException("Input boundary points must be double[][] of size (n x 2) or (n x 3)");
        }
        if (boundaryPts[0].length == 2) {
            for (int i = 0; i < boundaryPts.length; i++) {
                if (positionIsVisible(boundaryPts[i][0], boundaryPts[i][1])) {
                    return true;
                }
            }
        } else if (boundaryPts[0].length == 3) {
            for (int i = 0; i < boundaryPts.length; i++) {
                if (positionIsVisible(boundaryPts[i][0], boundaryPts[i][1], boundaryPts[i][2])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Abstract methods to get the actual position from the screen position.
     * Each camera needs to implement these methods on their own.
     */
    public abstract double[] getActualPositionFromScreenPosition(double screenX, double screenY);
    public abstract double[] getActualPositionFromScreenPosition(double screenX, double screenY, double posZ);

    /**
     * Get zoom amount when the camera is at certain height
     */
    public abstract double getZoomAtHeight(double inputZ);

    /**
     * Return actual angle from camera angle.
     */
    public double getActualAngleFromCameraAngle(double angleInCamera) {
        double newAngle = angleInCamera + angle;
        if (newAngle > Math.PI) newAngle -= MathUtils.PIX2;
        else if (newAngle < - Math.PI) newAngle +=MathUtils.PIX2;
        return newAngle;
    }

    public double getCameraAngleFromActualAngle(double actualAngle) {
        double newAngle = actualAngle - angle;
        if (newAngle > Math.PI) newAngle -= MathUtils.PIX2;
        else if (newAngle < - Math.PI) newAngle +=MathUtils.PIX2;
        return newAngle;
    }

    /**
     * Update camera preprocess abstract method.
     */
    protected abstract void updateCameraPreprocessing();

    /**
     * Getter and setter
     */
    public double getX() {
        return x + xVariation;
    }

    public double getY() {
        return y + yVariation;
    }

    public double getZ() {
        return (CameraConstants.MAXIMUM_ZOOM / zoom) * CameraConstants.HEIGHT_AT_MAX_ZOOM;
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
        updateCameraPreprocessing();
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
        updateCameraPreprocessing();
    }

    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
        updateCameraPreprocessing();
    }
}

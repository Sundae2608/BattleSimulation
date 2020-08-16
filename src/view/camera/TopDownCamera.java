package view.camera;

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

public class TopDownCamera extends BaseCamera {

    /**
     * Initialize camera with no broadcaster
     */
    public TopDownCamera(double inputX, double inputY, double inputWidth, double inputHeight) {
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
    public TopDownCamera(double inputX, double inputY, double inputWidth, double inputHeight, EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);
        x = inputX;
        y = inputY;

        width = inputWidth;
        height = inputHeight;
        angle = 0;
        zoom = 1.0;
        resize = 1.0;
    }

    /**
     * Get drawer position
     * Base on the position of the single and the view.camera, get the drawer position.
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

    public double[] getDrawingPosition(double inputX, double inputY, double inputZ) {
        float cosAngle = MathUtils.quickCos((float) angle);
        float sinAngle = MathUtils.quickSin((float) angle);
        double zAdjustedZoom =getZoomAtHeight(inputZ);
        double drawX90 = ((inputX - this.getX())) * zAdjustedZoom;
        double drawY90 = ((inputY - this.getY())) * zAdjustedZoom;
        double drawX = drawX90 * cosAngle + drawY90 * sinAngle + width / 2;
        double drawY = -drawX90 * sinAngle + drawY90 * cosAngle + height / 2;
        return new double[]{drawX, drawY};
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

    /**
     * Get the zoom level at a specific height.
     * @param inputZ
     * @return
     */
    public double getZoomAtHeight(double inputZ) {
        double cameraHeight = (CameraConstants.MAXIMUM_ZOOM / zoom) * CameraConstants.HEIGHT_AT_MAX_ZOOM - inputZ;
        double zAdjustedZoom = CameraConstants.HEIGHT_AT_MAX_ZOOM / cameraHeight * CameraConstants.MAXIMUM_ZOOM;
        return zAdjustedZoom;
    }

    /**
     * Get click position
     * Transform from mouse click position to actual backend position
     */
    public double[] getActualPositionFromScreenPosition(double screenX, double screenY) {
        float cosAngle = MathUtils.quickCos((float) angle);
        float sinAngle = MathUtils.quickSin((float) angle);
        double posX = screenX - width / 2;
        double posY = screenY - height / 2;
        double posX2 = (posX * cosAngle - posY * sinAngle) / zoom + x;
        double posY2 = (posX * sinAngle + posY * cosAngle) / zoom + y;
        return new double[] {posX2, posY2};
    }

    public double[] getActualPositionFromScreenPosition(double screenX, double screenY, double posZ) {
        float cosAngle = MathUtils.quickCos((float) angle);
        float sinAngle = MathUtils.quickSin((float) angle);
        double posX = screenX - width / 2;
        double posY = screenY - height / 2;
        double posX2 = (posX * cosAngle - posY * sinAngle) / getZoomAtHeight(posZ) + x;
        double posY2 = (posX * sinAngle + posY * cosAngle) / getZoomAtHeight(posZ) + y;
        return new double[] {posX2, posY2};
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

    @Override
    protected void updateCameraPreprocessing() {
        // This method does not have preprocessing to update.
        return;
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

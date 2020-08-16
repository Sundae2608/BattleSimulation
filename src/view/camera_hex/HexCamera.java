package view.camera_hex;

import model.events.EventBroadcaster;
import model.utils.MathUtils;
import view.camera.BaseCamera;
import view.constants.DrawingConstants;

public class HexCamera extends BaseCamera {

    // Boundary extension
    // Extend the boundary by a small amount is a good practice to ensure no "sudden appearance" at the boundary
    private final static double EXTENSION = 20;

    // Internal offset vector
    private double[] xVector;
    private double[] yVector;
    private double[] xReverseVector;
    private double[] yReverseVector;
    private double zScale;

    // Phi angle is the angle the camera looks down on the map. This variable is unique to
    // Hex camera.
    private double phiAngle;

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
        phiAngle = Math.PI / 3;
        zoom = 1.0;
        resize = 1.0;

        updateCameraPreprocessing();
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
        phiAngle = Math.PI / 6;
        zoom = 1.0;
        resize = 1.0;

        updateCameraPreprocessing();
    }

    /**
     * Update internal vector offsets
     */
    protected void updateCameraPreprocessing() {
        xVector = new double[] {
                MathUtils.quickCos((float) angle) * zoom, -MathUtils.quickSin((float) angle) * MathUtils.quickCos((float) phiAngle) * zoom
        };
        yVector = new double[] {
                MathUtils.quickSin((float) angle) * zoom, MathUtils.quickCos((float) angle) * MathUtils.quickCos((float) phiAngle) * zoom
        };
        xReverseVector = new double[] {
                MathUtils.quickCos((float) angle) / zoom, MathUtils.quickSin((float) angle) / zoom
        };
        yReverseVector = new double[] {
                -MathUtils.quickSin((float) angle) / zoom, MathUtils.quickCos((float) angle) / zoom
        };
        zScale = MathUtils.quickSin((float) phiAngle) * zoom * DrawingConstants.HEX_TERRAIN_HEIGHT_SCALE;
    }

    /**
     * Get drawer position
     * Base on the position of the single and the view.camera, get the drawer position.
     */
    @Override
    public double[] getDrawingPosition(double inputX, double inputY) {
        double xOffset = inputX - x;
        double yOffset = inputY - y;
        return new double[] {
                width / 2 + xOffset * xVector[0] + yOffset * yVector[0],
                height / 2 + xOffset * xVector[1] + yOffset * yVector[1]
        };
    }

    @Override
    public double[] getDrawingPosition(double inputX, double inputY, double inputZ) {
        double xOffset = inputX - x;
        double yOffset = inputY - y;
        return new double[] {
                width / 2 + xOffset * xVector[0] + yOffset * yVector[0],
                height / 2 + xOffset * xVector[1] + yOffset * yVector[1] - inputZ * zScale
        };
    }

    /**
     * Get click position
     * Transform from mouse click position to actual backend position
     */
    @Override
    public double[] getActualPositionFromScreenPosition(double screenX, double screenY) {
        double offsetFromCenterX = (screenX - width / 2);
        double offsetFromCenterY = (screenY - height / 2) / MathUtils.quickCos((float) phiAngle);
        return new double[] {
            offsetFromCenterX * xReverseVector[0] + offsetFromCenterY * yReverseVector[0] + x,
            offsetFromCenterX * xReverseVector[1] + offsetFromCenterY * yReverseVector[1] + y
        };
    }

    @Override
    public double[] getActualPositionFromScreenPosition(double screenX, double screenY, double posZ) {
        double offsetScreenX = screenX - width / 2;
        double offsetScreenY = (screenY - height / 2 + posZ * zScale) / MathUtils.quickCos((float) phiAngle);
        return new double[] {
                offsetScreenX * xReverseVector[0] + offsetScreenY * yReverseVector[0] + x,
                offsetScreenX * xReverseVector[1] + offsetScreenY * yReverseVector[1] + y
        };
    }

    @Override
    public double getZoomAtHeight(double inputZ) {
        // For hexagonal camera, the zoom stays unchanged throughout.
        return zoom;
    }

    public double getPhiAngle() {
        return phiAngle;
    }

    public void setPhiAngle(double phiAngle) {
        this.phiAngle = phiAngle;
    }
}

import model.terrain.Terrain;
import model.utils.MathUtils;
import processing.core.PApplet;
import processing.event.MouseEvent;
import view.camera.BaseCamera;
import view.camera.TopDownCamera;
import view.camera.CameraConstants;
import view.constants.MapMakerConstants;
import view.drawer.InfoDrawer;
import view.drawer.MapDrawer;
import view.drawer.UIDrawer;
import view.settings.DrawingSettings;

import java.util.ArrayList;
import java.util.HashSet;

public class PCGSimulation extends PApplet {

    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    private final static int INPUT_TOP_X = 0;
    private final static int INPUT_TOP_Y = 0;
    private final static int INPUT_DIV = 400;
    private final static int INPUT_NUM_X = 50;
    private final static int INPUT_NUM_Y = 50;

    // Drawing settings
    DrawingSettings drawingSettings;

    // Key and mouse pressed set
    HashSet<Character> keyPressedSet;
    boolean leftMouseHold;
    boolean rightMouseHold;

    // Circle screen size
    double circleScreenSize;

    // Drawer
    UIDrawer uiDrawer;
    MapDrawer mapDrawer;
    InfoDrawer infoDrawer;

    // Camera
    BaseCamera camera;
    double cameraRotationSpeed;
    double cameraDx;
    double cameraDy;
    int zoomCounter;
    double zoomGoal;

    // Terrain
    Terrain terrain;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);
        drawingSettings = new DrawingSettings();
    }

    public void setup() {
        // Set up terrain.
        terrain = new Terrain(INPUT_TOP_X, INPUT_TOP_Y, INPUT_DIV, INPUT_NUM_X, INPUT_NUM_Y);

        // Set up camera.
        camera = new TopDownCamera(
                INPUT_NUM_X * INPUT_DIV / 2,
                INPUT_NUM_Y * INPUT_DIV / 2,
                INPUT_WIDTH, INPUT_HEIGHT);
        camera.setZoom(0.10);
        cameraRotationSpeed = 0;
        cameraDx = 0;
        cameraDy = 0;

        // Set up zoom.
        zoomGoal = camera.getZoom();  // To ensure consistency

        // Set up drawer
        uiDrawer = new UIDrawer(this, camera, drawingSettings);
        mapDrawer = new MapDrawer(this, camera);
        infoDrawer = new InfoDrawer(this);

        // Drawing settings
        smooth(3);
        noCursor();

        // Set of keys pressed.
        keyPressedSet = new HashSet<>();
    }

    public void draw() {

        // Clear everything
        background(230);

        // Update the camera zoom.
        if (zoomCounter >= 0) {
            zoomCounter -= 1;
            double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / CameraConstants.ZOOM_SMOOTHEN_STEPS;
            camera.setZoom(zoom);
        }

        // Update the camera rotation.
        if (keyPressed) {
            if (key == 'q') {
                cameraRotationSpeed = CameraConstants.CAMERA_ROTATION_SPEED;
            }
            if (key == 'e') {
                cameraRotationSpeed = -CameraConstants.CAMERA_ROTATION_SPEED;
            }
        }
        camera.setAngle(camera.getAngle() + cameraRotationSpeed);
        cameraRotationSpeed *= CameraConstants.CAMERA_ZOOM_DECELERATION_COEFFICIENT;

        // Update the camera translation.
        if (keyPressed) {
            double screenMoveAngle;
            cameraDx = 0;
            cameraDy = 0;
            if (keyPressedSet.contains('a')) {
                screenMoveAngle = Math.PI + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
            if (keyPressedSet.contains('d')) {
                screenMoveAngle = camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
            if (keyPressedSet.contains('w')) {
                screenMoveAngle = Math.PI * 3 / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
            if (keyPressedSet.contains('s')) {
                screenMoveAngle = Math.PI / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
        }
        camera.move(cameraDx / camera.getZoom(), cameraDy  / camera.getZoom());
        cameraDx *= CameraConstants.CAMERA_MOVEMENT_DECELERATION_COEFFICIENT;
        cameraDy *= CameraConstants.CAMERA_MOVEMENT_DECELERATION_COEFFICIENT;

        // Drawing terrain line
        mapDrawer.drawTerrainLine(terrain);

        // Draw the circle that is the drawer.
        uiDrawer.paintCircle(mouseX, mouseY);

        // Draw zoom information
        StringBuilder s = new StringBuilder();
        s.append("Camera shake level              : " + String.format("%.2f", camera.getCameraShakeLevel()) + "\n");
        s.append("Zoom level                      : " + String.format("%.2f", camera.getZoom()) + "\n");
        infoDrawer.drawTextBox(s.toString(), 5, INPUT_HEIGHT - 5, 400);

        // Change the heights of all points within the paint brush.
        double dHeight = 0;
        if (leftMouseHold) {
            dHeight = MapMakerConstants.HEIGHT_CHANGE / camera.getZoom();
        } else if (rightMouseHold) {
            dHeight = -MapMakerConstants.HEIGHT_CHANGE / camera.getZoom();
        }
        ArrayList<int[]> ptList = getPointsWithinBrush(
                terrain, mouseX, mouseY, MapMakerConstants.PAINT_CIRCLE_SIZE, camera);
        for (int[] pt : ptList) {
            int i = pt[0];
            int j = pt[1];
            terrain.changeHeightAtTile(i, j, dHeight);
        }
    }

    @Override
    public void keyPressed() {
        keyPressedSet.add(key);
    }

    @Override
    public void keyReleased() {
        keyPressedSet.remove(key);
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        float scrollVal = event.getCount();
        if (scrollVal < 0) {
            // Zoom in
            zoomGoal *= CameraConstants.ZOOM_PER_SCROLL;
            if (zoomGoal > CameraConstants.MAXIMUM_ZOOM) zoomGoal = CameraConstants.MAXIMUM_ZOOM;
        } else if (scrollVal > 0) {
            // Zoom out
            zoomGoal /= CameraConstants.ZOOM_PER_SCROLL;
            if (zoomGoal < CameraConstants.MINIMUM_ZOOM) zoomGoal = CameraConstants.MINIMUM_ZOOM;
        }
        zoomCounter = CameraConstants.ZOOM_SMOOTHEN_STEPS;
    }

    /**
     * Return an array of int[] denotings the index that were within the circle centering at (mouseX, mouseY) and has
     * radius circleSize.
     */
    private ArrayList<int[]> getPointsWithinBrush(Terrain terrain, double mouseX, double mouseY, double circleSize, BaseCamera camera) {
        // Get the set of candidates by getting all points potentially within the size of the circles.
        double circleSizeActual = circleSize / camera.getZoom();
        int sizeInNumDivs = (int) (circleSizeActual / terrain.getDiv());
        double[] actualPositions = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
        double x = actualPositions[0];
        double y = actualPositions[1];
        int xDiv = (int) Math.round(x / terrain.getDiv());
        int yDiv = (int) Math.round(y / terrain.getDiv());
        int minX = xDiv - sizeInNumDivs / 2 - 2;
        int maxX = xDiv + sizeInNumDivs / 2 + 2;
        int minY = yDiv - sizeInNumDivs / 2 - 2;
        int maxY = yDiv + sizeInNumDivs / 2 + 2;

        // Check each candidate points, with i in the range of (minX, maxX) and j in (minY, maxY). If the candidate
        // point is within the radius of the selection circle, we add them to the selection.
        double squareRadius = MathUtils.square(circleSizeActual / 2);
        ArrayList<int[]> ptList = new ArrayList<>();
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                if (i < 0 || i >= terrain.getNumX() || j < 0 || j >= terrain.getNumY()) {
                    continue;
                }
                double[] pos = terrain.getPosFromTileIndex(i, j);
                double posX = pos[0];
                double posY = pos[1];
                double squareDistance = MathUtils.squareDistance(posX, posY, x, y);
                if (squareDistance < squareRadius) {
                    ptList.add(new int[] {i, j});
                }
            }
        }

        // Return point list
        return ptList;
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT) {
            leftMouseHold = true;
        } else if (mouseButton == RIGHT) {
            rightMouseHold = true;
        }
    }

    @Override
    public void mouseReleased() {
        leftMouseHold = false;
        rightMouseHold = false;
    }

    public static void main(String... args){
        PApplet.main("PCGSimulation");
    }
}

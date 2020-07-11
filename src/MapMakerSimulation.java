import model.terrain.Terrain;
import model.utils.MathUtils;
import processing.core.PApplet;
import processing.event.MouseEvent;
import view.camera.Camera;
import view.camera.CameraConstants;
import view.drawer.InfoDrawer;
import view.drawer.MapDrawer;

import java.util.HashSet;

public class MapMakerSimulation extends PApplet {

    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    private final static int INPUT_TOP_X = 0;
    private final static int INPUT_TOP_Y = 0;
    private final static int INPUT_DIV = 400;
    private final static int INPUT_NUM_X = 50;
    private final static int INPUT_NUM_Y = 50;

    // Key pressed set
    HashSet<Character> keyPressedSet;

    // Circle screen size
    double circleScreenSize;

    // Drawer
    MapDrawer mapDrawer;
    InfoDrawer infoDrawer;

    // Camera
    Camera camera;
    double cameraRotationSpeed;
    double cameraDx;
    double cameraDy;
    int zoomCounter;
    double zoomGoal;

    // Terrain
    Terrain terrain;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);
    }

    public void setup() {
        // Set up terrain.
        terrain = new Terrain(INPUT_TOP_X, INPUT_TOP_Y, INPUT_DIV, INPUT_NUM_X, INPUT_NUM_Y);

        // Set up camera.
        camera = new Camera(
                INPUT_NUM_X * INPUT_DIV / 2,
                INPUT_NUM_Y * INPUT_DIV / 2,
                INPUT_WIDTH, INPUT_HEIGHT);
        cameraRotationSpeed = 0;
        cameraDx = 0;
        cameraDy = 0;

        // Set up zoom.
        zoomGoal = camera.getZoom();  // To ensure consistency
        mapDrawer = new MapDrawer(this);
        infoDrawer = new InfoDrawer(this);

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
        mapDrawer.drawTerrainLine(terrain, camera);

        // Draw the circle that is the drawer.


        // Draw zoom information
        StringBuilder s = new StringBuilder();
        s.append("Camera shake level              : " + String.format("%.2f", camera.getCameraShakeLevel()) + "\n");
        s.append("Zoom level                      : " + String.format("%.2f", camera.getZoom()) + "\n");
        infoDrawer.drawTextBox(s.toString(), 5, INPUT_HEIGHT - 5);
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

    public static void main(String... args){
        PApplet.main("MapMakerSimulation");
    }
}

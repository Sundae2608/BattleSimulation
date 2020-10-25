import city_gen_model.CityEnvironment;
import city_gen_model.CityState;
import city_gen_model.algorithms.geometry.house_generation.HouseGenerationSettings;
import city_gen_model.algorithms.geometry.house_generation.HouseSizeSettings;
import city_gen_model.algorithms.geometry.house_generation.HouseType;
import city_gen_model.algorithms.geometry.tree_generation.TreeGenerationSettings;
import city_gen_model.settings.CitySimulationSettings;
import model.events.EventBroadcaster;
import model.events.EventType;
import model.events.MapEvent;
import model.map_objects.House;
import model.settings.MapGenerationMode;
import model.settings.MapGenerationSettings;
import model.terrain.Terrain;
import model.utils.MathUtils;
import model.utils.PhysicUtils;
import processing.core.PApplet;
import processing.event.MouseEvent;
import utils.ConfigUtils;
import view.camera.BaseCamera;
import view.camera.CameraConstants;
import view.camera.HexCamera;
import view.components.Button;
import view.components.CustomProcedure;
import view.constants.DrawingConstants;
import view.drawer.InfoDrawer;
import view.drawer.MapDrawer;
import view.settings.DrawingSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CitySimulation extends PApplet {

    int INPUT_WIDTH = 1920;
    int INPUT_HEIGHT = 1080;

    // Drawers
    InfoDrawer infoDrawer;
    MapDrawer mapDrawer;

    // UI components
    List<Button> buttons;

    // Settings
    DrawingSettings drawingSettings;
    CitySimulationSettings citySimulationSettings;
    MapGenerationSettings mapGenerationSettings;
    HouseGenerationSettings houseGenerationSettings;
    TreeGenerationSettings treeGenerationSettings;

    // Key and mouse pressed set
    HashSet<Character> keyPressedSet;

    // City variables
    Terrain terrain;
    CityState cityState;
    CityEnvironment cityEnvironment;
    EventBroadcaster eventBroadcaster;

    // Camera
    BaseCamera camera;
    double cameraRotationSpeed;
    double cameraDx;
    double cameraDy;
    int zoomCounter;
    double zoomGoal;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT);

        // Drawing settings
        drawingSettings = new DrawingSettings();
        drawingSettings.setDrawHouses(true);

        // City simulation settings
        citySimulationSettings = new CitySimulationSettings();
        citySimulationSettings.setTimeDelaySec(0.25);

        // Map generation settings
        mapGenerationSettings = new MapGenerationSettings();
        mapGenerationSettings.setPointExtension(true);
        mapGenerationSettings.setDividePolygonProb(1.0);

        // House generation settings
        houseGenerationSettings = new HouseGenerationSettings();
        houseGenerationSettings.setMapGenerationMode(MapGenerationMode.POLYGON_BASED);
        houseGenerationSettings.setDistanceFromEdge(200);
        houseGenerationSettings.setDistanceFromEdgeWiggle(16.0);
        houseGenerationSettings.setDistanceFromOther(32.0);
        houseGenerationSettings.setDistanceFromOtherWiggle(10.0);
        houseGenerationSettings.setDistanceFromCrossRoad(200.0);

        // House size settings
        HashMap<HouseType, HouseSizeSettings> sizeSettingsMap = new HashMap<>();
        sizeSettingsMap.put(HouseType.TRIANGLE, new HouseSizeSettings(
                220.0,
                40.0,
                51200.0,
                10000.0
        ));
        sizeSettingsMap.put(HouseType.L, new HouseSizeSettings(
                320.0,
                20.0,
                102400.0,
                10000.0
        ));
        sizeSettingsMap.put(HouseType.O, new HouseSizeSettings(
                640.0,
                100.0,
                409600.0,
                100000.0
        ));
        sizeSettingsMap.put(HouseType.U, new HouseSizeSettings(
                320.0,
                20.0,
                102400.0,
                10000.0
        ));
        sizeSettingsMap.put(HouseType.H, new HouseSizeSettings(
                320.0,
                20.0,
                102400.0,
                10000.0
        ));
        sizeSettingsMap.put(HouseType.T, new HouseSizeSettings(
                225.0,
                50.0,
                51200.0,
                10000.0
        ));
        sizeSettingsMap.put(HouseType.PLUS, new HouseSizeSettings(
                320.0,
                50.0,
                102400.0,
                50000.0
        ));
        sizeSettingsMap.put(HouseType.REGULAR, new HouseSizeSettings(
                160.0,
                50.0,
                25600.0,
                10000.0
        ));
        houseGenerationSettings.setHouseTypeSizeSettings(sizeSettingsMap);

        // House type probabilities
        HashMap<HouseType, Double> houseTypeProbs = new HashMap<>();
        houseTypeProbs.put(HouseType.TRIANGLE, 0.04);
        houseTypeProbs.put(HouseType.L, 0.04);
        houseTypeProbs.put(HouseType.O, 0.015);
        houseTypeProbs.put(HouseType.U, 0.015);
        houseTypeProbs.put(HouseType.H, 0.00);
        houseTypeProbs.put(HouseType.T, 0.04);
        houseTypeProbs.put(HouseType.PLUS, 0.00);
        houseTypeProbs.put(HouseType.REGULAR, 0.85);
        houseGenerationSettings.setHouseTypeProbs(houseTypeProbs);
        mapGenerationSettings.setHouseGenerationSettings(houseGenerationSettings);

        // Tree generation settings
        treeGenerationSettings = new TreeGenerationSettings();
        treeGenerationSettings.setDistanceFromEdge(150.0);
        treeGenerationSettings.setDistanceFromEdgeWiggle(40.0);
        treeGenerationSettings.setDistanceFromOther(250.0);
        treeGenerationSettings.setDistanceFromOtherWiggle(150.0);
        treeGenerationSettings.setDistanceFromCrossRoad(150.0);
        treeGenerationSettings.setSize(80);
        treeGenerationSettings.setSizeWiggle(30.0);
        mapGenerationSettings.setTreeGenerationSettings(treeGenerationSettings);

    }

    public void setup() {

        // Initialize city environment
        eventBroadcaster = new EventBroadcaster();
        String cityStateParamsConfig = "src/configs/city_configs/city_state.json";
        try {
            cityState = new CityState(eventBroadcaster, ConfigUtils.readCityStateParameters(cityStateParamsConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            terrain = ConfigUtils.createTerrainFromConfig(cityStateParamsConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cityEnvironment = new CityEnvironment(cityState, terrain, eventBroadcaster, mapGenerationSettings);

        // Set up progression model
        buttons = new ArrayList<>();
        buttons.add(new Button("Trigger Decay Event",
                INPUT_WIDTH -180, INPUT_HEIGHT -50, 170, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { eventBroadcaster.broadcastEvent(
                            new MapEvent(EventType.DESTROY_CITY,
                                    0, 0, 0, 50, 500)); }
                }));

        // Set up camera.
        camera = new HexCamera(
                terrain.getTopX() + terrain.getNumX() * terrain.getDiv() / 2,
                terrain.getTopY() + terrain.getNumY() * terrain.getDiv() / 2,
                INPUT_WIDTH, INPUT_HEIGHT);
        camera.setZoom(0.10);
        cameraRotationSpeed = 0;
        cameraDx = 0;
        cameraDy = 0;
        zoomGoal = camera.getZoom();  // To ensure consistency
        // Update the camera zoom.
        if (zoomCounter >= 0) {
            zoomCounter -= 1;
            double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / CameraConstants.ZOOM_SMOOTHEN_STEPS;
            camera.setZoom(zoom);
        }

        // Setup key pressed set.
        keyPressedSet = new HashSet<>();

        // Initialize all drawers
        infoDrawer = new InfoDrawer(this);
        mapDrawer = new MapDrawer(this, camera);
    }

    public void draw() {

        /**
         * Update backend
         */
        // Update all backend
        for (Button b : buttons) {
            b.update();
        }
        cityEnvironment.step();

        /**
         * Update camera
         */
        // Update the camera zoom.
        if (zoomCounter >= 0) {
            zoomCounter -= 1;
            double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / CameraConstants.ZOOM_SMOOTHEN_STEPS;
            camera.setZoom(zoom);
        }

        // Update the camera rotation.
        if (keyPressed) {
            if (keyPressedSet.contains('q')) {
                cameraRotationSpeed = CameraConstants.CAMERA_ROTATION_SPEED;
            }
            if (keyPressedSet.contains('e')) {
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

        /**
         * Drawing
         */
        background(255);

        // Draw terrain
        mapDrawer.drawTerrainLine(terrain);

        // Draw houses
        if (drawingSettings.isDrawHouses()) {
            int[] color = DrawingConstants.NORMAL_POLYGON_COLOR;
            for (House h : cityEnvironment.getHouses()) {
                double[][] pts = h.getBoundaryPoints();
                if (camera.boundaryPointsAreVisible(pts)) {
                    // Determine the color of the polygon
                    double[] mousePosition = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                    if (PhysicUtils.checkPolygonPointCollision(pts, mousePosition[0], mousePosition[1])) {
                        strokeWeight(1);
                        stroke(color[0],color[1],color[2], 60);
                        fill(color[0],color[1],color[2],60);
                    } else {
                        strokeWeight(1);
                        stroke(color[0],color[1],color[2], 36);
                        fill(color[0],color[1],color[2],35);
                    }
                    beginShape();
                    for (int i = 0; i < pts.length; i++) {
                        double x = pts[i][0];
                        double y = pts[i][1];
                        double[] drawingPt = camera.getDrawingPosition(x, y, terrain.getZFromPos(x, y));
                        vertex((float) drawingPt[0], (float) drawingPt[1]);
                    }
                    endShape(CLOSE);
                }
            }
        }

        infoDrawer.drawTextBox("Number of Houses: " +
                cityState.getCityStateParameters().getNumHouses(), 20, INPUT_HEIGHT -20, 200);
        for (Button b : buttons) {
            b.display();
        }
    }

    public static void main(String... args){
        PApplet.main("CitySimulation");
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
}

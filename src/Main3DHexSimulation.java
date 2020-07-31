import model.GameEnvironment;
import model.checker.EnvironmentChecker;
import model.logger.Log;
import model.monitor.MonitorEnum;
import model.settings.GameSettings;
import model.units.BaseUnit;
import model.utils.MathUtils;
import model.utils.UnitUtils;
import processing.core.PApplet;
import processing.sound.SoundFile;
import utils.ConfigUtils;
import view.audio.AudioSpeaker;
import view.camera.Camera;
import view.camera.CameraConstants;
import view.drawer.*;
import view.settings.AudioSettings;
import view.settings.DrawingMode;
import view.settings.DrawingSettings;
import view.settings.RenderMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class Main3DHexSimulation extends PApplet {

    // Screen constants
    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    /** Key pressed set */
    HashSet<Character> keyPressedSet;

    /** Drawers */
    UIDrawer uiDrawer;
    InfoDrawer infoDrawer;

    /** Sound variables */
    AudioSpeaker audioSpeaker;
    SoundFile backgroundMusic;

    /** Game variables */
    GameSettings gameSettings;
    GameEnvironment env;

    // Camera
    Camera camera;
    double cameraRotationSpeed;
    double cameraDx;
    double cameraDy;

    // Some graphical settings
    DrawingSettings drawingSettings;
    AudioSettings audioSettings;
    int zoomCounter;
    double zoomGoal;
    int planCounter;

    // Time recorder
    long lastTime;
    long backEndTime;
    long graphicTime;

    // Current playing state
    boolean currentlyPaused;

    // Control variable
    BaseUnit unitSelected;
    boolean rightClickedNotReleased;
    double rightClickActualX;
    double rightClickActualY;
    double unitEndPointX;
    double unitEndPointY;
    double unitEndAngle;
    BaseUnit closestUnit;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);

        // First log to initialize the logging tool
        Log.info("Initialize the log");

        // Window size
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);

        // Game settings
        gameSettings = new GameSettings();
        gameSettings.setApplyTerrainModifier(true);
        gameSettings.setBorderInwardCollision(false);
        gameSettings.setAllyCollision(true);
        gameSettings.setCollisionCheckingOnlyInCombat(false);
        gameSettings.setCavalryCollision(true);
        gameSettings.setEnableFlankingMechanics(true);
        gameSettings.setCountWrongFormationChanges(true);

        // Drawing settings
        drawingSettings = new DrawingSettings();
        drawingSettings.setRenderMode(RenderMode.MINIMALISTIC);
        drawingSettings.setDrawEye(DrawingMode.NOT_DRAW);
        drawingSettings.setDrawWeapon(DrawingMode.DRAW);
        drawingSettings.setProduceFootage(false);
        drawingSettings.setFrameSkips(0);
        drawingSettings.setDrawGrid(false);
        drawingSettings.setDrawSurface(false);
        drawingSettings.setSmoothCameraMovement(true);
        drawingSettings.setSmoothRotationSteps(40);
        drawingSettings.setSmoothPlanShowingSteps(100);
        drawingSettings.setDrawHeightField(true);
        drawingSettings.setDrawMapTexture(false);
        drawingSettings.setDrawSmooth(true);
        drawingSettings.setDrawDamageSustained(true);
        drawingSettings.setDrawTroopShadow(true);
        drawingSettings.setDrawSimplifiedTroopShape(true);
        drawingSettings.setDrawIcon(true);
        drawingSettings.setDrawVideoEffect(true);

        // Audio settings
        audioSettings = new AudioSettings();
        audioSettings.setBackgroundMusic(false);
        audioSettings.setSoundEffect(false);

        // Post-processing settings.
        if (!drawingSettings.isDrawSmooth()) noSmooth();
        else {
            smooth(4);
        }
    }

    public void setup() {
        /** Set up game config */
        // Create a new game based on the input configurations.
        String battleConfig = "src/configs/battle_configs/PhalanxTest.txt";
        String mapConfig = "src/configs/map_configs/ConfigWithTextureMap.txt";
        String constructsConfig = "src/configs/construct_configs/ConstructsMapConfig.txt";
        String surfaceConfig = "src/configs/surface_configs/NoSurfaceConfig.txt";
        String gameConfig = "src/configs/game_configs/GameConfig.txt";
        env = new GameEnvironment(gameConfig, mapConfig, constructsConfig, surfaceConfig, battleConfig, gameSettings);

        // Check to make sure that the game environment is valid
        try {
            EnvironmentChecker.checkEnvironmentValid(env);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** Keyboard setup */
        keyPressedSet = new HashSet<>();

        /** Camera setup */
        // Add unit to view.camera
        camera = new Camera(15000, 20000, INPUT_WIDTH, INPUT_HEIGHT,
                env.getBroadcaster());
        cameraRotationSpeed = 0;
        cameraDx = 0;
        cameraDy = 0;
        zoomGoal = camera.getZoom();  // To ensure consistency

        /** Drawer setup */
        uiDrawer = new UIDrawer(this, camera, drawingSettings);
        infoDrawer = new InfoDrawer(this);

        /** Set up audios */
        // Set up audio speaker
        try {
            audioSpeaker = ConfigUtils.readAudioConfigs(
                    "src/configs/audio_configs/AudioConfig.txt",
                    camera, this, env.getBroadcaster()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load background music
        if (audioSettings.isBackgroundMusic()) {
            backgroundMusic = new SoundFile(this, "audios/bg_music/bg1.mp3");
            backgroundMusic.amp(0.15f);
            backgroundMusic.loop();
        }

        // Playing state
        currentlyPaused = false;
    }

    /**
     * Looping method required for a Processing applet.
     */
    public void draw() {
        /** Update the backend
         * All backend processing of the games will be done here, along with several other time-keeping activities*/
        // Record time
        lastTime = System.nanoTime();

        if (!currentlyPaused) {
            // The environment makes one step forward in processing.
            for (int i = 0; i < drawingSettings.getFrameSkips() + 1; i++) {
                env.step();
            }
        }

        if (!currentlyPaused) {
            camera.update();
        }

        // Record backend time
        backEndTime = System.nanoTime() - lastTime;

        // Process some controller variables (such as unitSelected) to ensure there is no frontend bugs.
        // Common front end bugs include processing the unit when it is already dead.
        if (env.getDeadUnits().contains(unitSelected)) {
            unitSelected = null;
        }

        /** Update some graphical elements.
         * Some graphic elements of the game is extracted and pre-processed here. These include:
         * - Camera position updates.
         * - Smooth zoom update (if enabled).
         * - Unit size optimization.
         * - Update nearest unit to mouse cursor.
         */
        if (drawingSettings.isSmoothCameraMovement()) {
            // Update the camera zoom.
            if (zoomCounter >= 0) {
                zoomCounter -= 1;
                double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / CameraConstants.ZOOM_SMOOTHEN_STEPS;
                camera.setZoom(zoom);
            }

            // Update the camera angle.
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
        }

        // Update view.camera keyboard movement
        double screenMoveAngle;
        if (keyPressed) {
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
        camera.move(cameraDx / camera.getZoom(), cameraDy / camera.getZoom());
        cameraDx *= CameraConstants.CAMERA_MOVEMENT_DECELERATION_COEFFICIENT;
        cameraDy *= CameraConstants.CAMERA_MOVEMENT_DECELERATION_COEFFICIENT;

        // Pick the closest to the mouse
        double[] mousePositions = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
        double minDist = Double.MAX_VALUE;
        closestUnit = env.getUnits().get(0);
        for (BaseUnit unit : env.getUnits()) {
            double dist = MathUtils.squareDistance(unit.getAverageX(),
                    unit.getAverageY(),
                    mousePositions[0],
                    mousePositions[1]);
            if (dist < minDist) {
                minDist = dist;
                closestUnit = unit;
            }
        }

        /** Draw the game graphics
         * This section contains all code regarding the drawing of the game graphics, but excluding the UI. These
         * include:
         * - Map terrain.
         * - Map grid (for better coordinate positioning.
         * - Map texture.
         * - Map surfaces (Grass, desert, etc for debugging purpose
         * - Dead troops.
         * - Alive troops. (We draw alive troops later because alive troops step on dead troops)
         * - Constructs.
         * - Node system. (For path finding debugging).
         */


        /** Process the unit sound
         * Contains all codes regarding the processing of audio. At the moment, we only rely on the audio speaker to
         * process the audio for us.
         */
        if (audioSettings.isSoundEffect()) {
            audioSpeaker.processEvents();
        }

        /** Draw the UI.
         * This section contains all the HUD element of the game. These include:
         * - Unit banner.
         * - Game resource and debugging statistics
         * - Game tuner.
         */
        // Draw all the unit icon
        ArrayList<BaseUnit> unitsSortedByPosition = new ArrayList<>(env.getAliveUnits());
        Collections.sort(unitsSortedByPosition, new Comparator<BaseUnit>() {
            @Override
            public int compare(BaseUnit o1, BaseUnit o2) {
                double y1 = camera.getDrawingPosition(o1.getAverageX(), o1.getAverageY())[1];
                double y2 = camera.getDrawingPosition(o2.getAverageX(), o2.getAverageY())[1];
                return Double.compare(y1, y2);
            }
        });
        if (drawingSettings.isDrawIcon()) {
            for (BaseUnit unit : unitsSortedByPosition) {
                if (unit.getNumAlives() == 0) continue;
                boolean isSelected = unit == unitSelected;
                uiDrawer.drawUnitBanner(unit, isSelected);
            }
        }

        // Information about the closest unit on top left corner.
        fill(0, 0, 0, 200);
        rect(0, 0, 400, 150);
        fill(255, 255, 255);
        textAlign(LEFT);
        text(UnitUtils.getUnitName(closestUnit), 8, 15);
        text("Unit state: ", 8, 35); text(closestUnit.getState().toString(), 100, 35);
        text("Strength: ", 8, 50); text(String.valueOf(closestUnit.getNumAlives()) + "/" + String.valueOf(closestUnit.getTroops().size()), 100, 50);
        text("Stamina: ", 8, 65); text(String.format("%.2f", closestUnit.getStamina()), 100, 65);

        // Process graphics
        fill(0, 0, 0);
        graphicTime = System.nanoTime() - lastTime - backEndTime;

        // Write all the interesting counters here.
        StringBuilder s = new StringBuilder();
        s.append(env.getMonitor().getCounterString(
                new MonitorEnum[] {
                        MonitorEnum.COLLISION_TROOPS,
                        MonitorEnum.COLLISION_TROOP_AND_TERRAIN,
                        MonitorEnum.COLLISION_TROOP_AND_CONSTRUCT,
                        MonitorEnum.COLLISION_TROOP_AND_TREE,
                        MonitorEnum.COLLISION_OBJECT,
                }
        ));
        s.append(env.getMonitor().getTotalCounterString(
                new MonitorEnum[] {
                        MonitorEnum.WRONG_FORMATION_CHANGES,
                }
        ));
        s.append("Camera shake level              : " + String.format("%.2f", camera.getCameraShakeLevel()) + "\n");
        s.append("Zoom level                      : " + String.format("%.2f", camera.getZoom()) + "\n");
        s.append("Backends                        : " + String.format("%.2f", 1.0 * backEndTime / 1000000) + "ms\n");
        s.append("Graphics                        : " + String.format("%.2f", 1.0 * graphicTime / 1000000) + "ms\n");
        s.append("FPS                             : " + String.format("%.2f", 1.0 * 1000000000 / (graphicTime + backEndTime)));

        infoDrawer.drawTextBox(s.toString(), 5, INPUT_HEIGHT - 5, 500);

        // Pause / Play Button
        if (!currentlyPaused) {
            uiDrawer.pauseButton(INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        } else {
            uiDrawer.playButton(INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        }
    }

    public static void main(String... args){
        PApplet.main("Main3DHexSimulation");
    }
}

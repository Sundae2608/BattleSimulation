import model.GameEnvironment;
import model.checker.EnvironmentChecker;
import model.logger.Log;
import model.settings.GameSettings;
import model.units.BaseUnit;
import processing.core.PApplet;
import processing.sound.SoundFile;
import utils.ConfigUtils;
import view.audio.AudioSpeaker;
import view.camera.Camera;
import view.drawer.*;
import view.settings.AudioSettings;
import view.settings.DrawingMode;
import view.settings.DrawingSettings;
import view.settings.RenderMode;

import java.io.IOException;
import java.util.HashSet;

public class Main3DHexSimulation extends PApplet {

    // Screen constants
    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    /** Key pressed set */
    HashSet<Character> keyPressedSet;

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

        // -------------
        // Game settings
        // -------------
        gameSettings = new GameSettings();
        gameSettings.setApplyTerrainModifier(true);
        gameSettings.setBorderInwardCollision(false);
        gameSettings.setAllyCollision(true);
        gameSettings.setCollisionCheckingOnlyInCombat(false);
        gameSettings.setCavalryCollision(true);
        gameSettings.setEnableFlankingMechanics(true);
        gameSettings.setCountWrongFormationChanges(true);

        // ----------------
        // Graphic settings
        // ----------------
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

        /** Keyboard initialization */
        keyPressedSet = new HashSet<>();

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
        /** Update the backend */
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
    }
}

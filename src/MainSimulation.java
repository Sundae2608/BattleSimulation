import cern.colt.function.tint.IntIntFunction;
import cern.colt.matrix.tint.IntMatrix2D;
import cern.colt.matrix.tint.impl.DenseIntMatrix2D;
import drawer.UIDrawer;
import drawer.ShapeDrawer;
import javafx.util.Pair;
import map.Tile;
import algorithms.UnitModifier;
import camera.Camera;
import constants.*;
import processing.core.PGraphics;
import settings.*;
import drawer.DrawingVertices;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;
import processing.sound.SoundFile;
import singles.*;
import units.*;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainSimulation extends PApplet {

    // -------------------
    // Universal Constants
    // -------------------
    private final static int INPUT_WIDTH = 2560;
    private final static int INPUT_HEIGHT = 1440;

    // ------------------
    // Drawers
    // This helps store each special shape at size to save time.
    // TODO: Wrap around this into an optimizer object for scalability
    // ------------------

    // All Drawers
    UIDrawer uiDrawer;
    ShapeDrawer shapeDrawer;

    // Eye optimizer
    HashMap<Double, Double> eyeSizeMap;
    double currSizeEye;

    // Unit optimizers
    private final static int INDEX_TROOP_SIZE = 0;
    private final static int INDEX_SHADOW_SIZE = 1;
    private final static int INDEX_TROOP_SIMPLIED_SIZE = 2;
    private final static int INDEX_SHADOW_SIMPLIED_SIZE = 3;

    HashMap<Double, double[]> swordmanSizeMap;
    double[] currSizeSwordman;

    HashMap<Double, double[]> phalanxSizeMap;
    double[] currSizePhalanx;

    HashMap<Double, double[]> archerSizeMap;
    double[] currSizeArcher;

    HashMap<Double, double[]> slingerSizeMap;
    double[] currSizeSlinger;

    HashMap<Double, double[]> skirmisherSizeMap;
    double[] currSizeSkirmisher;

    HashMap<Double, DrawingVertices> cavalryShapeMap;
    HashMap<Double, double[]> cavalrySizeMap;
    double[] currSizeCavalry;

    HashMap<Double, double[]> horseArcherSizeMap;
    double[] currSizeHorseArcher;

    HashMap<BaseUnit, Pair<PImage, IntMatrix2D>> unitSimplifiedImageMap;
    HashMap<BaseUnit, Pair<PImage, IntMatrix2D>> unitShadowSimplifiedImageMap;

    double shadowXOffset;
    double shadowYOffset;
    double unitShadowXOffset;
    double unitShadowYOffset;

    // -----------
    // Sound files
    // -----------

    SoundFile soundLeftClick, soundRightClick;
    SoundFile soundArgh;

    SoundFile soundCombat;
    float combatAmplitude;

    SoundFile soundFootMarch;
    float footMarchAmplitude;

    SoundFile soundCavalryMarch;
    float cavalryMarchAplitude;

    SoundFile backgroundMusic;

    // -----------
    // Image files
    // -----------

    // UI Icons
    PImage iconCav, iconSpear, iconSword, iconArcher, iconSlinger, iconHorseArcher, iconSkirmisher;
    PImage iconCavUnsel, iconSpearUnsel, iconSwordUnsel, iconArcherUnsel,
            iconSlingerUnsel, iconHorseArcherUnsel, iconSkirmisherUnsel;
    PImage banner, bannerShadow, bannerSelected, bannerTexture;

    // Tiles
    // TODO(sonpham): This is deprcated
    PImage tileGrass;

    // List of all tiles
    private ArrayList<Tile> tiles;

    // --------------------
    // Game variables
    // Variable necessary for the running of the game
    // --------------------

    // Troops and Objects
    ArrayList<BaseUnit> units;
    UnitModifier unitModifier;
    ArrayList<BaseSingle> deadContainer;

    // Cameraqwe
    Camera camera;

    // Some drawingSettings
    DrawingSettings drawingSettings;
    AudioSettings audioSettings;
    GameSettings gameSettings;
    ExperimentSettings experimentSettings;
    int zoomCounter;
    double zoomGoal;
    int angleCounter;
    double angleGoal;
    int planCounter;

    // Time recorder
    long lastTime;
    long backEndTime;
    long graphicTime;

    // Current playing state
    boolean currentlyPaused;

    // Control variable
    BaseUnit unitSelected;
    BaseUnit closestUnit;

    public void settings() {

        // Window size
        size(INPUT_WIDTH, INPUT_HEIGHT, OPENGL);

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
        drawingSettings.setSmoothCameraMovement(true);
        drawingSettings.setSmoothCameraSteps(100);
        drawingSettings.setSmoothRotationSteps(40);
        drawingSettings.setSmoothPlanShowingSteps(100);
        drawingSettings.setDrawMap(true);
        drawingSettings.setDrawSmooth(true);
        drawingSettings.setDrawDamageSustained(true);
        drawingSettings.setDrawTroopShadow(true);
        drawingSettings.setDrawSimplifiedTroopShape(true);
        drawingSettings.setDrawIcon(true);
        drawingSettings.setInPositionOptimization(false);

        // Initializee drawer
        uiDrawer = new UIDrawer();
        shapeDrawer = new ShapeDrawer();

        // Graphic storage
        cavalryShapeMap = new HashMap<>();

        eyeSizeMap = new HashMap<>();
        phalanxSizeMap = new HashMap<>();
        slingerSizeMap = new HashMap<>();
        archerSizeMap = new HashMap<>();
        skirmisherSizeMap = new HashMap<>();
        swordmanSizeMap = new HashMap<>();
        cavalrySizeMap = new HashMap<>();
        horseArcherSizeMap = new HashMap<>();

        unitSimplifiedImageMap = new HashMap<>();
        unitShadowSimplifiedImageMap = new HashMap<>();

        // --------------
        // Audio settings
        // --------------
        audioSettings = new AudioSettings();
        audioSettings.setBackgroundMusic(true);
        audioSettings.setSoundEffect(true);

        // -------------
        // Game Settings
        // -------------
        // TODO: Add game settings here

        // -------------------
        // Experiment settings
        // -------------------
        experimentSettings = new ExperimentSettings();
        experimentSettings.setCavalryCollision(false);
        experimentSettings.setBorderInwardCollision(true);
        experimentSettings.setDrawTroopInDanger(false);
        experimentSettings.setDrawTroopInPosition(false);

        // ------------------------
        // Post processing settings
        // ------------------------
        if (!drawingSettings.isDrawSmooth()) noSmooth();
        else {
            smooth(4);
        }
    }

    public void setup() {

        // ----------------------
        // Load graphic resources
        // ----------------------
        iconSword = loadImage("imgs/BannerArt/iconSword.png");
        iconSpear = loadImage("imgs/BannerArt/iconSpear.png");
        iconCav = loadImage("imgs/BannerArt/iconCav.png");
        iconArcher = loadImage("imgs/BannerArt/iconArcher.png");
        iconHorseArcher = loadImage("imgs/BannerArt/iconHorseArcher.png");
        iconSlinger = loadImage("imgs/BannerArt/iconSlinger.png");
        iconSkirmisher = loadImage("imgs/BannerArt/iconSkirmisher.png");

        iconSwordUnsel = loadImage("imgs/SelectedIcons/SwordUnsel.png");
        iconSpearUnsel = loadImage("imgs/SelectedIcons/SpearUnsel.png");
        iconCavUnsel = loadImage("imgs/SelectedIcons/CavUnsel.png");
        iconArcherUnsel = loadImage("imgs/SelectedIcons/ArcherUnsel.png");
        iconHorseArcherUnsel = loadImage("imgs/SelectedIcons/HorseArcherUnsel.png");
        iconSlingerUnsel = loadImage("imgs/SelectedIcons/SlingerUnsel.png");
        iconSkirmisherUnsel = loadImage("imgs/SelectedIcons/SkirmisherUnsel.png");

        banner = loadImage("imgs/BannerArt/banner.png");
        bannerShadow = loadImage("imgs/BannerArt/bannerShadow.png");
        bannerTexture = loadImage("imgs/BannerArt/bannerTexture.png");
        bannerSelected = loadImage("imgs/BannerArt/bannerSelected.png");

        tileGrass = loadImage("imgs/SelectedTiles/grassTile128.png");

        // Load all tiles in the map
        loadMapTiles(-9000, -18000, "imgs/MapTiles/pharsalus", 1080);

        // ---------------
        // Load sound file
        // ---------------
        soundLeftClick = new SoundFile(this, "audios/click/click2.mp3");
        soundLeftClick.amp(0.7f);

        soundRightClick = new SoundFile(this, "audios/click/click1.mp3");
        soundRightClick.amp(0.7f);

        soundArgh = new SoundFile(this, "audios/argh/argh1_extreme.mp3");

        soundCombat = new SoundFile(this, "audios/unit_sound/medieval_combat.mp3");
        combatAmplitude = AudioConstants.MIN_AMPLITUDE;
        soundCombat.amp(combatAmplitude);
        soundCombat.loop();

        soundFootMarch = new SoundFile(this, "audios/unit_sound/footrunning.mp3");
        footMarchAmplitude = AudioConstants.MIN_AMPLITUDE;
        soundFootMarch.amp(footMarchAmplitude);
        soundFootMarch.loop();

        soundCavalryMarch = new SoundFile(this, "audios/unit_sound/cavalrymarch.mp3");
        cavalryMarchAplitude = AudioConstants.MIN_AMPLITUDE;
        soundCavalryMarch.amp(cavalryMarchAplitude);
        soundCavalryMarch.loop();

        // Load background music
        if (audioSettings.isBackgroundMusic()) {
            backgroundMusic = new SoundFile(this, "audios/bg_music/bg1.mp3");
            backgroundMusic.amp(0.15f);
            backgroundMusic.loop();
        }

        // -------------------
        // Preprocesing troops
        // -------------------

        // Create the Collision Modifier
        deadContainer = new ArrayList<>();
        unitModifier = new UnitModifier(deadContainer, experimentSettings);

        // Create troops from config file and add to collision modifier
        String configFile = "src/battle_configs/RomeVsGaulComplicated.txt";
        try {
            units = ConfigUtils.readConfigs(configFile, unitModifier.getObjectHasher());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (BaseUnit unit : units) {
            unitModifier.addUnit(unit);
        }

        // Create a simplified image version of each unit.
        preprocessSimplifiedUnitImages();

        // Add unit to camera
        camera = new Camera(INPUT_WIDTH / 2, INPUT_HEIGHT / 2, INPUT_WIDTH, INPUT_HEIGHT);
        zoomGoal = camera.getZoom();  // To ensure consistency
        angleGoal = camera.getAngle();

        // Playing state
        currentlyPaused = false;

        // Some graphic set up
        rectMode(CENTER);
    }

    public void draw() {

        // -------------------
        // Update the back end
        // -------------------

        // Record time
        lastTime = System.currentTimeMillis();

        if (!currentlyPaused) {
            // Update backend
            unitModifier.getObjectHasher().updateObjects();
            for (BaseUnit unit : units) unit.updateIntention();
            unitModifier.modifyObjects();
            for (BaseUnit unit : units) unit.updateState();
        }

        // Record backend time
        backEndTime = System.currentTimeMillis() - lastTime;

        // ----------------------------------------
        // Update some graphical elements
        // Some graphic elements of the game is extracted and pre-processed here.
        // - Camera position updates.
        // - Smooth zoom update (if configured)
        // - Circle Size optimization
        // - Update nearest unit to mouse cursor
        // ----------------------------------------

        // Frame skipper
        if (drawingSettings.getFrameSkips() != 0 & frameCount % (drawingSettings.getFrameSkips() + 1) == 0) return;

        // Update camera smooth zoom
        if (drawingSettings.isSmoothCameraMovement()) {
            if (zoomCounter >= 0) {
                zoomCounter -= 1;
                double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / drawingSettings.getSmoothCameraSteps();
                camera.setZoom(zoom);
            }
            if (keyPressed) {
                if (key == 'q') camera.setAngle(camera.getAngle() + UniversalConstants.CAMERA_ROTATION_SPEED);
                if (key == 'e') camera.setAngle(camera.getAngle() - UniversalConstants.CAMERA_ROTATION_SPEED);
            }
        }

        // Update camera keyboard movement
        double screenMoveAngle;
        if (keyPressed) {
            if (key == 'a') {
                screenMoveAngle = Math.PI + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
            if (key == 'd') {
                screenMoveAngle = camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
            if (key == 'w') {
                screenMoveAngle = Math.PI * 3 / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
            if (key == 's') {
                screenMoveAngle = Math.PI / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        UniversalConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
        }

        // Circle size optimization. This is to avoid repeat calculation of troop size
        updateSizeMaps();

        // Precalculate shadow, also for optimization
        if (drawingSettings.isDrawTroopShadow()) {
            shadowXOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.SHADOW_OFFSET * camera.getZoom();
            shadowYOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.SHADOW_OFFSET * camera.getZoom();
            unitShadowXOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
            unitShadowYOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
        }

        // Pick the closest to the mouse
        double[] mousePositions = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
        double minDist = Double.MAX_VALUE;
        closestUnit = units.get(0);
        for (BaseUnit unit : units) {
            double dist = MathUtils.squareDistance(unit.getAverageX(),
                    unit.getAverageY(),
                    mousePositions[0],
                    mousePositions[1]);
            if (dist < minDist) {
                minDist = dist;
                closestUnit = unit;
            }
        }

        // -----------------
        // Draw the graphics
        // -----------------

        // Clear everything
        background(230);

        // First, draw the map
        if (drawingSettings.isDrawMap()) {
            drawMapTiles(tiles, camera);
        }

        // Begin loop for columns
        if (drawingSettings.isDrawGrid()) {
            double[] drawPos = camera.getDrawingPosition(0.0, 0.0);
            double drawX = drawPos[0];
            double drawY = drawPos[1];
            float spacing = (float) (DrawingConstants.GRID_SIZE * (camera.getZoom()));
            float[] color = DrawingConstants.GRID_COLOR;
            stroke(color[0], color[1], color[2], color[3]);
            strokeWeight(1);
            for (float i = (float) drawX % spacing - spacing; i <= width; i += spacing) {
                line(i, 0, i, height);
            }
            for (float j = (float) drawY % spacing - spacing; j <= height; j += spacing) {
                line(0, j, width, j);
            }
            noStroke();
        }

        // Draw the grass tile.
        if (drawingSettings.getRenderMode() == RenderMode.REALISTIC) {
            double[] drawPos = camera.getDrawingPosition(0.0, 0.0);
            double drawX = drawPos[0];
            double drawY = drawPos[1];
            float drawWidth = (float) (tileGrass.width * (camera.getZoom()));
            float drawHeight = (float) (tileGrass.height * (camera.getZoom()));
            for (float i = (float) drawX % drawWidth - drawWidth; i <= width ; i += drawWidth) {
                for (float j = (float) drawY % drawHeight - drawHeight; j <= height; j += drawHeight) {
                    image(tileGrass, i, j, drawWidth, drawHeight);
                }
            }
        }

        // Dead troops
        noStroke();
        for (BaseSingle single : deadContainer) {
            portrayDeadSingle(single, camera, drawingSettings);
        }

        // If space is pressed, draw the goal position.
        if (keyPressed) {
            if (key == TAB) {
                planCounter = drawingSettings.getSmoothPlanShowingSteps();
            }
        }

        if (planCounter > 0) {
            for (BaseUnit unit : units) {
                if (unit.getState() == UnitState.MOVING) {
                    int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
                    fill(color[0], color[1], color[2], (int) (Math.min(1.0 * planCounter / 30, 0.90) * 255));
                    drawArrowPlan(unit, camera, drawingSettings);
                }
            }
            planCounter -= 1;
        }

        if (camera.getZoom() > UniversalConstants.ZOOM_RENDER_LEVEL_TROOP) {
            // Alive troop
            for (BaseUnit unit : units) {
                boolean unitHovered = unit == closestUnit;
                // First, draw the optimize masked version for troops in position
                if (drawingSettings.isInPositionOptimization() &&
                        camera.getZoom() < UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE) {
                    drawMaskedInPositionUnit(unit, camera, drawingSettings);
                }
                // For troops out of position, draw them individually
                for (BaseSingle single : unit.getTroops()) {
                    if (single.getState() == SingleState.DEAD) continue;
                    portrayAliveSingle(single, camera, drawingSettings, unitHovered);
                }
            }
        } else {
            // Draw unit block
            for (BaseUnit unit : units) {
                // TODO: Change to using UnitState instead for consistency
                if (unit.getNumAlives() == 0) continue;
                drawUnitBlock(unit, camera, drawingSettings, false);
            }
        }

        // Draw the objects
        ArrayList<BaseObject> objects = unitModifier.getObjectHasher().getObjects();
        for (BaseObject obj : objects) {
            if (obj.isAlive()) drawObject(obj, camera, drawingSettings);
        }

        // -------------------
        // Procecss unit sound
        // -------------------
        if (audioSettings.isSoundEffect()) processUnitSound(units, camera);

        // -----------
        // Draw the UI
        // -----------

        // Draw all the unit icon
        ArrayList<BaseUnit> unitsSortedByPosition = (ArrayList) units.clone();
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
                double[] drawingPos = camera.getDrawingPosition(unit.getAverageX(), unit.getAverageY());
                int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());

                imageMode(CORNER);
                blendMode(NORMAL);
                image(unit == unitSelected ? bannerSelected : bannerShadow,
                        (float) (drawingPos[0] - 42),
                        (float) (drawingPos[1] - 220), 84, 220);
                fill(color[0], color[1], color[2], 255);
                rectMode(CORNER);
                rect((float) (drawingPos[0] - 30), (float) (drawingPos[1] - 186),
                        60, 94);
                image(banner,
                        (float) (drawingPos[0] - 42),
                        (float) (drawingPos[1] - 220), 84, 220);
                int[] healthColor = DrawingUtils.COLOR_HEALTH;
                fill(healthColor[0], healthColor[1], healthColor[2], healthColor[3]);
                rect((float) (drawingPos[0] - 28), (float) (drawingPos[1] - 90), 56.0f * unit.getNumAlives() / unit.getTroops().size(), 8);
                int[] moraleColor = DrawingUtils.COLOR_MORALE;
                fill(moraleColor[0], moraleColor[1], moraleColor[2], moraleColor[3]);
                rect((float) (drawingPos[0] - 28), (float) (drawingPos[1] - 80),56, 8);
                blendMode(MULTIPLY);
                image(bannerTexture,
                        (float) (drawingPos[0] - 42),
                        (float) (drawingPos[1] - 220), 84, 220);
                blendMode(NORMAL);
                if (unit instanceof CavalryUnit) {
                    image(iconCav,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 169, 60, 60);
                } else if (unit instanceof PhalanxUnit) {
                    image(iconSpear,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 169, 60, 60);
                } else if (unit instanceof SwordmenUnit) {
                    image(iconSword,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 169, 60, 60);
                } else if (unit instanceof ArcherUnit) {
                    image(iconArcher,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 169, 60, 60);
                } else if (unit instanceof SkirmisherUnit) {
                    image(iconSkirmisher,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 169, 60, 60);
                }
            }
        }

        // Information about the unit
        fill(0, 0, 0, 200);
        rect(0, 0, 400, 150);
        fill(255, 255, 255);
        textAlign(LEFT);
        text(UnitUtils.getUnitName(closestUnit), 8, 15);
        text("Unit state: ", 8, 35); text(closestUnit.getState().toString(), 100, 35);
        text("Strength: ", 8, 50); text(String.valueOf(closestUnit.getNumAlives()) + "/" + String.valueOf(closestUnit.getTroops().size()), 100, 50);

        // Process graphics
        fill(0, 0, 0);
        graphicTime = System.currentTimeMillis() - lastTime - backEndTime;
        textAlign(LEFT);
        text("Zoom level: " + Double.toString(camera.getZoom()), 5, INPUT_HEIGHT - 50);
        text("Backends: " + Long.toString(backEndTime) + "ms", 5, INPUT_HEIGHT - 35);
        text("Graphics: " + Long.toString(graphicTime) + "ms", 5, INPUT_HEIGHT - 20);
        if (graphicTime + backEndTime != 0) {
            text("FPS: " + Long.toString(1000 / (graphicTime + backEndTime)), 5, INPUT_HEIGHT - 5);
        } else {
            text("FPS: Infinity", 5, INPUT_HEIGHT - 5);
        }

        // Pause / Play Button
        if (!currentlyPaused) {
            uiDrawer.pauseButton(g,INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        } else {
            uiDrawer.playButton(g,INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        }

        // -----------------
        // Produce the image
        // -----------------
        if (drawingSettings.isProduceFootage()) {
            // Saves each frame as line-000001.png, line-000002.png, etc.
            saveFrame("line-######.png");
        }
    }

    /**
     * Check if mouse position is in the range of the pause or play button
     */
    private boolean overPauseOrPlay() {
        return (INPUT_WIDTH - 70 < mouseX && mouseX < INPUT_WIDTH - 30 &&
                INPUT_HEIGHT - 70 < mouseY && mouseY < INPUT_HEIGHT - 30);
    }

    @Override
    /**
     * Process the mouse click. The mouse click mechanism will be done as followed.
     * One click
     */
    public void mouseClicked() {

        // Check if in the range of pause button
        if (overPauseOrPlay()) {
            if(currentlyPaused) {
                currentlyPaused = false;
                if (audioSettings.isBackgroundMusic()) backgroundMusic.loop();
                if (audioSettings.isSoundEffect()) {
                    soundCombat.loop();
                    soundFootMarch.loop();
                    soundCavalryMarch.loop();
                }
            } else {
                currentlyPaused = true;
                if (audioSettings.isBackgroundMusic()) backgroundMusic.pause();
                if (audioSettings.isSoundEffect()) {
                    soundCombat.pause();
                    soundFootMarch.pause();
                    soundCavalryMarch.pause();
                }
            }
        }

        //
        if (mouseButton == LEFT) {
            unitSelected = closestUnit;
            soundLeftClick.play();
        } else if (mouseButton == RIGHT) {
            soundRightClick.play();
            if (unitSelected != null) {
                if (unitSelected instanceof ArcherUnit) {
                    // Conver closest unit to click
                    double[] screenPos = camera.getDrawingPosition(
                            closestUnit.getAverageX(),
                            closestUnit.getAverageY());
                    // If it's an archer unit, check the faction and distance from the closest unit from camera
                    if (closestUnit.getPoliticalFaction() != unitSelected.getPoliticalFaction() &&
                            MathUtils.squareDistance(mouseX, mouseY, screenPos[0], screenPos[1]) <
                                    UniversalConstants.SQUARE_CLICK_ATTACK_DISTANCE) {
                        ((ArcherUnit) unitSelected).setUnitFiredAt(closestUnit);
                    } else {
                        double[] position = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                        double posX = position[0];
                        double posY = position[1];
                        double angle = Math.atan2(posY - units.get(0).getAnchorY(), posX - units.get(0).getAnchorX());
                        unitSelected.moveFormationKeptTo(posX, posY, angle);
                    }
                } else {
                    double[] position = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                    double posX = position[0];
                    double posY = position[1];
                    double angle = Math.atan2(posY - units.get(0).getAnchorY(), posX - units.get(0).getAnchorX());
                    unitSelected.moveFormationKeptTo(posX, posY, angle);
                }
            }
        }
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        float scrollVal = event.getCount();
        if (!drawingSettings.isSmoothCameraMovement()) {
            // Non-smooth zoom processing
            if (scrollVal < 0) {
                // Zoom in
                camera.setZoom(camera.getZoom() * UniversalConstants.ZOOM_PER_SCROLL);
            } else if (scrollVal > 0) {
                // Zoom out
                camera.setZoom(camera.getZoom() / UniversalConstants.ZOOM_PER_SCROLL);
                if (camera.getZoom() < UniversalConstants.MINIMUM_ZOOM) camera.setZoom(UniversalConstants.MINIMUM_ZOOM);
            }
        } else {
            // Smooth-zoom processing
            if (scrollVal < 0) {
                // Zoom in
                zoomGoal *= UniversalConstants.ZOOM_PER_SCROLL;
            } else if (scrollVal > 0) {
                // Zoom out
                zoomGoal /= UniversalConstants.ZOOM_PER_SCROLL;
                if (zoomGoal < UniversalConstants.MINIMUM_ZOOM) zoomGoal = UniversalConstants.MINIMUM_ZOOM;
            }
            zoomCounter = drawingSettings.getSmoothCameraSteps();
        }
    }

    public static void main(String... args){
        PApplet.main("MainSimulation");
    }

    @Override
    public void keyPressed() {
        if (key == 'c') {
            experimentSettings.setDrawTroopInDanger(!experimentSettings.isDrawTroopInDanger());
        }
    }

    /**
     *  __  __           _      _    _       _ _      ____        _   _           _          _   _
     * |  \/  |         | |    | |  | |     (_) |    / __ \      | | (_)         (_)        | | (_)
     * | \  / | __ _ ___| | __ | |  | |_ __  _| |_  | |  | |_ __ | |_ _ _ __ ___  _ ______ _| |_ _  ___  _ __
     * | |\/| |/ _` / __| |/ / | |  | | '_ \| | __| | |  | | '_ \| __| | '_ ` _ \| |_  / _` | __| |/ _ \| '_ \
     * | |  | | (_| \__ \   <  | |__| | | | | | |_  | |__| | |_) | |_| | | | | | | |/ / (_| | |_| | (_) | | | |
     * |_|  |_|\__,_|___/_|\_\  \____/|_| |_|_|\__|  \____/| .__/ \__|_|_| |_| |_|_/___\__,_|\__|_|\___/|_| |_|
     *                                                     | |
     *                                                     |_|
     */

    /**
     * Create a mask based on unit that is roughly in position
     */
    private int[] unitMask(PImage image, IntMatrix2D alpha, BaseUnit unit) {
        boolean[][] inPositionMask = unit.getTroopsInPosition();
        double singleSpacing = unit.getSpacing() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        IntMatrix2D imageMap = new DenseIntMatrix2D(image.height, image.width);
        for (int i = 0; i < unit.getDepth(); i++) {
            for (int j = 0; j < unit.getWidth(); j++) {
                if (inPositionMask[i][j]) {
                    IntMatrix2D imagePart = imageMap.viewPart((int) (i * singleSpacing), (int) (j * singleSpacing),
                            (int) (singleSpacing), (int) (singleSpacing));
                    IntMatrix2D alphaPart = alpha.viewPart((int) (i * singleSpacing), (int) (j * singleSpacing),
                            (int) (singleSpacing), (int) (singleSpacing));
                    imagePart.assign(alphaPart, new IntIntFunction() {
                        @Override
                        public int apply(int i, int i1) {
                            return Math.min(i, i1);
                        }
                    });
                }
            }
        }
        return ((DenseIntMatrix2D) imageMap).elements();
    }

    /**
     * Draw all troop that is in position in a masked image.
     */
    private void drawMaskedInPositionUnit(BaseUnit unit, Camera camera, DrawingSettings settings) {
        if (settings.isDrawTroopShadow()) {
            Pair<PImage, IntMatrix2D> imageAndAlpha = unitShadowSimplifiedImageMap.get(unit);
            PImage img = imageAndAlpha.getKey();
            IntMatrix2D alpha = imageAndAlpha.getValue();
            int[] mask = unitMask(img, alpha, unit);
            img.mask(mask);

            double[] drawingPosition = camera.getDrawingPosition(unit.getAnchorX(), unit.getAnchorY());
            double drawX = drawingPosition[0];
            double drawY = drawingPosition[1];

            double relativeUnitImageScale = camera.getZoom() / UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
            pushMatrix();
            translate((float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset));
            rotate((float) (unit.getAnchorAngle() - camera.getAngle() + Math.PI / 2));
            image(img, (float) (- img.width / 2 * relativeUnitImageScale),
                    (float) (- unit.getSpacing() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE / 2 * relativeUnitImageScale),
                    (float) (img.width * relativeUnitImageScale),
                    (float) (img.height * relativeUnitImageScale));
            popMatrix();
        }

        Pair<PImage, IntMatrix2D> imageAndAlpha = unitSimplifiedImageMap.get(unit);
        PImage img = imageAndAlpha.getKey();
        IntMatrix2D alpha = imageAndAlpha.getValue();
        int[] mask = unitMask(img, alpha, unit);
        img.mask(mask);

        double[] drawingPosition = camera.getDrawingPosition(unit.getAnchorX(), unit.getAnchorY());
        double drawX = drawingPosition[0];
        double drawY = drawingPosition[1];

        double relativeUnitImageScale = camera.getZoom() / UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        pushMatrix();
        translate((float) drawX, (float) drawY);
        rotate((float) (unit.getAnchorAngle() - camera.getAngle() + Math.PI / 2));
        image(img, (float) (- img.width / 2 * relativeUnitImageScale),
                (float) (- unit.getSpacing() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE / 2 * relativeUnitImageScale),
                (float) (img.width * relativeUnitImageScale),
                (float) (img.height * relativeUnitImageScale));
        popMatrix();
    }

    /**
     * Create a simplified image and shadow image of the unit. This helps with drawer optimization since troop that is
     * roughly in their supposed position can just be drawn using an image map instead of being drawn individually
     */
    private PImage createSimplifiedUnitImage(BaseUnit unit) {
        double singleSpacing = unit.getSpacing() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        double singleSize = unit.getTroops().get(0).getSize() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        double imageWidth = unit.getWidth() * singleSpacing;
        double imageHeight = unit.getDepth() * singleSpacing;

        // Now, start to create and draw the image
        double topLeftX = singleSpacing / 2;
        double topLeftY = singleSpacing / 2;
        double x;
        double y;
        PGraphics pg = createGraphics((int) imageWidth, (int) imageHeight, P3D);
        pg.beginDraw();
        pg.noStroke();
        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        pg.background(color[0], color[1], color[2], 0);
        for (int i = 0; i < unit.getWidth(); i++) {
            for (int j = 0; j < unit.getDepth(); j++) {
                x = topLeftX + singleSpacing * i;
                y = topLeftY + singleSpacing * j;
                pg.fill(color[0], color[1], color[2], color[3]);
                pg.ellipse((float) x, (float) y, (float) singleSize, (float) singleSize);
            }
        }
        pg.endDraw();
        return pg.get();
    }

    private PImage createSimplifiedShadowUnitImage(BaseUnit unit) {
        double singleSpacing = unit.getSpacing() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        double shadowSize = unit.getTroops().get(0).getSize() * UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE
                * UniversalConstants.SHADOW_SIZE;
        double imageWidth = unit.getWidth() * singleSpacing;
        double imageHeight = unit.getDepth() * singleSpacing;

        // Now, start to create and draw the image
        double topLeftX = singleSpacing / 2;
        double topLeftY = singleSpacing / 2;
        double x;
        double y;
        PGraphics pg = createGraphics((int) imageWidth, (int) imageHeight, P3D);
        pg.beginDraw();
        pg.noStroke();
        int[] color = UniversalConstants.SHADOW_COLOR;
        pg.background(color[0], color[1], color[2], 0);
        for (int i = 0; i < unit.getWidth(); i++) {
            for (int j = 0; j < unit.getDepth(); j++) {
                x = topLeftX + singleSpacing * i;
                y = topLeftY + singleSpacing * j;
                pg.fill(color[0], color[1], color[2], color[3]);
                pg.ellipse((float) x, (float) y, (float) shadowSize, (float) shadowSize);
            }
        }
        pg.endDraw();
        return pg.get();
    }

    /**
     *   _____                                                          _               _          _
     *  / ____|                                                        (_)             | |        | |
     * | |  __  __ _ _ __ ___   ___   _ __  _ __ ___   ___ ___  ___ ___ _ _ __   __ _  | |__   ___| |_ __   ___ _ __
     * | | |_ |/ _` | '_ ` _ \ / _ \ | '_ \| '__/ _ \ / __/ _ \/ __/ __| | '_ \ / _` | | '_ \ / _ \ | '_ \ / _ \ '__|
     * | |__| | (_| | | | | | |  __/ | |_) | | | (_) | (_|  __/\__ \__ \ | | | | (_| | | | | |  __/ | |_) |  __/ |
     *  \_____|\__,_|_| |_| |_|\___| | .__/|_|  \___/ \___\___||___/___/_|_| |_|\__, | |_| |_|\___|_| .__/ \___|_|
     *                               | |                                         __/ |              | |
     *                               |_|                                        |___/               |_|
     */

    void preprocessSimplifiedUnitImages() {
        // Create an image version of troops. This helps with drawer optimization when ther are a lot of troops in
        // the field.
        for (BaseUnit unit : units) {
            PImage img = createSimplifiedUnitImage(unit);
            int[][] alpha = DrawingUtils.getAlphaArray(img);
            IntMatrix2D alphaArray = new DenseIntMatrix2D(alpha);
            unitSimplifiedImageMap.put(unit, new Pair<>(img, alphaArray));

            PImage shadow = createSimplifiedShadowUnitImage(unit);
            int[][] shadowAlpha = DrawingUtils.getAlphaArray(shadow);
            IntMatrix2D shadowAlphaArray = new DenseIntMatrix2D(shadowAlpha);
            unitShadowSimplifiedImageMap.put(unit, new Pair<>(shadow, shadowAlphaArray));
        }
    }

    /**
     * Load all tile from map, given the top left position.
     * Path must contains tile images
     * Each image file name must be in the format IMG-i-j.png, in which i and j is the row and column position of each
     * tile when all tile is laid out into the map.
     * @param x top left x position.
     * @param y top left y position.
     * @param path path to the folder containing images of all tiles.
     * @param tileSize the size of each tile.
     */
    private void loadMapTiles(double x, double y, String path, double tileSize) {

        // Initialize the tile list
        tiles = new ArrayList<>();

        // Load all the tiles
        Pattern numberPattern = Pattern.compile("[0-9]+");
        File folder = new File(Paths.get(path).toString());
        File[] fileList = folder.listFiles();

        for (File file : fileList) {
            String fileName = file.getName();
            Matcher m = numberPattern.matcher(fileName);
            m.find();
            int row = Integer.valueOf(m.group());
            m.find();
            int col = Integer.valueOf(m.group());
            PImage img = loadImage(Paths.get(path, fileName).toString());
            tiles.add(new Tile(x + col * tileSize, y + row * tileSize, tileSize, img));
        }
    }

    /**
     * Update the size map baseded on camera zoom
     */
    private void updateSizeMaps() {

        // Eye
        if (!eyeSizeMap.containsKey(camera.getZoom())) {
            eyeSizeMap.put(camera.getZoom(), UniversalConstants.EYE_SIZE * camera.getZoom());
        }
        currSizeEye = eyeSizeMap.get(camera.getZoom());

        // Swordman
        if (!swordmanSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = SwordmanConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            swordmanSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeSwordman = swordmanSizeMap.get(camera.getZoom());

        // Phalanx
        if (!phalanxSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = PhalanxConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            phalanxSizeMap.put(camera.getZoom(), newSize);
        }
        currSizePhalanx = phalanxSizeMap.get(camera.getZoom());

        // Slinger
        if (!slingerSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = SlingerConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            slingerSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeSlinger = slingerSizeMap.get(camera.getZoom());

        // Archer
        if (!archerSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = ArcherConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            archerSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeArcher = archerSizeMap.get(camera.getZoom());

        // Skirmisher
        if (!skirmisherSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = SkirmisherConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            skirmisherSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeSkirmisher = skirmisherSizeMap.get(camera.getZoom());

        // Cavalry
        if (!cavalrySizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = CavalryConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            cavalrySizeMap.put(camera.getZoom(), newSize);
        }
        currSizeCavalry = cavalrySizeMap.get(camera.getZoom());

        // Horse Archer
        if (!horseArcherSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            newSize[0] = HorseArcherConstants.SINGLE_SIZE * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            horseArcherSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeHorseArcher = horseArcherSizeMap.get(camera.getZoom());
    }

    /**
     *   _____                       _                                     _
     *  / ____|                     | |                                   (_)
     * | (___   ___  _   _ _ __   __| |  _ __  _ __ ___   ___ ___  ___ ___ _ _ __   __ _
     *  \___ \ / _ \| | | | '_ \ / _` | | '_ \| '__/ _ \ / __/ _ \/ __/ __| | '_ \ / _` |
     *  ____) | (_) | |_| | | | | (_| | | |_) | | | (_) | (_|  __/\__ \__ \ | | | | (_| |
     * |_____/ \___/ \__,_|_| |_|\__,_| | .__/|_|  \___/ \___\___||___/___/_|_| |_|\__, |
     *                                  | |                                         __/ |
     *                                  |_|                                        |___/
     */
    void processUnitSound(ArrayList<BaseUnit> units, Camera camera) {

        // Reset sound amplitudes
        combatAmplitude = AudioConstants.MIN_AMPLITUDE;
        footMarchAmplitude = AudioConstants.MIN_AMPLITUDE;
        cavalryMarchAplitude = AudioConstants.MIN_AMPLITUDE;

        // for each unit, calculate distance to camera.
        for (BaseUnit unit : units) {
            if (unit.getNumAlives() == 0) continue;
            double distance = MathUtils.magnitude(
                    camera.getX() - unit.getAverageX(),
                    camera.getY() - unit.getAverageY()) * camera.getZoom();
            double volumeAdded = AudioConstants.MAX_VOLUME_DISTANCE / Math.max(distance, AudioConstants.MAX_VOLUME_DISTANCE);
            if (unit.isInContactWithEnemy()) {
                combatAmplitude += volumeAdded;
            } else if (unit.getState() == UnitState.MOVING) {
                if (unit instanceof CavalryUnit || unit instanceof HorseArcherUnit) {
                    cavalryMarchAplitude += volumeAdded;
                } else {
                    footMarchAmplitude += volumeAdded;
                }
            }
        }
        combatAmplitude = (float) Math.min((Math.min(combatAmplitude, 1) * camera.getZoom()), 1);
        footMarchAmplitude = (float) Math.min((Math.min(footMarchAmplitude, 1) * camera.getZoom()), 1);
        cavalryMarchAplitude = (float) Math.min((Math.min(cavalryMarchAplitude, 1) * camera.getZoom()), 1);
        soundCombat.amp(combatAmplitude);
        soundFootMarch.amp(footMarchAmplitude);
        soundCavalryMarch.amp(cavalryMarchAplitude);
    }
    /**
     *   ____  _     _           _     _____
     *  / __ \| |   (_)         | |   |  __ \
     * | |  | | |__  _  ___  ___| |_  | |  | |_ __ __ ___      _____ _ __
     * | |  | | '_ \| |/ _ \/ __| __| | |  | | '__/ _` \ \ /\ / / _ \ '__|
     * | |__| | |_) | |  __/ (__| |_  | |__| | | | (_| |\ V  V /  __/ |
     *  \____/|_.__/| |\___|\___|\__| |_____/|_|  \__,_| \_/\_/ \___|_|
     *             _/ |
     *            |__/
     */

    void drawMapTiles(ArrayList<Tile> tiles, Camera camera) {

        double[][] cameraBoundingBox = {
                {0, 0},
                {camera.getWidth(), 0},
                {camera.getWidth(), camera.getHeight()},
                {0, camera.getHeight()}
        };

        for (Tile tile : tiles) {
            //
            double[][] tileCorners = tile.getFourCorners();
            double[][] drawCorners = new double[4][2];
            boolean drawable = false;
            for (int i = 0; i < tile.getFourCorners().length; i++) {
                drawCorners[i] = camera.getDrawingPosition(tileCorners[i][0], tileCorners[i][1]);
                if (DrawingUtils.drawable(drawCorners[i][0], drawCorners[i][1], INPUT_WIDTH, INPUT_HEIGHT)) {
                    drawable = true;
                    break;
                }
            }

            // Use bounding box check
            if (!drawable & PhysicUtils.rotatedBoundingBoxCollide(drawCorners, cameraBoundingBox)) drawable = true;

            // Draw the tile only if it is actually on screen
            if (drawable) {
                pushMatrix();
                translate((float) drawCorners[0][0], (float) drawCorners[0][1]);
                rotate((float) -camera.getAngle());
                image(tile.getTile(), 0, 0,
                        (float) (tile.getSize() * camera.getZoom()),
                        (float) (tile.getSize() * camera.getZoom()));
                popMatrix();
            }
        }
    }

    void drawObject(BaseObject object, Camera camera, DrawingSettings settings) {

        // Recalculate position and shape based on the camera position
        double[] position = camera.getDrawingPosition(object.getX(), object.getY());
        double drawX = position[0];
        double drawY = position[1];

        // Check if the object is drawable.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // Draw the object
        double angle = camera.getDrawingAngle(object.getAngle());
        if (object instanceof Arrow) {
            fill(50, 50, 50);
            shapeDrawer.arrow(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoom()), settings);
        }
    }

    void drawObjectCarriedByTroop(int lifeTime, BaseObject object, BaseSingle single, DrawingSettings settings) {

        // Recalculate object actual position
        Pair<Double, Double> rotatedVector = MathUtils.rotate(object.getX(), object.getY(), single.getAngle());

        // Recalculate position and shape based on the camera position
        double[] position = camera.getDrawingPosition(
                rotatedVector.getKey() + single.getX(),
                rotatedVector.getValue() + single.getY());
        double drawX = position[0];
        double drawY = position[1];

        // Draw the object
        int opacity = (int) (255 * Math.min(1.0, 1.0 * lifeTime / UniversalConstants.CARRIED_OBJECT_FADEAWAY));
        double angle = camera.getDrawingAngle(object.getAngle() + single.getAngle());
        if (object instanceof Arrow) {
            fill(50, 50, 50, opacity);
            shapeDrawer.arrow(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoom()), settings);
        }
    }

    /**
     *  _    _       _ _         _____
     * | |  | |     (_) |       |  __ \
     * | |  | |_ __  _| |_ ___  | |  | |_ __ __ ___      _____ _ __
     * | |  | | '_ \| | __/ __| | |  | | '__/ _` \ \ /\ / / _ \ '__|
     * | |__| | | | | | |_\__ \ | |__| | | | (_| |\ V  V /  __/ |
     *  \____/|_| |_|_|\__|___/ |_____/|_|  \__,_| \_/\_/ \___|_|
     */

    /**
     * Draw unit arrow plan. This arrow indicates the potential position that the unit is moving to.
     */
    void drawArrowPlan(BaseUnit unit, Camera camera, DrawingSettings settings) {
        double beginX = unit.getAnchorX();
        double beginY = unit.getAnchorY();
        double endX = unit.getGoalX();
        double endY = unit.getGoalY();
        double angle = MathUtils.atan2(endY - beginY, endX - beginX);
        double rightAngle = angle + Math.PI / 2;

        double upUnitX = MathUtils.quickCos((float) angle) * DrawingConstants.PLAN_ARROW_SIZE;
        double upUnitY = MathUtils.quickSin((float) angle) * DrawingConstants.PLAN_ARROW_SIZE;
        double rightUnitX = MathUtils.quickCos((float) rightAngle) * DrawingConstants.PLAN_ARROW_SIZE;
        double rightUnitY = MathUtils.quickSin((float) rightAngle) * DrawingConstants.PLAN_ARROW_SIZE;

        double[][] arrow = {
                {beginX + rightUnitX * 0.8, beginY + rightUnitY * 0.8},
                {endX + rightUnitX * 0.66 - upUnitX * 2.4, endY + rightUnitY * 0.66 - upUnitY * 2.4},
                {endX + rightUnitX * 1.66 - upUnitX * 3, endY + rightUnitY * 1.66 - upUnitY * 3},
                {endX, endY},
                {endX - rightUnitX * 1.66 - upUnitX * 3, endY - rightUnitY * 1.66 - upUnitY * 3},
                {endX - rightUnitX * 0.66 - upUnitX * 2.4, endY - rightUnitY * 0.66 - upUnitY * 2.4},
                {beginX - rightUnitX * 0.8, beginY - rightUnitY * 0.8},
        };

        beginShape();
        for (int i = 0; i < arrow.length; i++) {
            double[] drawingPosition = camera.getDrawingPosition(arrow[i][0], arrow[i][1]);
            vertex((float) drawingPosition[0], (float) drawingPosition[1]);
        }
        endShape(CLOSE);
    }

    /**
     * Draw the block representation of the goal
     */
    void drawGoalUnitBlock(BaseUnit unit, Camera camera, DrawingSettings settings, boolean hovered) {
        // draw the bounding box.
        double[][] boundingBox = unit.getGoalBoundingBox();

        // Convert to drawer points
        double[] p1 = camera.getDrawingPosition(boundingBox[0][0], boundingBox[0][1]);
        double[] p2 = camera.getDrawingPosition(boundingBox[1][0], boundingBox[1][1]);
        double[] p3 = camera.getDrawingPosition(boundingBox[2][0], boundingBox[2][1]);
        double[] p4 = camera.getDrawingPosition(boundingBox[3][0], boundingBox[3][1]);

        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        fill(color[0], color[1], color[2], 150);
        beginShape();
        vertex((float) p1[0], (float) p1[1]);
        vertex((float) p2[0], (float) p2[1]);
        vertex((float) p3[0], (float) p3[1]);
        vertex((float) p4[0], (float) p4[1]);
        endShape(CLOSE);
    }

    /**
     * Draw the block representing the entire unit.
     */
    void drawUnitBlock(BaseUnit unit, Camera camera, DrawingSettings settings, boolean hovered) {

        // draw the bounding box.
        double[][] boundingBox = unit.getBoundingBox();

        // Convert to drawer points
        double[] p1 = camera.getDrawingPosition(boundingBox[0][0], boundingBox[0][1]);
        double[] p2 = camera.getDrawingPosition(boundingBox[1][0], boundingBox[1][1]);
        double[] p3 = camera.getDrawingPosition(boundingBox[2][0], boundingBox[2][1]);
        double[] p4 = camera.getDrawingPosition(boundingBox[3][0], boundingBox[3][1]);
        double[] p5 = camera.getDrawingPosition(boundingBox[4][0], boundingBox[4][1]);
        double[] p6 = camera.getDrawingPosition(boundingBox[5][0], boundingBox[5][1]);

        // First, draw the box indicating the unit at full strength
        fill(DrawingConstants.UNIT_SIZE_COLOR[0],
                DrawingConstants.UNIT_SIZE_COLOR[1],
                DrawingConstants.UNIT_SIZE_COLOR[2],
                DrawingConstants.UNIT_SIZE_COLOR[3]);
        beginShape();
        vertex((float) p1[0], (float) p1[1]);
        vertex((float) p2[0], (float) p2[1]);
        vertex((float) p5[0], (float) p5[1]);
        vertex((float) p6[0], (float) p6[1]);
        endShape(CLOSE);

        // Then draw the box indicating the actual current scale of the the unit
        int[] shadowColor = UniversalConstants.SHADOW_COLOR;
        fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
        beginShape();
        vertex((float) (p1[0] + unitShadowXOffset * 2), (float) (p1[1] + unitShadowYOffset * 2));
        vertex((float) (p2[0] + unitShadowXOffset * 2), (float) (p2[1] + unitShadowYOffset * 2));
        vertex((float) (p3[0] + unitShadowXOffset * 2), (float) (p3[1] + unitShadowYOffset * 2));
        vertex((float) (p4[0] + unitShadowXOffset * 2), (float) (p4[1] + unitShadowYOffset * 2));
        endShape(CLOSE);

        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        if (closestUnit == unit) {
            fill(color[0], color[1], color[2], 255);
        } else {
            fill(color[0], color[1], color[2], 255);
        }
        if (unitSelected == unit) {
            fill(color[0], color[1], color[2], 255);
        }
        beginShape();
        vertex((float) p1[0], (float) p1[1]);
        vertex((float) p2[0], (float) p2[1]);
        vertex((float) p3[0], (float) p3[1]);
        vertex((float) p4[0], (float) p4[1]);
        endShape(CLOSE);
    }

    /**
     * Portray alive troop. The troop is only portrayable if
     */
    void portrayAliveSingle(BaseSingle single, Camera camera, DrawingSettings settings, boolean hovered) {
        // Recalculate position and shape based on the camera position
        double[] position = camera.getDrawingPosition(single.getX(), single.getY());
        double drawX = position[0];
        double drawY = position[1];

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // If it's drawable, draw the alive unit and potentially add some sound
        soundAliveSingle(single);
        if (drawingSettings.isInPositionOptimization() &&
                camera.getZoom() < UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE &&
                single.isInPosition() && !single.isInDanger()) {
            // Don't draw if troop is in position and zoom is small. This will be handled by a mask image to improve
            // optimization.
            return;
        }

        // Draw all the object sticking to the individual
        HashMap<BaseObject, Integer> carriedObjects = single.getCarriedObjects();
        for (BaseObject obj : carriedObjects.keySet()) {
            drawObjectCarriedByTroop(carriedObjects.get(obj), obj, single, settings);
        }
        drawAliveSingle(drawX, drawY, single, camera, settings, hovered);
    }

    /**
     * Add sound created by the alive troop. These will include attack, defend and pain sounds.
     */
    void soundAliveSingle(BaseSingle single) {

        // If sound effect not turned on don't play anything
        if (!audioSettings.isSoundEffect()) return;

        // Pain sound if the unit just got hit
        if (MathUtils.randUniform() < AudioConstants.SCREAM_TENDENCY) {
            if (single.getJustHit() > 0) {
                // Perform pain sound here.
            }
        }
    }

    /**
     * Draw alive unit
     */
    void drawAliveSingle(double drawX, double drawY, BaseSingle single, Camera camera, DrawingSettings settings, boolean hovered) {

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // Pre calculate drawer information
        double angle = camera.getDrawingAngle(single.getFacingAngle());
        double unitX = MathUtils.quickCos((float) angle);
        double unitY = MathUtils.quickSin((float) angle);

        // Fill the color by political faction
        int[] color = DrawingUtils.getFactionColor(single.getPoliticalFaction());
        int[] modifiedColor = new int[4];
        int[] shadowColor = UniversalConstants.SHADOW_COLOR;

        // Modify the color by the amount of damage sustain
        if (settings.isDrawDamageSustained()) {
            double sustainColorRatio = Math.min(single.getDamageSustain() / UniversalConstants.DAMAGE_SUSTAIN_MAXIMUM_EFFECT, 1.0);
            modifiedColor[0] = (int) (255.0 * sustainColorRatio + (1 - sustainColorRatio) * color[0]);
            modifiedColor[1] = (int) ((1 - sustainColorRatio) * color[1]);
            modifiedColor[2] = (int) ((1 - sustainColorRatio) * color[2]);
            modifiedColor[3] = 255;
        } else {
            modifiedColor = color;
        }

        // Modify the color if the unit is in danger of being collided
        if (experimentSettings.isBorderInwardCollision() && experimentSettings.isDrawTroopInDanger() &&
            single.getUnit().getInDanger()[single.getRow()][single.getCol()]) {
            modifiedColor = DrawingUtils.COLOR_IN_DANGER;
        }

        if (experimentSettings.isDrawTroopInPosition() && single.isInPosition()) {
            modifiedColor = DrawingUtils.COLOR_IN_POSITION;
        }

        // Draw the single based on the type of the unit.
        if (single instanceof CavalrySingle) {
            if (drawingSettings.isDrawTroopShadow()) {
                // Calvary shadow
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.cavalryShape(g,
                        (float) (drawX + shadowXOffset),
                        (float) (drawY + shadowYOffset),
                        (float) angle,
                        (float) (currSizeCavalry[INDEX_SHADOW_SIZE]),
                        camera);
            }

            // Calvary shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.cavalryShape(g,
                    (float) drawX, (float) drawY,
                    (float) angle, (float) (currSizeCavalry[0]),
                    camera);

            // Eye
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                fill(0, 0, 0, 128);
                ellipse((float) (drawX + (unitX * currSizeCavalry[INDEX_TROOP_SIZE] * 0.25)),
                        (float) (drawY + (unitY * currSizeCavalry[INDEX_TROOP_SIZE] * 0.25)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }

            // Cavalry Sword
            if (settings.getDrawWeapon() == DrawingMode.DRAW) {
                fill(50, 50, 50);
                double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                shapeDrawer.sword(g, (float) (drawX + (diaRightUnitX * currSizeCavalry[INDEX_TROOP_SIZE] * 0.45)),
                        (float) (drawY + (diaRightUnitY * currSizeCavalry[INDEX_TROOP_SIZE] * 0.45)),
                        (float) angle,
                        (float) camera.getZoom(),
                        settings);
            }
        } else if (single instanceof HorseArcherSingle) {
            // Calvary shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.cavalryShape(g,
                        (float) (drawX + shadowXOffset),
                        (float) (drawY + shadowYOffset),
                        (float) angle,
                        (float) (currSizeHorseArcher[INDEX_SHADOW_SIZE]),
                        camera);
            }

            // Calvary shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.cavalryShape(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (currSizeHorseArcher[INDEX_TROOP_SIZE]), camera);

            // Eye
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                fill(0, 0, 0, 128);
                ellipse((float) (drawX + (unitX * currSizeHorseArcher[INDEX_TROOP_SIZE] * 0.25)),
                        (float) (drawY + (unitY * currSizeHorseArcher[INDEX_TROOP_SIZE] * 0.25)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }
        } else if (single instanceof ArcherSingle) {
            // Archer shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset),
                        (float) currSizeArcher[INDEX_SHADOW_SIZE],
                        (float) currSizeArcher[INDEX_SHADOW_SIMPLIED_SIZE], camera);
            }

            // Archer circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeArcher[INDEX_TROOP_SIZE], (float) currSizeArcher[INDEX_TROOP_SIMPLIED_SIZE], camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeArcher[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (drawY + (unitY * currSizeArcher[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }
//
            // Archer bow
            fill(50, 50, 50);
            shapeDrawer.bow(g, (float) drawX, (float) drawY, (float) angle, (float) currSizeArcher[INDEX_TROOP_SIZE], settings);

        } else if (single instanceof SkirmisherSingle) {
            // Skirmisher shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset),
                        (float) currSizeSkirmisher[INDEX_SHADOW_SIZE],
                        (float) currSizeSkirmisher[INDEX_SHADOW_SIMPLIED_SIZE], camera);
            }

            // Skirmisher circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSkirmisher[INDEX_TROOP_SIZE], (float) currSizeSkirmisher[INDEX_TROOP_SIMPLIED_SIZE], camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSkirmisher[INDEX_TROOP_SIZE] / 2 * .6)),
                        (float) (drawY + (unitY * currSizeSkirmisher[INDEX_TROOP_SIZE] / 2 * .6)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }
        } else if (single instanceof SlingerSingle) {
            // Slinger shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset),
                        (float) currSizeSlinger[INDEX_SHADOW_SIZE],
                        (float) currSizeSlinger[INDEX_SHADOW_SIMPLIED_SIZE], camera);
            }

            // Slinger circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSlinger[INDEX_TROOP_SIZE], (float) currSizeSlinger[INDEX_TROOP_SIMPLIED_SIZE], camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSlinger[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (drawY + (unitY * currSizeSlinger[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }
        } else if (single instanceof PhalanxSingle) {
            // Phalanx shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset),
                        (float) currSizePhalanx[INDEX_SHADOW_SIZE],
                        (float) currSizePhalanx[INDEX_SHADOW_SIMPLIED_SIZE], camera);
            }

            // Phalanx circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizePhalanx[INDEX_TROOP_SIZE], (float) currSizePhalanx[INDEX_TROOP_SIMPLIED_SIZE], camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizePhalanx[INDEX_TROOP_SIZE] * .3)),
                        (float) (drawY + (unitY * currSizePhalanx[INDEX_TROOP_SIZE] * .3)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }

            // Spear
            if (settings.getDrawWeapon() == DrawingMode.DRAW) {
                if (camera.getZoom() > UniversalConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE || single.getRow() < 5) {
                    fill(50, 50, 50);
                    double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                    double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                    shapeDrawer.spear(g,
                            (float) (drawX + diaRightUnitX * currSizePhalanx[INDEX_TROOP_SIZE] * 0.45),
                            (float) (drawY + diaRightUnitY * currSizePhalanx[INDEX_TROOP_SIZE] * 0.45),
                            (float) angle, 100, (float) camera.getZoom(), settings);
                }
            }

        } else if (single instanceof SwordmanSingle) {
            // Swordman shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset),
                        (float) currSizeSwordman[INDEX_SHADOW_SIZE],
                        (float) currSizeSwordman[INDEX_SHADOW_SIMPLIED_SIZE], camera);
            }

            // Swordman circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSwordman[INDEX_TROOP_SIZE], (float) currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE], camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSwordman[0] * .3)),
                        (float) (drawY + (unitY * currSizeSwordman[0] * .3)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }

            // Sword
            if (settings.getDrawWeapon() == DrawingMode.DRAW) {
                fill(50, 50, 50);
                double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                shapeDrawer.sword(g,
                        (float) (drawX + (diaRightUnitX * currSizeSwordman[INDEX_TROOP_SIZE] * 0.45)),
                        (float) (drawY + (diaRightUnitY * currSizeSwordman[INDEX_TROOP_SIZE] * 0.45)),
                        (float) angle,
                        (float) camera.getZoom(),
                        settings);
            }
        } else {
            // Swordman shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset),
                        (float) currSizeSwordman[INDEX_SHADOW_SIZE],
                        (float) currSizeSwordman[INDEX_SHADOW_SIMPLIED_SIZE], camera);
            }

            // Swordman circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSwordman[INDEX_TROOP_SIZE], (float) currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE], camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSwordman[INDEX_TROOP_SIZE] / .3)),
                        (float) (drawY + (unitY * currSizeSwordman[INDEX_TROOP_SIZE] / .3)),
                        (float) (currSizeEye),
                        (float) (currSizeEye));
            }
        }
    }

    /**
     * Portray dead unit
     */
    void portrayDeadSingle(BaseSingle single, Camera camera, DrawingSettings settings) {
        // Recalculate position and shape based on the camera position
        double[] position = camera.getDrawingPosition(single.getX(), single.getY());
        double drawX = position[0];
        double drawY = position[1];

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // If it's drawable, draw the alive unit and potentially add some sound
        drawDeadSingle(drawX, drawY, single, camera, settings);
        soundDeadSingle(single, camera);
    }

    /**
     * Add sound created by troop that just died
     */
    void soundDeadSingle(BaseSingle single, Camera camera) {

        // If sound effect not turned on don't play anything
        if (!audioSettings.isSoundEffect()) return;

        // Scream dead once
        if (single.isScreamDeath()) {
            // Play dead sound here
            single.setScreamDeath(false);  // Only scream death once
        }
    }

    /**
     * Dead unit drawer
     * A dead unit will most of the time have just a gray body
     */
    void drawDeadSingle(double drawX, double drawY, BaseSingle single, Camera camera, DrawingSettings settings) {

        // Angle of the dead troop
        double angle = camera.getDrawingAngle(single.getFacingAngle());

        // Fill with color of dead
        int[] color = DrawingUtils.COLOR_DEAD;
        fill(color[0], color[1], color[2], color[3]);

        if (single instanceof CavalrySingle) {
            shapeDrawer.cavalryShape(g, (float) drawX, (float) drawY, (float) angle, (float) currSizeCavalry[INDEX_TROOP_SIZE], camera);
        } else if (single instanceof HorseArcherSingle) {
            shapeDrawer.cavalryShape(g, (float) drawX, (float) drawY, (float) angle, (float) currSizeHorseArcher[INDEX_TROOP_SIZE], camera);
        } else if (single instanceof PhalanxSingle) {
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizePhalanx[INDEX_TROOP_SIZE], (float) currSizePhalanx[INDEX_TROOP_SIMPLIED_SIZE], camera);
        } else if (single instanceof SwordmanSingle) {
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSwordman[INDEX_TROOP_SIZE], (float) currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE], camera);
        } else if (single instanceof ArcherSingle) {
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeArcher[INDEX_TROOP_SIZE], (float) currSizeArcher[INDEX_TROOP_SIMPLIED_SIZE], camera);
        } else if (single instanceof SlingerSingle) {
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSlinger[INDEX_TROOP_SIZE], (float) currSizeSlinger[INDEX_TROOP_SIMPLIED_SIZE], camera);
        } else if (single instanceof SkirmisherSingle) {
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY, (float) currSizeSkirmisher[INDEX_TROOP_SIZE], (float) currSizeSkirmisher[INDEX_TROOP_SIMPLIED_SIZE], camera);
        }
    }
}

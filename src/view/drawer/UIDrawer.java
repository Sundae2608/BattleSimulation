package view.drawer;

import model.constants.GameplayConstants;
import model.enums.UnitType;
import model.units.BaseUnit;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.BaseCamera;
import view.constants.DrawingConstants;
import view.constants.MapMakerConstants;
import view.components.Scrollbar;
import view.settings.DrawingSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains drawers for all UI elements in the game.
 */
public class UIDrawer extends BaseDrawer {

    // UI Icons
    PImage banner, bannerShadow, bannerSelected, bannerTexture;
    HashMap<UnitType, PImage> iconMap;
    PImage aiIcon;

    PApplet applet;
    BaseCamera camera;
    DrawingSettings drawingSettings;

    Map<String, Scrollbar> scrollbars;

    int xStart = 1000;
    int yScrollbarStart = 900;

    public UIDrawer(PApplet inputApplet, BaseCamera inputCamera, DrawingSettings inputDrawingSettings) {
        // Inject dependencies.
        applet = inputApplet;
        camera = inputCamera;
        drawingSettings = inputDrawingSettings;

        // Load icon images
        iconMap = new HashMap<>();
        iconMap.put(UnitType.SWORDMAN, applet.loadImage("imgs/BannerArt/iconSword.png"));
        iconMap.put(UnitType.PHALANX, applet.loadImage("imgs/BannerArt/iconSpear.png"));
        iconMap.put(UnitType.CAVALRY, applet.loadImage("imgs/BannerArt/iconCav.png"));
        iconMap.put(UnitType.ARCHER, applet.loadImage("imgs/BannerArt/iconArcher.png"));
        iconMap.put(UnitType.HORSE_ARCHER, applet.loadImage("imgs/BannerArt/iconHorseArcher.png"));
        iconMap.put(UnitType.SLINGER, applet.loadImage("imgs/BannerArt/iconSlinger.png"));
        iconMap.put(UnitType.SKIRMISHER, applet.loadImage("imgs/BannerArt/iconSkirmisher.png"));
        iconMap.put(UnitType.BALLISTA, applet.loadImage("imgs/BannerArt/iconBallista.png"));
        iconMap.put(UnitType.CATAPULT, applet.loadImage("imgs/BannerArt/iconCatapult.png"));
        iconMap.put(UnitType.GUN_INFANTRY, applet.loadImage("imgs/BannerArt/iconGunInfantry.png"));
        iconMap.put(UnitType.DEFAULT, applet.loadImage("imgs/BannerArt/iconDefault.png"));

        // AI Icon
        aiIcon = applet.loadImage("imgs/AIIcon/aiIcon-01.png");

        // Load banner images
        banner = applet.loadImage("imgs/BannerArt/SimplifiedBanner-01.png");
        bannerShadow = applet.loadImage("imgs/BannerArt/SimplifiedBanner-02.png");
        bannerTexture = applet.loadImage("imgs/BannerArt/SimplifiedBanner-03.png");
        bannerSelected = applet.loadImage("imgs/BannerArt/SimplifiedBanner-04.png");

        scrollbars = new HashMap<>();
    }

    @Override
    public void preprocess() {
        return;
    }

    /**
     * Draw unit banner.
     */
    public void drawUnitBanner(BaseUnit unit, boolean isSelected, boolean isAI) {
        double[] drawingPos = camera.getDrawingPosition(unit.getAverageX(), unit.getAverageY(), unit.getAverageZ());
        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        applet.rectMode(PApplet.CORNER);
        applet.imageMode(PApplet.CORNER);
        applet.blendMode(PApplet.NORMAL);
        applet.image(isSelected ? bannerSelected : bannerShadow,
                (float) (drawingPos[0] - 42),
                (float) (drawingPos[1] - 111), 84, 111);
        applet.fill(color[0], color[1], color[2], 255);
        applet.rect(
                (float) (drawingPos[0] - 30),
                (float) (drawingPos[1] - 93 + 60.0 * (1.0 - 1.0 * unit.getNumAlives() / unit.getTroops().size())),
                (float) 60, (float) (60.0 * unit.getNumAlives() / unit.getTroops().size()));
        applet.image(banner,
                (float) (drawingPos[0] - 42),
                (float) (drawingPos[1] - 111), 84, 111);
        int[] moraleColor = DrawingConstants.COLOR_MORALE;
        applet.fill(moraleColor[0], moraleColor[1], moraleColor[2], moraleColor[3]);
        applet.rect((float) (drawingPos[0] - 28), (float) (drawingPos[1] - 32),
                (float) (56 * Math.max(unit.getMorale(), 0.0) / GameplayConstants.BASE_MORALE), 8);
        applet.blendMode(PApplet.MULTIPLY);
        applet.image(bannerTexture,
                (float) (drawingPos[0] - 42),
                (float) (drawingPos[1] - 111), 84, 111);
        applet.blendMode(PApplet.NORMAL);
        applet.image(iconMap.get(unit.getUnitType()),
                (float) (drawingPos[0] - 30),
                (float) (drawingPos[1] - 92), 60, 60);
        applet.rectMode(PApplet.CENTER);

        // AI Icon
        if (isAI) {
            applet.image(aiIcon, (float) (drawingPos[0] - 49), (float) (drawingPos[1] - 111), 35, 35);
        }
    }

    /**
     * Paint circle
     */
    public void paintCircle(float x, float y) {
        applet.noFill();
        applet.strokeWeight(3);
        applet.stroke(MapMakerConstants.PAINT_CIRCLE_COLOR[0], MapMakerConstants.PAINT_CIRCLE_COLOR[1],
                MapMakerConstants.PAINT_CIRCLE_COLOR[2], MapMakerConstants.PAINT_CIRCLE_COLOR[3]);
        applet.circle(x, y, MapMakerConstants.PAINT_CIRCLE_SIZE);
        applet.noStroke();
    }

    /**
     * Pause button
     */
    public void pauseButton(float x, float y, float size) {
        applet.fill(80, 80, 80);
        applet.pushMatrix();
        applet.translate(x, y);

        applet.beginShape();
        applet.vertex(-0.5f * size, -0.5f * size);
        applet.vertex(-0.1f * size, -0.5f * size);
        applet.vertex(-0.1f * size, 0.5f * size);
        applet.vertex(-0.5f * size, 0.5f * size);
        applet.endShape(PApplet.CLOSE);

        applet.beginShape();
        applet.vertex(0.1f * size, 0.5f * size);
        applet.vertex(0.5f * size, 0.5f * size);
        applet.vertex(0.5f * size, -0.5f * size);
        applet.vertex(0.1f * size, -0.5f * size);
        applet.endShape(PApplet.CLOSE);
        applet.popMatrix();
    }

    /**
     * Play button
     */
    public void playButton(float x, float y, float size) {
        applet.fill(80, 80, 80);
        applet.pushMatrix();
        applet.translate(x, y);

        applet.beginShape();
        applet.vertex(-0.5f * size, -0.5f * size);
        applet.vertex(-0.5f * size, 0.5f * size);
        applet.vertex(0.5f * size, 0);
        applet.endShape(PApplet.CLOSE);
        applet.popMatrix();
    }
}
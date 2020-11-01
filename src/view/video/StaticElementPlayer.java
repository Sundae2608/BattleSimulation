package view.video;

import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventListener;
import model.utils.MathUtils;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.BaseCamera;

import java.util.ArrayList;
import java.util.HashMap;

public class StaticElementPlayer extends EventListener {
    HashMap<StaticElementType, ArrayList<StaticTemplate>> templateMap;
    ArrayList<StaticElement> elementArrayList;
    PApplet applet;
    BaseCamera camera;
    HashMap<StaticElement, Double> sizeMap;

    public StaticElementPlayer(PApplet inputApplet,
                              BaseCamera inputCamera,
                              HashMap<StaticElementType, ArrayList<StaticTemplate>> inputTemplateMap,
                              EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);
        applet = inputApplet;
        camera = inputCamera;
        templateMap = inputTemplateMap;
        elementArrayList = new ArrayList<>();
        sizeMap = new HashMap<>();
    }

    @Override
    protected void listenEvent(Event e) {
        StaticElementType elementType = null;
        switch (e.getEventType()) {
            case BLOOD_STAIN:
                elementType = StaticElementType.BLOOD_STAIN;
                break;
        }
        if (elementType != null && templateMap.containsKey(elementType)) {
            int randomElementSelection = MathUtils.randint(0, templateMap.get(elementType).size() - 1);
            elementArrayList.add(
                    new StaticElement(e.getX(), e.getY(), e.getZ(), e.getAngle(),
                            templateMap.get(elementType).get(randomElementSelection)));
        }
    }

    /**
     * Process current elements in the array
     */
    public void processElementQueue() {
        ArrayList<StaticElement> newArray = new ArrayList<>();
        for (StaticElement element : elementArrayList) {
            if (!element.hasFaded()) {
                // If video has not ended, draw the element on the screen
                double[] drawingPos = camera.getDrawingPosition(element.x, element.y, element.z);
                double zoom = camera.getZoomAtHeight(element.z);
                PImage image = element.template.image;
                applet.pushMatrix();
                applet.translate((float) drawingPos[0], (float) drawingPos[1]);
                applet.rotate((float) camera.getCameraAngleFromActualAngle(element.angle));
                float alpha = element.frame < element.template.fadeStart ? 1 :
                        (float) (1.0 * (element.template.fadeEnd - element.frame) /
                                (element.template.fadeEnd - element.template.fadeStart));
                if (!sizeMap.containsKey(element)) {
                    sizeMap.put(element, MathUtils.randDouble(element.template.minSize, element.template.maxSize));
                }
                double size = sizeMap.get(element);
                applet.tint(255, alpha * 255);
                applet.image(image, 0, (float) (- image.height * zoom * size / 2),
                        (float) (image.width * zoom * size),
                        (float) (image.height * zoom * size));
                applet.tint(255, 255);
                applet.popMatrix();
                element.step();

                // And accept this element in the new array, as the element has not ended yet
                newArray.add(element);
            }
        }
        elementArrayList = newArray;
    }
}

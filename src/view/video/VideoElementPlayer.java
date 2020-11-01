package view.video;

import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventListener;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.BaseCamera;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoElementPlayer extends EventListener {
    HashMap<VideoElementType, VideoTemplate> templateMap;
    ArrayList<VideoElement> elementArrayList;
    PApplet applet;
    BaseCamera camera;

    public VideoElementPlayer(PApplet inputApplet,
                              BaseCamera inputCamera,
                              HashMap<VideoElementType, VideoTemplate> inputTemplateMap,
                              EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);
        applet = inputApplet;
        camera = inputCamera;
        templateMap = inputTemplateMap;
        elementArrayList = new ArrayList<>();
    }

    @Override
    protected void listenEvent(Event e) {
        VideoElementType elementType = null;
        switch (e.getEventType()) {
            case EXPLOSION:
                elementType = VideoElementType.EXPLOSION;
                break;
            case MATCHLOCK_FIRE:
                elementType = VideoElementType.MATCHLOCK_GUNFIRE;
                break;
            case BLOOD_STAIN:
                elementType = VideoElementType.BLOOD_STAIN;
                break;
        }
        if (elementType != null && templateMap.containsKey(elementType)) {
            elementArrayList.add(
                    new VideoElement(e.getX(), e.getY(), e.getZ(), e.getAngle(),
                    templateMap.get(elementType)));
        }
    }

    /**
     * Process current elements in the array
     */
    public void processElementQueue() {
        ArrayList<VideoElement> newArray = new ArrayList<>();
        for (VideoElement element : elementArrayList) {
            if (!element.videoHasEnded()) {
                // If video has not ended, draw the element on the screen
                double[] drawingPos = camera.getDrawingPosition(element.x, element.y, element.z);
                double zoom = camera.getZoomAtHeight(element.z);
                PImage image = element.template.getSequence().get(element.frame);
                applet.pushMatrix();
                applet.translate((float) drawingPos[0], (float) drawingPos[1]);
                applet.rotate((float) camera.getCameraAngleFromActualAngle(element.angle));
                applet.image(image, 0, (float) (- image.height * zoom / 2),
                        (float) (image.width * zoom),
                        (float) (image.height * zoom));
                applet.popMatrix();
                element.step();

                // And accept this element in the new array, as the element has not ended yet
                newArray.add(element);
            }
        }
        elementArrayList = newArray;
    }
}

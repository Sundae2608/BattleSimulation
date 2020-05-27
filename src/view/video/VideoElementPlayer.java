package view.video;

import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventListener;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import view.camera.Camera;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoElementPlayer extends EventListener {
    HashMap<VideoElementType, VideoTemplate> templateMap;
    ArrayList<VideoElement> elementArrayList;
    PApplet applet;
    Camera camera;

    public VideoElementPlayer(PApplet inputApplet,
                              Camera inputCamera,
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
        }
        if (elementType != null && templateMap.containsKey(elementType)) {
            elementArrayList.add(new VideoElement(e.getX(), e.getY(), e.getZ(), templateMap.get(elementType)));
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
                applet.blendMode(PConstants.ADD);
                applet.image(image,
                        (float) (drawingPos[0] - image.width * zoom / 2),
                        (float) (drawingPos[1] - image.height * zoom / 2),
                        (float) (image.width * zoom),
                        (float) (image.height * zoom));
                applet.blendMode(PConstants.BLEND);
                element.step();

                // And accept this element in the new array, as the element has not ended yet
                newArray.add(element);
            }
        }
        elementArrayList = newArray;
    }
}

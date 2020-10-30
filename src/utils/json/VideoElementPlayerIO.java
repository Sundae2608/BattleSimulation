package utils.json;

import model.events.EventBroadcaster;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.BaseCamera;
import view.video.VideoElementPlayer;
import view.video.VideoElementType;
import view.video.VideoTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VideoElementPlayerIO extends JsonIO<VideoElementPlayer> {

    private BaseCamera camera;
    private PApplet applet;
    private EventBroadcaster eventBroadcaster;

    public VideoElementPlayerIO(BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) {
        this.camera = camera;
        this.applet = applet;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Override
    public VideoElementPlayer read(String filePath) throws IOException {
        JSONObject mainObj = null;
        try {
            mainObj = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        HashMap<VideoElementType, VideoTemplate> templateMap = new HashMap<>();
        JSONArray jsonArray = (JSONArray) mainObj.get("video_element_config");
        for(Object obj : jsonArray){
            JSONObject videoElementJsonObj;
            if (obj instanceof JSONObject) {

                // Extract the element type and the folder contains video information
                videoElementJsonObj = (JSONObject) obj;
                VideoElementType elementType = VideoElementType.valueOf((String) videoElementJsonObj.get("type"));
                File folder = new File((String) videoElementJsonObj.get("video_path"));

                // Build the sequence
                File[] listOfFiles = folder.listFiles();
                ArrayList<PImage> sequence = new ArrayList<>();
                for (int i = 0; i < listOfFiles.length; i++) {
                    PImage image = applet.loadImage(listOfFiles[i].getCanonicalPath());
                    sequence.add(image);
                }

                // Create the template and add it to the template map
                templateMap.put(elementType, new VideoTemplate(elementType, sequence));
            }
        }

        // Return the video element player
        VideoElementPlayer player = new VideoElementPlayer(applet, camera, templateMap, eventBroadcaster);
        return player;
    }
}

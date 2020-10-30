package utils.json;

import model.events.EventBroadcaster;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;
import processing.core.PImage;
import view.camera.BaseCamera;
import view.video.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StaticElementPlayerIO extends JsonIO<StaticElementPlayer> {

    private BaseCamera camera;
    private PApplet applet;
    private EventBroadcaster eventBroadcaster;

    public StaticElementPlayerIO(BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) {
        this.camera = camera;
        this.applet = applet;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Override
    public StaticElementPlayer read(String filePath) throws IOException {
        JSONObject mainObj = null;
        try {
            mainObj = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        HashMap<StaticElementType, ArrayList<StaticTemplate>> templateMap = new HashMap<>();
        JSONArray jsonArray = (JSONArray) mainObj.get("static_element_config");
        for(Object obj : jsonArray){
            JSONObject staticElementType;
            if (obj instanceof JSONObject) {

                // Extract the element type and the folder contains static element information
                staticElementType = (JSONObject) obj;
                StaticElementType elementType = StaticElementType.valueOf(getString(staticElementType.get("type")));
                PImage image = applet.loadImage(getString(staticElementType.get("image_path")));
                int fadeStart = getInt(staticElementType.get("fade_start"));
                int fadeEnd = getInt(staticElementType.get("fade_end"));

                // Create the template and add it to the template map
                if (!templateMap.containsKey(elementType)) {
                    templateMap.put(elementType, new ArrayList<>());
                }
                templateMap.get(elementType).add(new StaticTemplate(elementType, image, fadeStart, fadeEnd));
            }
        }

        // Return the static element element player
        StaticElementPlayer player = new StaticElementPlayer(applet, camera, templateMap, eventBroadcaster);
        return player;
    }
}

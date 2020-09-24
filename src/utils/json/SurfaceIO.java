package utils.json;

import model.surface.BaseSurface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SurfaceIO implements JsonIO<ArrayList<BaseSurface>> {
    @Override
    public ArrayList<BaseSurface> read(String filePath) throws IOException {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("game_config");

        // Read each surface config and create the surface
        ArrayList<BaseSurface> surfaces = new ArrayList<>();
        for(Object obj : jsonArray) {
            JSONObject statsObject;
            if (obj instanceof JSONObject) {
                statsObject = (JSONObject) obj;

            }
        }
    }

    @Override
    public void save(ArrayList<BaseSurface> data, String filePath) throws IOException {

    }
}

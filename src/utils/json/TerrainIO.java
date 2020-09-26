package utils.json;
import model.terrain.Terrain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class TerrainIO extends JsonIO<Terrain> {
    @Override
    public Terrain read(String filePath) {
        // Initialize the JSON object
        JSONObject obj = null;
        try {
            obj = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        obj = (JSONObject) obj.get("terrain_config");

        // Read terrain information
        double topX = getDouble(obj.get("top_x"));
        double topY = getDouble(obj.get("top_y"));
        double div =getDouble(obj.get("div"));
        int numX = getInt(obj.get("num_x"));
        int numY = getInt(obj.get("num_y"));
        double minHeight = getDouble(obj.get("min_height"));
        double maxHeight = getDouble(obj.get("max_height"));
        double perlinScale = getDouble(obj.get("perlin_scale"));
        double perlinDetailScale = getDouble(obj.get("perlin_detail_scale"));
        double perlinDetailHeightRatio = getDouble(obj.get("perlin_detail_height_ratio"));

        // Create a new terrain using the input configs.
        return new Terrain(
                topX, topY, div, numX, numY, minHeight, maxHeight,
                perlinScale, perlinDetailScale, perlinDetailHeightRatio
        );
    }
}

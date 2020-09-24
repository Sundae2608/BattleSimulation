package utils.json;
import model.terrain.Terrain;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TerrainIO implements JsonIO<Terrain> {
    @Override
    public Terrain read(String filePath) {
        JSONObject obj = null;
        try {
            obj = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        double topX = (double) obj.get("top_x");
        double topY = (double) obj.get("top_y");
        double div = (double) obj.get("div");
        int numX = Math.toIntExact((Long) obj.get("num_x"));
        int numY = Math.toIntExact((Long) obj.get("num_y"));
        double minHeight = (double) obj.get("min_height");
        double maxHeight = (double) obj.get("max_height");
        double perlinScale = (double) obj.get("perlin_scale");
        double perlinDetailScale = (double) obj.get("perlin_detail_scale");
        double perlinDetailHeightRatio = (double) obj.get("perlin_detail_height_ratio");

        // Create a new terrain using the input configs.
        return new Terrain(
                topX, topY, div, numX, numY, minHeight, maxHeight,
                perlinScale, perlinDetailScale, perlinDetailHeightRatio
        );
    }

    @Override
    public void save(Terrain terrain, String filePath) throws IOException {
        JSONObject terrainJsonObject = new JSONObject();
        terrainJsonObject.put("top_x", terrain.getTopX());
        terrainJsonObject.put("top_y", terrain.getTopY());
        terrainJsonObject.put("div", terrain.getDiv());
        terrainJsonObject.put("num_x", terrain.getNumX());
        terrainJsonObject.put("num_y", terrain.getNumY());
        terrainJsonObject.put("min_height", terrain.getMinZ());
        terrainJsonObject.put("max_height", terrain.getMaxZ());
        terrainJsonObject.put("perlin_scale", terrain.getPerlinScale());
        terrainJsonObject.put("perlin_detail_scale", terrain.getPerlinDetailScale());
        terrainJsonObject.put("perlin_detail_height_ratio", terrain.getPerlinDetailHeightRatio());

        FileWriter file = new FileWriter(filePath);
        file.write(terrainJsonObject.toJSONString());
        file.close();
    }
}

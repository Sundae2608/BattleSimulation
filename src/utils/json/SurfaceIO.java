package utils.json;

import model.enums.SurfaceType;
import model.surface.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SurfaceIO extends JsonIO<ArrayList<BaseSurface>> {
    @Override
    public ArrayList<BaseSurface> read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("surface_config");

        // Read each surface config and create the surface
        ArrayList<BaseSurface> surfaces = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject surfaceObject;
            if (obj instanceof JSONObject) {
                surfaceObject = (JSONObject) obj;
                double averageTreeRadius = 0.0;
                double sizeWiggling = 0.0;
                double averageDistance = 0.0;
                double distanceWiggling = 0.0;
                double averageTreeHeight = 0.0;
                double heightWiggling = 0.0;
                int treeHashDiv = 0;

                // Extract surface type
                SurfaceType type = SurfaceType.valueOf((String) surfaceObject.get("type"));
                JSONArray boundaryPtsObj = (JSONArray) surfaceObject.get("boundary_points");
                int numPts = boundaryPtsObj.size();
                ArrayList<double[]> pts = new ArrayList<>();
                for (int i = 0; i < numPts; i++) {
                    double[] newPt = new double[] {
                            getDouble(((JSONObject) boundaryPtsObj.get(i)).get("x")),
                            getDouble(((JSONObject) boundaryPtsObj.get(i)).get("y"))
                    };
                    pts.add(newPt);
                }
                if (type == SurfaceType.FOREST) {
                    averageTreeRadius = getDouble(surfaceObject.get("average_tree_radius"));
                    sizeWiggling = getDouble(surfaceObject.get("size_wiggling"));
                    averageDistance = getDouble(surfaceObject.get("average_distance"));
                    distanceWiggling = getDouble(surfaceObject.get("distance_wiggling"));
                    averageTreeHeight = getDouble(surfaceObject.get("average_tree_height"));
                    heightWiggling = getDouble(surfaceObject.get("height_wiggling"));
                    treeHashDiv = getInt(surfaceObject.get("tree_hash_div"));
                }

                // Based on the surface type, create the surface and add to the surface array.
                BaseSurface surface = null;
                switch (type) {
                    case SNOW:
                        surface = new SnowSurface(type, pts);
                        break;
                    case BEACH:
                        surface = new BeachSurface(type, pts);
                        break;
                    case MARSH:
                        surface = new MarshSurface(type, pts);
                        break;
                    case DESERT:
                        surface = new DesertSurface(type, pts);
                        break;
                    case FOREST:
                        surface = new ForestSurface(
                                type, pts, averageTreeRadius, sizeWiggling, averageDistance, distanceWiggling,
                                averageTreeHeight, heightWiggling, treeHashDiv);
                        break;
                    case RIVERSIDE:
                        surface = new RiversideSurface(type, pts);
                        break;
                    case SHALLOW_RIVER:
                        surface = new ShallowRiverSurface(type, pts);
                        break;
                    default:
                        break;
                }
                if (surface != null) surfaces.add(surface);
            }
        }
        return surfaces;
    }
}

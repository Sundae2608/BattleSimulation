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

public class SurfaceIO implements JsonIO<ArrayList<BaseSurface>> {
    @Override
    public ArrayList<BaseSurface> read(String filePath) throws IOException {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("game_config");

        // Read each surface config and create the surface
        ArrayList<BaseSurface> surfaces = new ArrayList<>();
        for(Object obj : jsonArray) {
            JSONObject surfaceObject;
            if (obj instanceof JSONObject) {
                surfaceObject = (JSONObject) obj;
                double averageTreeRadius = 0.0;
                double sizeWiggling = 0.0;
                double averageDistance = 0.0;
                double distanceWiggling = 0.0;

                // Extract surface type
                SurfaceType type = SurfaceType.valueOf((String) surfaceObject.get("type"));
                JSONArray boundaryPtsObj = (JSONArray) surfaceObject.get("boundary_points");
                int numPts = boundaryPtsObj.size();
                ArrayList<double[]> pts = new ArrayList<>();
                for (int i = 0; i < numPts; i++) {
                    double[] newPt = new double[] {
                            Double.valueOf((String) ((JSONObject) boundaryPtsObj.get(0)).get("x")),
                            Double.valueOf((String) ((JSONObject) boundaryPtsObj.get(0)).get("y"))
                    };
                    pts.add(newPt);
                }
                if (type == SurfaceType.FOREST) {
                    averageTreeRadius = Double.valueOf((String) surfaceObject.get("average_tree_radius"));
                    sizeWiggling = Double.valueOf((String) surfaceObject.get("average_tree_radius"));
                    averageDistance = Double.valueOf((String) surfaceObject.get("average_tree_radius"));
                    distanceWiggling = Double.valueOf((String) surfaceObject.get("average_tree_radius"));
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
                                type, pts, averageTreeRadius, sizeWiggling, averageDistance, distanceWiggling);
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

    @Override
    public void save(ArrayList<BaseSurface> data, String filePath) throws IOException {

    }
}

package utils.json;

import javafx.util.Pair;
import model.algorithms.pathfinding.Graph;
import model.construct.Construct;
import model.construct.ConstructType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstructsAndGraphIO extends JsonIO<Pair<Graph, ArrayList<Construct>>> {
    @Override
    public Pair<Graph, ArrayList<Construct>> read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        jsonObject = (JSONObject) jsonObject.get("constructs_and_graph_config");

        // Read all the constructs
        ArrayList<Construct> constructs = new ArrayList<>();
        JSONArray constructsArray = (JSONArray) jsonObject.get("constructs");
        for(Object obj : constructsArray) {
            JSONObject constructObj = (JSONObject) obj;
            ConstructType type = ConstructType.valueOf((String) constructObj.get("type"));
            JSONArray ptsArray = (JSONArray) constructObj.get("boundary_points");
            int numPts = ptsArray.size();
            ArrayList<double[]> pts = new ArrayList<>();
            for (int i = 0; i < numPts; i++) {
                double[] newPt = new double[] {
                        getDouble(((JSONObject) ptsArray.get(i)).get("x")),
                        getDouble(((JSONObject) ptsArray.get(i)).get("y"))
                };
                pts.add(newPt);
            }
            constructs.add(new Construct(type, pts));
        }

        // Read all the graph information.
        JSONObject graphObj = (JSONObject) jsonObject.get("graph");
        JSONArray nodesArrayObj = (JSONArray) graphObj.get("nodes");
        JSONArray edgesArrayObj = (JSONArray) graphObj.get("edges");
        HashMap<Integer, double[]> nodes = new HashMap<>();
        ArrayList<int[]> edges = new ArrayList<>();
        for (Object obj : nodesArrayObj) {
            JSONObject nodeObj = (JSONObject) obj;
            int index = getInt(nodeObj.get("index"));
            double x = getDouble(nodeObj.get("x"));
            double y = getDouble(nodeObj.get("y"));
            nodes.put(index, new double[] {x, y});
        }
        for (Object obj : edgesArrayObj) {
            JSONObject edgeObj = (JSONObject) obj;
            int node1 = getInt(edgeObj.get("node1"));
            int node2 = getInt(edgeObj.get("node2"));
            edges.add(new int[] {node1, node2});
        }
        Graph graph = new Graph(nodes, edges);

        // Return the graph construct pair
        return new Pair<>(graph, constructs);
    }
}

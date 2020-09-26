package utils.json;

import javafx.util.Pair;
import model.algorithms.pathfinding.Graph;
import model.construct.Construct;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConstructsAndGraphIO implements JsonIO<Pair<Graph, ArrayList<Construct>>> {
    @Override
    public Pair<Graph, ArrayList<Construct>> read(String filePath) throws IOException {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        jsonObject = (JSONObject) jsonObject.get("constructs_and_graph_config");

        // Read all the constructs
        JSONArray constructsArray = (JSONArray) jsonObject.get("constructs");
        for(Object obj : constructsArray) {
            JSONObject constructObj;
            
        }
    }

    @Override
    public void save(Pair data, String filePath) throws IOException {

    }
}

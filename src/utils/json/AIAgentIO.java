package utils.json;

import model.enums.PoliticalFaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import view.audio.Audio;
import view.audio.AudioSpeaker;
import view.audio.AudioType;
import view.audio.SpeakingType;

import java.io.FileReader;
import java.io.IOException;

public class AIAgentIO extends JsonIO<PoliticalFaction> {
    @Override
    public PoliticalFaction read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        jsonObject = (JSONObject) jsonObject.get("ai_config");

        // AI Config currently only houses Political faction. Add more config and create a new AI data holder if
        // necessary.
        return PoliticalFaction.valueOf((String) jsonObject.get("ai_faction"));
    }
}

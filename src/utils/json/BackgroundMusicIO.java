package utils.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;
import processing.sound.SoundFile;

import java.io.FileReader;
import java.io.IOException;

public class BackgroundMusicIO extends JsonIO<SoundFile> {

    PApplet pApplet;

    public BackgroundMusicIO(PApplet pApplet) {
        this.pApplet = pApplet;
    }

    @Override
    public SoundFile read(String filePath) {
        // Initialize the JSON Object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONObject bgmObject = (JSONObject) jsonObject.get("background_music_config");

        // Read the background music config
        String fileLocation = getString(bgmObject.get("file"));
        SoundFile soundFile = new SoundFile(pApplet, fileLocation);
        float volume = getFloat(bgmObject.get("base_volume"));
        soundFile.amp(volume);
        return soundFile;
    }
}

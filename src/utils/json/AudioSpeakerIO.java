package utils.json;

import model.events.EventBroadcaster;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;
import utils.ConfigUtils;
import view.audio.Audio;
import view.audio.AudioSpeaker;
import view.audio.AudioType;
import view.audio.SpeakingType;
import view.camera.BaseCamera;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

public class AudioSpeakerIO implements JsonIO<AudioSpeaker> {
    private BaseCamera camera;
    private PApplet applet;
    private EventBroadcaster eventBroadcaster;

    public AudioSpeakerIO(BaseCamera camera, PApplet applet, EventBroadcaster eventBroadcaster) {
        this.camera = camera;
        this.applet = applet;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Override
    public AudioSpeaker read(String filePath) {
        AudioSpeaker speaker = new AudioSpeaker(camera, applet, eventBroadcaster);
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = (JSONArray) jsonObject.get("audioConfig");

        for(Object obj: jsonArray){
            JSONObject audioTypeJsonObject;
            if (obj instanceof JSONObject) {
                audioTypeJsonObject = (JSONObject)obj;
                String audioPath = (String) audioTypeJsonObject.get("file");
                AudioType audioType = AudioType.valueOf((String) audioTypeJsonObject.get("audio_type"));
                SpeakingType speakingType = SpeakingType.valueOf((String) audioTypeJsonObject.get("broadcast_type"));
                float baseVolume = Float.parseFloat((String) audioTypeJsonObject.get("base_volume"));

                Audio audio = new Audio(
                        audioPath, audioType, speakingType, baseVolume, applet
                );
                speaker.addAudio(audioType, audio);
            }
        }
        return speaker;
    }

    @Override
    public void save(AudioSpeaker audioSpeaker, String filePath) {

    }
}

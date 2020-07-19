package view.audio;

import model.events.Event;
import model.events.EventBroadcaster;
import model.events.EventListener;
import model.events.custom_events.CavalryMarchingEvent;
import model.events.custom_events.SoldierMarchingEvent;
import model.utils.MathUtils;
import processing.core.PApplet;
import view.camera.Camera;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioSpeaker extends EventListener {

    // The Camera that listens to the speaker
    Camera camera;

    // The applet that the object will broadcast the audio to.
    PApplet pApplet;

    // Queue of event to be processed
    ArrayList<Event> eventList;

    // A map from audio type to the audio stats
    HashMap<AudioType, Audio> audioMap;

    public AudioSpeaker(Camera inputCamera, PApplet applet, EventBroadcaster eventBroadcaster) {
        super(eventBroadcaster);
        camera = inputCamera;
        eventList = new ArrayList<>();
        audioMap = new HashMap<>();
        pApplet = applet;
    }

    @Override
    protected void listenEvent(Event e) {
        // TODO: All events are currently assumed to be audio events and added here. However, pre-processing will help
        //  with processing speed. Implement a map from EventType to AudioType internally.
        eventList.add(e);
    }

    public void pauseAllAmbientSounds() {
        for (AudioType type : audioMap.keySet()) {
            Audio audio = audioMap.get(type);
            audio.soundFile.pause();
        }
    }

    public void resumeAllAmbientSounds() {
        for (AudioType type : audioMap.keySet()) {
            Audio audio = audioMap.get(type);
            audio.soundFile.loop();
        }
    }

    public void processEvents() {
        // Reset the volume of all ambient audio
        for (AudioType type : audioMap.keySet()) {
            Audio audio = audioMap.get(type);
            switch (audio.speakingType) {
                case AMBIENT:
                    audio.resetVolume();
            }
        }
        for (Event e : eventList) {
            double distanceToCamera = MathUtils.magnitude(
                    camera.getX() - e.getX(),
                    camera.getY() - e.getY(),
                    camera.getZ() - e.getZ());
            float volumePortionAdded = (float) (AudioConstants.MAX_VOLUME_DISTANCE /
                    Math.max(distanceToCamera, AudioConstants.MAX_VOLUME_DISTANCE));
            Audio audio = null;
            switch (e.getEventType()) {
                case CAVALRY_RUNNING:
                    audio = audioMap.get(AudioType.CAVALRY_RUNNING);
                    volumePortionAdded *= AudioConstants.SOUND_PROPORTION_PER_SOLDER *
                            ((CavalryMarchingEvent) e).getNumSingles();
                    break;
                case CAVALRY_CHARGE:
                    audio = audioMap.get(AudioType.CAVALRY_CHARGE);
                    audio.playSound();
                    break;
                case ARROW_CROWD_HIT:
                    audio = audioMap.get(AudioType.ARROW_CROWD_HIT);
                    break;
                case SOLDIER_CHARGE:
                    audio = audioMap.get(AudioType.SOLDIER_CHARGE);
                    audio.playSound();
                    break;
                case EXPLOSION:
                    audio = audioMap.get(AudioType.EXPLOSION);
                    audio.playSound();
                    break;
                case BALLISTA_HIT_GROUND:
                    audio = audioMap.get(AudioType.BALLISTA_HIT_GROUND);
                    audio.playSound();;
                    break;
                case BALLISTA_HIT_FLESH:
                    audio = audioMap.get(AudioType.BALLISTA_HIT_FLESH);
                    audio.playSound();;
                    break;
                case SOLDIER_FIGHTING:
                    audio = audioMap.get(AudioType.SOLDIER_FIGHTING);
                    break;
                case SOLDIER_MARCHING:
                    volumePortionAdded *= AudioConstants.SOUND_PROPORTION_PER_SOLDER *
                            ((SoldierMarchingEvent) e).getNumSingles();
                    audio = audioMap.get(AudioType.SOLDIER_MARCHING);
                    break;
                default:
                    break;
            }
            // If there is an audio accompanying the event
            if (audio != null && audio.speakingType == SpeakingType.AMBIENT) audio.addVolume(volumePortionAdded * audio.getBaseVolume());
        }
        // After processing, clear the event list
        eventList.clear();
    }

    public void addAudio(AudioType audioType, Audio audio) {
        audioMap.put(audioType, audio);
    }

    // TODO: Refactor AudioType into actually different Audio children class. Using children class is more fitting for
    //  this use case.
    public void broadcastBurstSound(AudioType audioType) {
        Audio audio = audioMap.get(audioType);
        if (audio.speakingType == SpeakingType.BURST) {
            audioMap.get(audioType).playSound();
        }
    }

    public void broadcastOverlaySound(AudioType audioType) {
        Audio audio = audioMap.get(audioType);
        if (audio.speakingType == SpeakingType.OVERLAY) {
            audioMap.get(audioType).playSound();
        }
    }
}

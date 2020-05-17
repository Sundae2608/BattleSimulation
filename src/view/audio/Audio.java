package view.audio;

import processing.core.PApplet;
import processing.sound.SoundFile;

public class Audio {

    // The sound of the file.
    SoundFile soundFile;
    AudioType audioType;
    SpeakingType speakingType;
    private float baseVolume;
    float curVolume;

    public Audio(String filePath,
                 AudioType inputAudioType,
                 SpeakingType inputSpeakingType,
                 float inputBaseVolume,
                 PApplet applet) {
        soundFile = new SoundFile(applet, filePath);
        audioType = inputAudioType;
        speakingType = inputSpeakingType;
        baseVolume = inputBaseVolume;
        curVolume = baseVolume;
        switch (speakingType) {
            case BURST:
                soundFile.amp(curVolume);
                break;
            case OVERLAY:
                soundFile.amp(curVolume);
                break;
            case AMBIENT:
                soundFile.amp(AudioConstants.MIN_AMPLITUDE);
                soundFile.loop();
                break;
        }
    }

    public void playSound() {
        switch (speakingType) {
            case BURST:
            case OVERLAY:
                soundFile.play();
                break;
            case AMBIENT:
            default:
        }
    }

    public void resetVolume() {
        curVolume = AudioConstants.MIN_AMPLITUDE;
        soundFile.amp(curVolume);
    }

    public void addVolume(float volume) {
        curVolume += volume;
        soundFile.amp(Math.min(curVolume, 1.0f));
    }

    public float getBaseVolume() {
        return baseVolume;
    }
}

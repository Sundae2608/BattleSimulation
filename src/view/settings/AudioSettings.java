package view.settings;

public class AudioSettings {

    private boolean backgroundMusic;
    private boolean soundEffect;

    public AudioSettings() {}

    public boolean isBackgroundMusic() {
        return backgroundMusic;
    }
    public void setBackgroundMusic(boolean backgroundMusic) {
        this.backgroundMusic = backgroundMusic;
    }

    public boolean isSoundEffect() {
        return soundEffect;
    }
    public void setSoundEffect(boolean soundEffect) {
        this.soundEffect = soundEffect;
    }
}

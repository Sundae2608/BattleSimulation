package model.constants;

public class AudioConstants {

    // Tendency to scream in pain
    public static double SCREAM_TENDENCY = 0.03;

    // Amplitude of the dead scream
    public static double DEATH_SCREAM_AMP = 0.1;

    // Distance in which max volume is heard.
    public static double MAX_VOLUME_DISTANCE = 600;

    // Minimum amplitude (to avoid booming the speaker)
    public static float MIN_AMPLITUDE = 0.000001f;
}

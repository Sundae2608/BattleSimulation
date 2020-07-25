package model.sound;

import model.enums.SingleState;
import model.units.unit_stats.UnitSoundStats;

public class SoundSource {

    //TODO: Maybe a Arraylist of X,Y,Z
    double noise; //unit in dB
    double noiseCoordinateX;
    double noiseCoordinateY;
    double noiseCoordinateZ;
    String noiseLabel; // to label what kind of noise is to be received by sound sink

    // Constructor
    public SoundSource () {
    }

    // Additional constructor...
    public SoundSource (UnitSoundStats unitSoundStats, double averageX, double averageY, double averageZ) {
        noise = unitSoundStats.noise;
    }

    public double getNoise() {
        return noise;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    public double getNoiseCoordinateX() {
        return noiseCoordinateX;
    }

    public void setNoiseCoordinateX(double noiseCoordinateX) {
        this.noiseCoordinateX = noiseCoordinateX;
    }

    public double getNoiseCoordinateY() {
        return noiseCoordinateY;
    }

    public void setNoiseCoordinateY(double noiseCoordinateY) {
        this.noiseCoordinateY = noiseCoordinateY;
    }

    public double getNoiseCoordinateZ() {
        return noiseCoordinateZ;
    }

    public void setNoiseCoordinateZ(double noiseCoordinateZ) {
        this.noiseCoordinateZ = noiseCoordinateZ;
    }

    public String getNoiseLabel() {
        return noiseLabel;
    }

    public void setNoiseLabel(String noiseLabel) {
        this.noiseLabel = noiseLabel;
    }
}

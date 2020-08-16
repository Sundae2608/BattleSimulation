package model.sound;

import model.units.unit_stats.UnitSoundStats;

import java.util.ArrayList;

public class SoundSource {


    double noise; //unit in dB

    // TODO: change this to tuple XYZ
    // If the noise is a single point
    double x;
    double y;
    double z;

    //TODO: Maybe a Arraylist of X,Y,Z instead of just X, Y, Z
    // If the soundSource has many speakers
    ArrayList<Double> noiseArrayX;
    ArrayList<Double> noiseArrayY;
    ArrayList<Double> noiseArrayZ;

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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getNoiseCoordinateZ() {
        return z;
    }

    public void setNoiseCoordinateZ(double noiseCoordinateZ) {
        this.z = noiseCoordinateZ;
    }

    public String getNoiseLabel() {
        return noiseLabel;
    }

    public void setNoiseLabel(String noiseLabel) {
        this.noiseLabel = noiseLabel;
    }

    public ArrayList<Double> getNoiseArrayX() {
        return noiseArrayX;
    }

    public void setNoiseArrayX(ArrayList<Double> noiseArrayX) {
        this.noiseArrayX = noiseArrayX;
    }

    public ArrayList<Double> getNoiseArrayY() {
        return noiseArrayY;
    }

    public void setNoiseArrayY(ArrayList<Double> noiseArrayY) {
        this.noiseArrayY = noiseArrayY;
    }

    public ArrayList<Double> getNoiseArrayZ() {
        return noiseArrayZ;
    }

    public void setNoiseArrayZ(ArrayList<Double> noiseArrayZ) {
        this.noiseArrayZ = noiseArrayZ;
    }
}

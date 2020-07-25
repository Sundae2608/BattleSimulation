package model.sound;

import model.units.unit_stats.UnitSoundStats;

public class SoundSource {

    double noise; //unit in dB
    double noiseCoordinateX;
    double noiseCoordinateY;
    double noiseCoordinateZ;
    String noiseLabel;

    // Constructor
    public SoundSource () {
    }

    // Additional constructor...
    public SoundSource (UnitSoundStats unitSoundStats, double averageX, double averageY, double averageZ) {
        noise = unitSoundStats.noise;
        noiseCoordinateX = averageX;
        noiseCoordinateY = averageY;
        noiseCoordinateZ = averageZ;
    }


    // Setters and Getters
    public void setNoise(double noiseInput){
        noise = noiseInput;
    }

    public double getNoise(){
        return noise;
    }

    public void setNoiseCoordinateX(double noiseInputCoordinate){
        noiseCoordinateX = noiseInputCoordinate;
    }

    public double getNoiseCoordinateX(){
        return noiseCoordinateX;
    }

    public void setNoiseCoordinateY(double noiseInputCoordinate){
        noiseCoordinateY = noiseInputCoordinate;
    }

    public double getNoiseCoordinateY(){
        return noiseCoordinateY;
    }
    public void setNoiseCoordinateZ(double noiseInputCoordinate){
        noiseCoordinateZ = noiseInputCoordinate;
    }

    public double getNoiseCoordinateZ(){
        return noiseCoordinateZ;
    }

    public void setNoiseLabel(String inputNoiseLabel){
        noiseLabel = inputNoiseLabel;
    }

    public String getNoiseLabel(){
        return noiseLabel;
    }

}

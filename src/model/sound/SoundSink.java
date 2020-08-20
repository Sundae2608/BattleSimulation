package model.sound;

import javafx.util.Pair;
import model.surface.BaseSurface;
import model.terrain.Terrain;
import model.units.BaseUnit;
import model.utils.PhysicUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SoundSink {
    // The sound sink is a single point
    double x;
    double y;
    double z;

    HashMap<String, Pair<Double, Double>> soundSinkEverything; // Because each unit is a sound sink itself, each unit should host
    // a list of SoundSource objects along with the level of noise (left element) and respective directional angle
    // (right element)
    HashMap<String, Double> soundSinkPerceived; // Because each identify certain sound sources, this is a filtered
    // version of the soundSink instance. The double here indicate the respective angle

    // Constructor
    public SoundSink () {
        soundSinkEverything = new HashMap<>();
        soundSinkPerceived = new HashMap<>();
    }

    /**
     *
     * @param soundSources: this is a list of all the sound sources in the map
     */
    public void updateSoundSink(ArrayList<SoundSource> soundSources, Terrain terrain, ArrayList<BaseSurface> surfaces,
                                ArrayList<BaseUnit> units, BaseUnit thisUnit){
        soundSinkEverything.clear(); // Clearing and updating new Hashmap for every iteration

        for (SoundSource soundSource : soundSources){
            String perceivedNoiseLabel = PhysicUtils.getPerceivedNoiseLabel(soundSource, terrain, surfaces,
                    units, thisUnit);
            Pair<Double, Double> perceivedNoise = PhysicUtils.getPerceivedNoise(soundSource, terrain, surfaces, thisUnit);
            soundSinkEverything.put(perceivedNoiseLabel, perceivedNoise);
        }
    }

    // TODO: Need revision, since dB can be negative
    public void updatePerceivedSoundSink(){
        soundSinkPerceived.clear(); // Clearing and updating new Hashmap for every iteration
        Double totalDB = 0.0;

        for (String perceivedNoiseLabel : soundSinkEverything.keySet()){
            totalDB = totalDB + soundSinkEverything.get(perceivedNoiseLabel).getKey();
        }

        for (String perceivedNoiseLabel : soundSinkEverything.keySet()){
            // Get noise level
            double perceivedNoiseLevel = soundSinkEverything.get(perceivedNoiseLabel).getKey();

            // TODO: This threshold 0.2 should be set somewhere. It is a magic number for now
            // TODO: Sort out unique
            if (perceivedNoiseLevel/totalDB < 0.2){
                continue;
            } else{
                soundSinkPerceived.put(perceivedNoiseLabel, perceivedNoiseLevel);
            }
        }

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
}

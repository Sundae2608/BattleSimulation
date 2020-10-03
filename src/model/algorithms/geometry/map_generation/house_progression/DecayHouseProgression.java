package model.algorithms.geometry.map_generation.house_progression;

import model.algorithms.geometry.map_generation.Progression;

public class DecayHouseProgression implements Progression {
    @Override
    public int progress(int value) {
        return value - 2;
    }
}

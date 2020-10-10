package city_gen_model.house_progression;

import city_gen_model.Progression;

public class NominalHouseProgression implements Progression {

    @Override
    public int progress(int value) {
        return value + 1;
    }
}

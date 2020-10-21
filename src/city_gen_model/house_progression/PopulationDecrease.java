package city_gen_model.house_progression;

import city_gen_model.Progression;

public class PopulationDecrease implements Progression {
    @Override
    public int progress(int value) {
        return Math.max(value - 5, 0);
    }
}

package city_gen_model.progression;

public interface Progression {

    double getNextValue(double currentValue, int timestep);
}

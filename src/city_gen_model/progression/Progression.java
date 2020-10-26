package city_gen_model.progression;

public interface Progression {

    double getNextValue(int currentValue);

    double getRateOfChange();
}

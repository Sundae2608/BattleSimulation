package city_gen_model.progression;

public class LinearFunction implements Progression {
    private double delta;
    public LinearFunction(double delta) {
        this.delta = delta;
    }

    @Override
    public double getNextValue(int currentValue) {
        return currentValue + delta;
    }
}

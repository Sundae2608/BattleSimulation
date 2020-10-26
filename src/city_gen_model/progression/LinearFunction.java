package city_gen_model.progression;

public class LinearFunction implements Progression {

    private double rateOfChange;

    public LinearFunction(double rate) {
        this.rateOfChange = rate;
    }

    @Override
    public double getNextValue(int currentValue) {
        return currentValue + rateOfChange;
    }

    @Override
    public double getRateOfChange() {
        return rateOfChange;
    }
}

package city_gen_model.progression;

public class LinearFunction implements Progression {

    private double rateOfChange;

    public LinearFunction(double rate) {
        this.rateOfChange = rate;
    }

    @Override
    public double getNextValue(double currentValue, int timestep) {
        return Math.max(currentValue + rateOfChange*timestep, 0);
    }

}

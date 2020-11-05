package city_gen_model.progression;

public class ExponentialFunction implements Progression{

    private double rateOfChange;

    public ExponentialFunction(double rate) {
        this.rateOfChange = rate;
    }

    @Override
    public double getNextValue(double currentValue) {
        return Math.max(currentValue * rateOfChange, 0);
    }
}

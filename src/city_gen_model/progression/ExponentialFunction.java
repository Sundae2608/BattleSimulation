package city_gen_model.progression;

public class ExponentialFunction implements Progression{

    private double coefficient;

    public ExponentialFunction(double coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public double getNextValue(int currentValue) {
        return currentValue*coefficient;
    }
}

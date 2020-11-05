package city_gen_model.progression;

public class LogisticFunction implements Progression {

    private double relativeGrowthCoefficient;
    private int carryingCapacity;
    public LogisticFunction(double relativeGrowthCoefficient, int carryingCapacity) {
        this.relativeGrowthCoefficient = relativeGrowthCoefficient;
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public double getNextValue(double currentValue) {
        return currentValue + relativeGrowthCoefficient*currentValue*(1-currentValue/carryingCapacity);
    }
}

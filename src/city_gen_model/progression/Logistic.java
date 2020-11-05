package city_gen_model.progression;

public class Logistic implements Progression {

    private double relativeGrowthCoefficient;
    private int carryingCapacity;
    public Logistic(double relativeGrowthCoefficient, int carryingCapacity) {
        this.relativeGrowthCoefficient = relativeGrowthCoefficient;
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public double getNextValue(double currentValue) {
        return relativeGrowthCoefficient*currentValue*(1-currentValue/carryingCapacity);
    }
}

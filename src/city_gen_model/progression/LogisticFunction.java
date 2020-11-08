package city_gen_model.progression;

public class LogisticFunction implements Progression {

    private double relativeGrowthCoefficient;
    private int carryingCapacity;
    public LogisticFunction(double relativeGrowthCoefficient, int carryingCapacity) {
        this.relativeGrowthCoefficient = relativeGrowthCoefficient;
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public double getNextValue(double currentValue, int timestep) {
        return carryingCapacity / (1 + ((carryingCapacity-currentValue)/currentValue)*Math.pow(Math.E, -1*relativeGrowthCoefficient*timestep));
    }
}

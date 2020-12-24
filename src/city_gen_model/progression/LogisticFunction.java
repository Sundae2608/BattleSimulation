package city_gen_model.progression;

public class LogisticFunction implements Progression {

    private double relativeGrowthCoefficient;
    private double carryingCapacity;
    public LogisticFunction(double relativeGrowthCoefficient, double carryingCapacity) {
        this.relativeGrowthCoefficient = relativeGrowthCoefficient;
        this.carryingCapacity = carryingCapacity;
    }

    /**
     *  Use this formula to calculate the number of next values in next x time steps
     *  P(t) = K / (1 + (K- Po)/Po) * (e ^(-rt))
     *  K: carrying capacity
     *  Po: Initial population
     *  r: relative growth coefficient
     *  t: time interval
     *  https://en.wikipedia.org/wiki/Logistic_function
     * @param currentValue
     * @param timeInterval
     * @return
     */
    @Override
    public double getNextValue(double currentValue, double timeInterval) {
        return carryingCapacity /
                (1 + ((carryingCapacity - currentValue) / currentValue) *
                        Math.pow(Math.E, -1 * relativeGrowthCoefficient * timeInterval));
    }

    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(double carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public double getRelativeGrowthCoefficient() {
        return relativeGrowthCoefficient;
    }

    public void setRelativeGrowthCoefficient(double relativeGrowthCoefficient) {
        this.relativeGrowthCoefficient = relativeGrowthCoefficient;
    }
}

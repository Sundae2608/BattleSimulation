package city_gen_model.progression;

/**
 * Used to implement mathematical models such as linear, exponential or logistic
 */
public interface Progression {
    /**
     * Return quantity of house/people/building in the next time interval
     * Time interval is the number of time steps that the city simulation advances
     * @param currentValue
     * @param timeInterval
     * @return
     */
    double getNextValue(double currentValue, double timeInterval);
}

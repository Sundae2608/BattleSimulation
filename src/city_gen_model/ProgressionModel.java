package city_gen_model;

import city_gen_model.city_events.CityEvent;
import city_gen_model.city_events.CityEventScheduler;
import city_gen_model.progression.LogisticFunction;

import java.util.*;

public class ProgressionModel {

    private CityObjects cityObjects;
    private Map<CityObjectType, LogisticFunction> logisticFunctions;
    private CityEventScheduler cityEventScheduler;
    private int currentTimeStep;

    public ProgressionModel(CityObjects cityObjects) {
        this.cityEventScheduler = new CityEventScheduler();
        this.cityObjects = cityObjects;

        // Create Logistic functions from city objects
        this.logisticFunctions = new HashMap<>();
        for (CityObjectType cityObjectType : CityObjectType.values()) {
            double relativeGrowthCoefficient = this.cityObjects.getRelativeGrowthCoefficient(cityObjectType);
            double capacity = this.cityObjects.getCapacity(cityObjectType);

            logisticFunctions.put(cityObjectType, new LogisticFunction(relativeGrowthCoefficient, capacity));
        }
        this.currentTimeStep = 0;
    }

    /**
     * Add new progression functions to model.
     * @param cityEvent
     */
    public void registerEvent(CityEvent cityEvent) {
        cityEventScheduler.registerEvent(currentTimeStep + cityEvent.getTimeDelay() + 1, cityEvent);
    }

    /**
     *
     * @param numMonths
     */
    public void update(int numMonths) {
        // Get events in the next numMonths
        List<CityEvent> cityEvents = new ArrayList<>();
        for (int i = 0; i < numMonths; i++) {
            cityEvents.addAll(cityEventScheduler.getEvents(currentTimeStep+i));
        }

        // Modify current logistic functions
        if (cityEvents != null) {
            for (CityEvent cityEvent : cityEvents) {
                cityEvent.updateModel(this);
            }
        }

        // Apply functions in logistic function to cityObjects
        for (CityObjectType cityObjectType : CityObjectType.values()) {
            cityObjects.setQuantity(cityObjectType, logisticFunctions.get(cityObjectType).getNextValue(cityObjects
                    .getQuantity(cityObjectType), numMonths));
        }

        // Increase timeStep
        this.currentTimeStep += numMonths;
    }

    public void setRelativeGrowthCoefficient(CityObjectType objectType, double value) {
        logisticFunctions.get(objectType).setRelativeGrowthCoefficient(value);
    }

    public double getRelativeGrowthCoefficient(CityObjectType objectType) {
        return logisticFunctions.get(objectType).getRelativeGrowthCoefficient();
    }

    public void setCarryingCapacity(CityObjectType objectType, double value) {
        logisticFunctions.get(objectType).setCarryingCapacity(value);
    }

    public double getCarryingCapacity(CityObjectType objectType) {
        return logisticFunctions.get(objectType).getCarryingCapacity();
    }
}

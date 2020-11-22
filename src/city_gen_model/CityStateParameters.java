package city_gen_model;

import java.util.HashMap;
import java.util.Map;

/**
 * Store quantities, relative growth as well as capacity for each of the CityParameterType
 */
public class CityStateParameters {
    Map<CityParamType, Double> quantityMap;
    Map<CityParamType, Double> relativeGrowthCoefficientMap; // Used for logistic function
    Map<CityParamType, Double> capacityMap; // Used for logistic function

    public CityStateParameters() {
        quantityMap = new HashMap<>();
        relativeGrowthCoefficientMap = new HashMap<>();
        capacityMap = new HashMap<>();

        for (CityParamType cityParamType : CityParamType.values()) {
            quantityMap.put(cityParamType, 0.0);
            relativeGrowthCoefficientMap.put(cityParamType, 0.0);
            capacityMap.put(cityParamType, 0.0);
        }
    }

    public double getQuantity(CityParamType paramType) {
        return quantityMap.get(paramType);
    }

    public void setQuantity(CityParamType paramType, double value) {
        quantityMap.put(paramType, value);
    }

    public double getRelativeGrowthCoefficient(CityParamType paramType) {
        return relativeGrowthCoefficientMap.get(paramType);
    }

    public void setRelativeGrowthCoefficient(CityParamType paramType, double value) {
        relativeGrowthCoefficientMap.put(paramType, value);
    }

    public double getCapacity(CityParamType paramType) {
        return capacityMap.get(paramType);
    }

    public void setCapacity(CityParamType paramType, double value) {
        capacityMap.put(paramType, value);
    }

    /**
     * Determine if the city state is valid
     * @return
     */
    public boolean valid() {
        if (getQuantity(CityParamType.PERSON) > getQuantity(CityParamType.HOUSE)*2 &&
                getQuantity(CityParamType.PERSON) < getQuantity(CityParamType.HOUSE)*6) {
            return true;
        }
        return false;
    }
}
